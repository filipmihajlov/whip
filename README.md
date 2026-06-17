# 🪢 Whip

A pair of toys that crack an animated whip on top of your AI chat — because sometimes the assistant *needs* it.

Two flavours, both self-contained, no admin permissions needed:

| Folder | What it is | When to use it |
|---|---|---|
| [`whip-demo/`](whip-demo/) | Single HTML file with a fake chat that whips itself on Send | See the animation in 5 seconds — just open `index.html` |
| [`whip-overlay/`](whip-overlay/) | Electron app: transparent, click-through, always-on-top overlay that draws the whip on **any** window | Real use — strike your actual AI chat |

## Quick start

### Demo (browser)

```sh
open whip-demo/index.html
```

Type something, press **Enter**, watch the whip crack.

### Overlay (Electron, runs in background)

```sh
cd whip-overlay
npm install     # one-time
npm start
```

A 🪢 appears in the macOS menu bar. Triggers:

- **⌘⇧↩** anywhere → whip cracks
- **🪢 menu → "Whip now"** → same thing, via the tray
- **POST http://127.0.0.1:7654/crack** → fire from any app or script (curl, JetBrains External Tools, Stream Deck, etc.)

See [`whip-overlay/README.md`](whip-overlay/README.md) for calibration, sound toggle, and the JetBrains macro setup that makes ⌘↩ both send the message *and* fire the whip.

## What's under the hood

- **Whip rope** — 24-point Verlet physics chain with distance-constraint relaxation. Wind-up → snap → impact → recoil scripted; intermediate points free-move so the rope curves naturally.
- **Impact** — radial flash with `mix-blend-mode: screen`, CSS shake, floating damage number.
- **Sound** — WebAudio: filtered noise burst (the "crack") + 160 → 42 Hz sine sweep (the "thwack"). No audio files.
- **HTTP trigger** (overlay only) — tiny `http.createServer` bound to `127.0.0.1` so scripts and shortcuts can fire it without OS keyboard hooks.

## Built with

- Vanilla HTML / CSS / JS (no framework)
- Electron 31 (overlay only)
- `uiohook-napi` (optional, for auto-fire via OS-level key observation — needs macOS Accessibility permission, so usually disabled)

## License

MIT — go nuts.

