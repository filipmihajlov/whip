// Electron main process — transparent, click-through, always-on-top overlay
// covering the primary display. Listens for a global hotkey and tells the
// renderer to fire off a whip-crack animation.

const {
  app,
  BrowserWindow,
  globalShortcut,
  screen,
  Tray,
  Menu,
  nativeImage,
  ipcMain,
  systemPreferences,
  dialog,
} = require('electron');
const path = require('path');
const http = require('http');

// Allow WebAudio to start without a user gesture — the overlay never gets one
// because it's click-through.
app.commandLine.appendSwitch('autoplay-policy', 'no-user-gesture-required');

// ---- Config (edit to taste) ----
// macOS-style accelerator. Default: ⌘⇧↩ (Cmd+Shift+Enter). Pick something that
// does NOT conflict with the JetBrains "send message" shortcut (usually ⌘↩).
const HOTKEY = process.env.WHIP_HOTKEY || 'CommandOrControl+Shift+Return';
const CALIBRATE_HOTKEY = 'CommandOrControl+Shift+Alt+W';
const AUTO_COOLDOWN_MS = 400;   // throttle to avoid overlapping cracks
const AUTO_REQUIRE_META = false; // true = fire only on ⌘+Enter; false = any Enter
const HTTP_PORT = Number(process.env.WHIP_HTTP_PORT) || 7654;

let win = null;
let tray = null;
let httpServer = null;
let soundEnabled = true; // hydrated from renderer
let autoEnabled = false; // hydrated from renderer
let hookStarted = false;
let lastAutoFire = 0;

function createOverlay() {
  const display = screen.getPrimaryDisplay();
  const { x, y, width, height } = display.bounds;

  win = new BrowserWindow({
    x,
    y,
    width,
    height,
    transparent: true,
    frame: false,
    alwaysOnTop: true,
    skipTaskbar: true,
    resizable: false,
    movable: false,
    focusable: false,        // never steal focus from the app underneath
    hasShadow: false,
    fullscreenable: false,
    show: true,
    backgroundColor: '#00000000',
    webPreferences: {
      contextIsolation: true,
      nodeIntegration: false,
      backgroundThrottling: false,
      preload: path.join(__dirname, 'preload.js'),
    },
  });

  // Click-through: forward mouse events to whatever is underneath.
  win.setIgnoreMouseEvents(true, { forward: true });
  // Float above full-screen apps + every workspace.
  win.setAlwaysOnTop(true, 'screen-saver');
  win.setVisibleOnAllWorkspaces(true, { visibleOnFullScreen: true });

  win.loadFile(path.join(__dirname, 'overlay.html'));

  // Uncomment to debug the overlay:
  // win.webContents.openDevTools({ mode: 'detach' });
}

function triggerCrack() {
  if (!win || win.isDestroyed()) return;
  // Calls a global function defined in overlay.html.
  win.webContents
    .executeJavaScript('window.whipCrack && window.whipCrack();')
    .catch((err) => console.error('whipCrack failed:', err));
}

function toggleCalibration() {
  if (!win || win.isDestroyed()) return;
  win.webContents
    .executeJavaScript('window.whipCalibrate && window.whipCalibrate();')
    .catch(() => {});
}

function setCalibrationInteractive(interactive) {
  if (!win || win.isDestroyed()) return;
  if (interactive) {
    win.setIgnoreMouseEvents(false);
    win.setFocusable(true);
    win.focus();
  } else {
    win.setIgnoreMouseEvents(true, { forward: true });
    win.setFocusable(false);
  }
}

function createTray() {
  // Empty 16x16 image; macOS will fall back to the title text.
  const icon = nativeImage.createEmpty();
  tray = new Tray(icon);
  tray.setTitle('🪢');
  tray.setToolTip('Whip overlay');
  rebuildTrayMenu();
}

function rebuildTrayMenu() {
  if (!tray) return;
  const menu = Menu.buildFromTemplate([
    { label: `Whip now (${HOTKEY})`, click: () => triggerCrack() },
    {
      label: `Auto-fire on ${AUTO_REQUIRE_META ? '⌘↩' : '↩'}`,
      type: 'checkbox',
      checked: autoEnabled,
      click: (item) => setAuto(item.checked, /* fromTray */ true),
    },
    {
      label: 'Sound',
      type: 'checkbox',
      checked: soundEnabled,
      click: (item) => {
        soundEnabled = item.checked;
        if (win && !win.isDestroyed()) {
          win.webContents.send('whip-sound', soundEnabled);
        }
      },
    },
    { type: 'separator' },
    { label: `HTTP trigger: 127.0.0.1:${HTTP_PORT}/crack`, enabled: false },
    { label: `Calibrate origin/target (${CALIBRATE_HOTKEY})`, click: () => toggleCalibration() },
    { type: 'separator' },
    { label: 'Quit', role: 'quit' },
  ]);
  tray.setContextMenu(menu);
}

