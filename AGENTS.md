# AGENTS.md

## Big picture
- This repo delivers the same whip-crack experience through 3 independent targets: browser demo, Electron overlay, and JetBrains plugin.
- Keep behavior parity (rope motion feel, phase timing, sound character) across targets; there is no shared runtime package.

## Components
- `whip-demo/` (`index.html`): single-file vanilla HTML/CSS/JS reference implementation.
- `whip-overlay/` (`main.js`, `overlay.html`, `preload.js`): transparent always-on-top Electron overlay with hotkey + local HTTP trigger.
- `whip-plugin/` (`src/main/kotlin/com/whip/*`): IntelliJ plugin rendering whip in editors.

## Data flow and boundaries
- Trigger sources (hotkey/UI/HTTP/action) call a target-specific crack entrypoint.
- Animation uses a 24-point Verlet rope and 4 phases: wind-up -> snap -> impact/hold -> recoil.
- Sound is synthesized at runtime (noise crack + low thwack), no audio assets.
- Overlay boundary: main process handles window/triggers; renderer draws; `preload.js` exposes minimal IPC.
- Plugin boundary: `WhipAction` -> trigger/canvas classes; settings via `WhipSettingsState` (`PersistentStateComponent`).

## Workflows
```bash
open whip-demo/index.html
cd whip-overlay && npm install && npm start
WHIP_HOTKEY="F13" WHIP_HTTP_PORT=8080 npm start
cd whip-plugin && ./build.sh
cd whip-plugin && ./gradlew clean buildPlugin
```
- Plugin zip output: `whip-plugin/build/distributions/`.

## Project conventions
- Primary trigger key is `Cmd+Shift+Enter` (chosen to avoid JetBrains `Cmd+Enter` conflicts).
- Overlay calibration temporarily disables click-through; preserve this interaction model.
- Overlay HTTP endpoint is local-only (`POST http://127.0.0.1:<port>/crack`); avoid broadening bind scope by default.
- Persist settings in existing stores only: overlay `localStorage`, plugin `WhipSettingsState`.
- If retuning animation constants/timings, align all 3 targets in the same change.

## Integration points
- Overlay deps: `electron`, optional `uiohook-napi` (macOS Accessibility permission required).
- Plugin toolchain: IntelliJ Platform 2023.3, Kotlin JVM, JDK 17 target.

## Read first
- `README.md`
- `whip-demo/index.html`
- `whip-overlay/main.js`, `whip-overlay/overlay.html`, `whip-overlay/preload.js`
- `whip-plugin/build.gradle.kts`, `whip-plugin/src/main/resources/META-INF/plugin.xml`
- `whip-plugin/src/main/kotlin/com/whip/WhipCanvas.kt`, `whip-plugin/src/main/kotlin/com/whip/VerletRope.kt`, `whip-plugin/src/main/kotlin/com/whip/WhipSound.kt`

## Existing AI guidance
- No prior `AGENTS.md`/`AGENT.md`/`CLAUDE.md`/cursor/windsurf/cline rule files were found; guidance came from project `README.md` files.
