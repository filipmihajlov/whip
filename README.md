# 🪢 Whip

A JetBrains plugin that cracks an animated whip on top of your editor — because sometimes the AI *needs* motivation.

Works with **IntelliJ IDEA**, **Android Studio**, and all JetBrains IDEs (2023.3+).

## Install

1. Download the latest zip from `whip-plugin/build/distributions/`
2. In your IDE: **Settings → Plugins → ⚙️ → Install Plugin from Disk…** → select the zip
3. Restart

## Usage

| Trigger | What happens |
|---|---|
| **⌘⇧↩** (Cmd+Shift+Enter) | Crack the whip |
| **Tools → Calibrate Whip Layout** | Drag start/end points to position the whip |
| **Settings → Tools → Whippy** | Toggle sound, auto-mode, color themes, speed profiles |

### Auto-mode (fire on Enter)

When enabled in settings, the whip fires on every plain **Enter** keypress across the IDE. See the FAQ below for limiting it to Copilot Chat only.

## What's under the hood

- **Whip rope** — 24-point Verlet physics chain with distance-constraint relaxation. Wind-up → snap → impact → recoil scripted; intermediate points free-move so the rope curves naturally.
- **Impact** — radial flash with screen-blend, shake, floating damage number.
- **Sound** — synthesized noise burst (the "crack") + low sine sweep (the "thwack"). No audio files.

## Build from source

```sh
cd whip-plugin
./gradlew clean buildPlugin
```

Output zip: `whip-plugin/build/distributions/`

## Compatibility

| IDE | Minimum version |
|---|---|
| IntelliJ IDEA (Community & Ultimate) | 2023.3 |
| Android Studio | Jellyfish (2023.3.1) |
| WebStorm, PyCharm, CLion, etc. | 2023.3 |

The plugin declares `com.intellij.modules.platform` as its only dependency, which is the base module present in every JetBrains IDE.

## FAQ

### Can the whip fire only when pressing Enter in GitHub Copilot Chat?

**Yes, with a code change.** Currently auto-mode fires on every unmodified Enter keypress IDE-wide. To restrict it to Copilot Chat only, the `WhipEditorListener` dispatcher can inspect the focused Swing component hierarchy and check whether it belongs to the Copilot Chat tool window (e.g., by walking up the parent chain looking for the `"GitHub Copilot"` tool window content). This is technically straightforward but relies on Copilot's internal UI structure which isn't a stable public API — it may need updates when Copilot versions change.

## License

MIT