// ---- Auto-fire (non-consuming OS-level keyboard observation) ----
function setAuto(on, fromTray = false) {
  // Auto-fire needs Accessibility permission, which on macOS usually requires
  // admin rights. If we can't get it, fall back gracefully and tell the user.
  if (on && process.platform === 'darwin') {
    const granted = systemPreferences.isTrustedAccessibilityClient(false);
    if (!granted) {
      const choice = dialog.showMessageBoxSync({
        type: 'info',
        title: 'Auto-fire needs Accessibility permission',
        message: "Can't enable auto-fire on this Mac",
        detail:
          "Auto-fire needs to observe ⌘↩ at the OS level (so JetBrains still " +
          "receives it). That requires Accessibility permission in System Settings → " +
          "Privacy & Security → Accessibility, which usually needs admin rights.\n\n" +
          "Workaround without admin:\n" +
          "  1. In JetBrains: Settings → Keymap, search for the AI Assistant " +
          "\"Send message\" action.\n" +
          "  2. Change its shortcut to ⌘⇧↩ (Cmd+Shift+Return).\n" +
          "  3. Press ⌘⇧↩ to send — the whip will fire automatically.\n\n" +
          "Auto-fire toggle will stay OFF.",
        buttons: ['OK'],
        defaultId: 0,
      });
      void choice;
      // Revert: don't enable, tell renderer to uncheck.
      autoEnabled = false;
      if (win && !win.isDestroyed()) win.webContents.send('whip-auto', false);
      if (fromTray) rebuildTrayMenu();
      return;
    }
  }

  autoEnabled = !!on;
  if (autoEnabled) startHook();
  else stopHook();
  if (win && !win.isDestroyed()) win.webContents.send('whip-auto', autoEnabled);
  if (fromTray) rebuildTrayMenu();
}

function ensureAccessibilityPermission() {
  // Kept for symmetry but no longer prompts on every toggle — setAuto handles it.
  if (process.platform !== 'darwin') return true;
  return systemPreferences.isTrustedAccessibilityClient(false);
}

function startHook() {
  if (hookStarted) return;
  let uIOhook, UiohookKey;
  try {
    ({ uIOhook, UiohookKey } = require('uiohook-napi'));
  } catch (err) {
    console.error('uiohook-napi not installed:', err);
    return;
  }
  try {
    uIOhook.removeAllListeners('keydown');
    uIOhook.on('keydown', (e) => {
      if (e.keycode !== UiohookKey.Enter) return;
      if (AUTO_REQUIRE_META && !e.metaKey) return;
      const now = Date.now();
      if (now - lastAutoFire < AUTO_COOLDOWN_MS) return;
      lastAutoFire = now;
      triggerCrack();
    });
    uIOhook.start();
    hookStarted = true;
  } catch (err) {
    console.error('uiohook start failed:', err);
  }
}

function stopHook() {
  if (!hookStarted) return;
  try {
    const { uIOhook } = require('uiohook-napi');
    uIOhook.removeAllListeners('keydown');
    uIOhook.stop();
  } catch (err) {
    console.error('uiohook stop failed:', err);
  }
  hookStarted = false;
}

// ---- Local HTTP trigger: POST/GET http://127.0.0.1:PORT/crack ----
// Used by JetBrains External Tools (curl) so a single ⌘↩ macro can both send
// the message and fire the whip — no Accessibility permission needed.
function startHttpServer() {
  if (httpServer) return;
  httpServer = http.createServer((req, res) => {
    // CORS so a userscript / web page can ping too.
    res.setHeader('Access-Control-Allow-Origin', '*');
    if (req.method === 'OPTIONS') { res.writeHead(204); res.end(); return; }
    const url = (req.url || '/').split('?')[0];
    if (url === '/crack' || url === '/') {
      triggerCrack();
      res.writeHead(204);
      res.end();
    } else {
      res.writeHead(404);
      res.end('Not found. Try POST /crack');
    }
  });
  // 127.0.0.1 only — never expose to the network.
  httpServer.listen(HTTP_PORT, '127.0.0.1', () => {
    console.log(`Whip HTTP trigger: http://127.0.0.1:${HTTP_PORT}/crack`);
  });
  httpServer.on('error', (err) => console.error('HTTP server error:', err));
}

function stopHttpServer() {
  if (httpServer) {
    httpServer.close();
    httpServer = null;
  }
}

app.whenReady().then(() => {
  // macOS: stay out of the Dock — overlay only.
  if (process.platform === 'darwin' && app.dock) app.dock.hide();

  createOverlay();
  createTray();
  startHttpServer();

  if (!globalShortcut.register(HOTKEY, triggerCrack)) {
    console.error(`Failed to register hotkey: ${HOTKEY}`);
  }
  if (!globalShortcut.register(CALIBRATE_HOTKEY, toggleCalibration)) {
    console.error(`Failed to register calibration hotkey: ${CALIBRATE_HOTKEY}`);
  }

  // Renderer asks us to toggle click-through during calibration.
  ipcMain.on('whip-interactive', (_, on) => setCalibrationInteractive(on));

  // Renderer reports its hydrated sound state so the tray checkbox matches.
  ipcMain.on('whip-sound-init', (_, on) => {
    soundEnabled = !!on;
    rebuildTrayMenu();
  });

  // Renderer reports its hydrated auto-fire state.
  ipcMain.on('whip-auto-init', (_, on) => {
    autoEnabled = !!on;
    if (autoEnabled) {
      // Only actually start the hook if Accessibility is granted; otherwise
      // silently revert so the tray shows the correct (off) state.
      if (process.platform === 'darwin' &&
          !systemPreferences.isTrustedAccessibilityClient(false)) {
        autoEnabled = false;
        if (win && !win.isDestroyed()) win.webContents.send('whip-auto', false);
      } else {
        startHook();
      }
    }
    rebuildTrayMenu();
  });
});

app.on('will-quit', () => {
  globalShortcut.unregisterAll();
  stopHook();
  stopHttpServer();
});
// Keep alive when the (only) overlay window is "closed" — we never close it.
app.on('window-all-closed', () => {});




