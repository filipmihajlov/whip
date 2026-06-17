// Preload — exposes a tiny bridge for calibration, sound, and auto-fire state.
const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('whipBridge', {
  setInteractive: (on) => ipcRenderer.send('whip-interactive', !!on),
  reportSound: (on) => ipcRenderer.send('whip-sound-init', !!on),
  onSoundChange: (cb) => ipcRenderer.on('whip-sound', (_, v) => cb(!!v)),
  reportAuto: (on) => ipcRenderer.send('whip-auto-init', !!on),
  onAutoChange: (cb) => ipcRenderer.on('whip-auto', (_, v) => cb(!!v)),
  reportColor: (id) => ipcRenderer.send('whip-color-init', id),
  onColorChange: (cb) => ipcRenderer.on('whip-color', (_, id) => cb(id)),
  reportSpeed: (id) => ipcRenderer.send('whip-speed-init', id),
  onSpeedChange: (cb) => ipcRenderer.on('whip-speed', (_, id) => cb(id)),
});

