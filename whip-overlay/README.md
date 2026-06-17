# Whip Overlay 🪢
A transparent, click-through, always-on-top window that draws an animated
whip-crack on top of whatever app you're in — including the JetBrains AI
chat panel.
## Install
```sh
cd whip-overlay
npm install
```
(Downloads Electron ~100 MB the first time.)
## Run
```sh
npm start
```
A 🪢 icon appears in your macOS menu bar. The overlay is invisible until you
trigger it.
## Use
| Action | Shortcut |
|---|---|
| Crack the whip | **⌘⇧↩** (Cmd + Shift + Enter) |
| Calibrate origin/target | **⌃⌥⇧W** (Ctrl + Alt + Shift + W) |
| Quit | Menu bar 🪢 → Quit |
**Why not ⌘↩?** That's the JetBrains "send message" shortcut, and Electron
`globalShortcut` would steal it. So the whip is on a separate gesture: press
⌘↩ to send your message, then ⌘⇧↩ for the whip. You can fire them in quick
succession (or even simultaneously) for the same feel.
> Want it to crack **automatically** when you press ⌘↩ without intercepting
> the keystroke? See [Bonus](#bonus-fire-automatically-on-) below.
## Calibrate
Press **⌃⌥⇧W**. A dim overlay appears with two draggable markers:
- 🟢 **Green** marker — drag onto your JetBrains chat **Send** button.
- 🔴 **Red** marker — drag onto the **assistant avatar** of the latest reply.
Press **Enter** to save, **Esc** to discard. Saved in `localStorage` of the
overlay; persists across restarts.
Use the **Test crack** button in the calibration card to preview before
saving.
## Configure
Environment variables:
| Variable | Default | Notes |
|---|---|---|
| `WHIP_HOTKEY` | `CommandOrControl+Shift+Return` | Electron accelerator string |
Example:
```sh
WHIP_HOTKEY="F13" npm start
```
## Bonus: fire automatically on ⌘↩
`Electron.globalShortcut` always consumes its accelerator. To *observe* ⌘↩
without consuming it (so JetBrains still sends the message), use
[`uiohook-napi`](https://github.com/SnosMe/uiohook-napi):
```sh
npm install uiohook-napi
```
Then in `main.js` replace the `globalShortcut.register` block with a
`uiohook-napi` listener that watches for ⌘↩ and calls `triggerCrack()`.
macOS will prompt for **Accessibility** permission — grant it under
*System Settings → Privacy & Security → Accessibility*.
## Known limits
- Targets the **primary display** only. Multi-monitor needs creating one
  overlay window per `screen.getAllDisplays()` entry.
- On macOS, transparent always-on-top windows need a focused app to render
  correctly in some edge cases (Mission Control, etc.).
- Origin/target are pixel coordinates — they don't follow the chat panel if
  you move/resize the tool window. Recalibrate after rearranging your IDE
  layout.
## Files
- `package.json` — Electron app manifest
- `main.js`      — Electron main process: window, tray, global hotkeys
- `preload.js`   — tiny IPC bridge for calibration interactivity
- `overlay.html` — renderer: whip physics + animation + calibration UI
About ~500 lines of JS in total. Edit and reload (Quit + `npm start`) to iterate.
