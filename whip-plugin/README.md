# 🪢 Whip Plugin for JetBrains IDEs

A JetBrains IDE plugin that cracks an animated whip on top of your code — because sometimes your IDE *needs* it.

## Features

- **24-point Verlet physics** — smooth, realistic rope dynamics
- **Hotkey support** — Press **⌘⇧↩** (Cmd+Shift+Enter) to fire
- **Synthesized sound** — crack + thwack, generated in real-time (no audio files)
- **Impact effects** — radial flash and screen shake
- **Customizable** — toggle auto-fire and sound in **Settings | Tools | Whippy**

## Installation

### From JetBrains Marketplace (once published)
1. Open your JetBrains IDE
2. Go to **Settings → Plugins → Marketplace**
3. Search for "Whip"
4. Click **Install** and restart

### Local Development
1. Clone or download this repo
2. Open `whip-plugin/` in IntelliJ IDEA
3. Run **Build → Build Plugin JAR**
4. Go to **Settings → Plugins → Install plugin from disk**
5. Select the generated JAR and restart

## Usage

Just press **⌘⇧↩** in any JetBrains IDE (IntelliJ IDEA, PyCharm, WebStorm, etc.) to crack the whip.

The whip will:
1. Wind up from your editor
2. Snap forward in a sweeping arc
3. Crack with a visual flash and sound
4. Recoil smoothly back to rest

## How It Works

- **Physics**: 24-point Verlet chain with distance constraints, gravity, and damping
- **Sound**: Synthesized in real-time using Java's `javax.sound.sampled`
  - Noise burst (high-pass filtered) for the "crack"
  - Sine sweep (160 → 42 Hz) for the "thwack"
- **Rendering**: Canvas-based 2D graphics with anti-aliasing

## Building

```bash
cd whip-plugin
./gradlew buildPlugin
```

Output JAR: `build/distributions/whip-plugin-1.0.0.jar`

## Requirements

- IntelliJ IDEA 2023.3+
- JDK 17+

## License

MIT — go nuts.

