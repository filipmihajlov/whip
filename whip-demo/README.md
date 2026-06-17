# Whip for Copilot — Local Demo
A self-contained HTML demo of the whip-when-you-send animation. **No build, no deps.**
## Run
```sh
open whip-demo/index.html
```
…or just double-click the file.
## Try this
- Type anything, press **Enter**, watch the whip wind up and crack.
- Toggle **Sound** in the side panel (synthesized via WebAudio, no files).
- Switch **Intensity** between Gentle / Firm / Merciless.
- Send several messages quickly to confirm it does not break.
## How it works
| Piece | Implementation |
|---|---|
| Whip rope | Verlet integration, 24-point chain, 18 distance-constraint iterations per frame |
| Animation | 4 phases: wind-up, snap, impact, recoil. Tip pinned to a scripted path during wind-up + snap, then released for natural recoil |
| Impact flash | DOM div with radial-gradient + mix-blend-mode: screen |
| Avatar reaction | CSS shake keyframes + inset red box-shadow |
| Damage number | Floating DOM element with transform + opacity transition |
| Sound | WebAudio: filtered noise burst + 150Hz to 45Hz sine thwack |
All in one file, ~500 lines.
## What's tunable
- `N` — rope segment count (default 24)
- `T_WIND`, `T_SNAP`, `T_HOLD`, `T_RECOIL` — phase durations in ms
- `windBack` — wind-up offset relative to handle origin
- gravity (the `+ 0.45` in `physicsStep`)
- Leather colours (the r/g/bch calc in `drawRope`)
## Next step (once you like the look)
Port this to a JetBrains plugin that injects the same JS into the AI chat's JCEF webview. The animation logic stays identical; only the origin and target DOM selectors change to point at the real send button and the real assistant avatar.
