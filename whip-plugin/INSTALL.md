# 🪢 Whip Plugin — Installation & Development Guide

## Quick Start

### Option 1: Install from JAR (Easiest)
If you already have a pre-built JAR file:

1. Open your JetBrains IDE (IntelliJ IDEA, PyCharm, WebStorm, etc.)
2. Go to **Settings → Plugins**
3. Click the ⚙️ icon → **Install plugin from disk**
4. Select the `whip-plugin-1.0.0.zip` file
5. Click **OK** and restart the IDE
6. Press **⌘⇧↩** to crack the whip!

---

## Building from Source

### Prerequisites
- **JDK 17+** (check: `java -version`)
- **Gradle 8.4+** (check: `gradle --version`)

### Step 1: Install Gradle (if needed)

**macOS (Homebrew):**
```bash
brew install gradle
```

**Other platforms:**
Visit [gradle.org/install](https://gradle.org/install/)

Verify installation:
```bash
gradle --version
```

### Step 2: Clone/Download the Plugin

```bash
cd /path/to/whip/whip-plugin
```

### Step 3: Build the Plugin

```bash
./build.sh
```

Or manually:
```bash
gradle buildPlugin
```

The output will be in `build/distributions/whip-plugin-1.0.0.zip`

### Step 4: Install the Built Plugin

1. Open your JetBrains IDE
2. **Settings → Plugins → ⚙️ → Install plugin from disk**
3. Navigate to `build/distributions/whip-plugin-1.0.0.zip`
4. Click **OK** → **Restart IDE**

---

## Development Setup

If you want to modify or debug the plugin:

1. Clone the repo
2. Open `whip-plugin/` folder in **IntelliJ IDEA**
3. Let IDEA auto-detect the Gradle project
4. Right-click `build.gradle.kts` → **Run Gradle**: `buildPlugin`

Or use the IDE's built-in **Run → Run 'buildPlugin'** task.

---

## Usage

Once installed:

- **Press ⌘⇧↩** anywhere in the IDE to crack the whip
- The whip animates from your cursor position toward the center of the editor
- Sound plays automatically (can be toggled in future versions)

---

## Troubleshooting

### "Gradle not found"
Install Gradle using Homebrew or visit [gradle.org](https://gradle.org/install)

### "JDK 17 not found"
Check: `java -version`

If needed, install via:
```bash
brew install openjdk@17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
```

### Plugin won't install
- Ensure IDE is on **2023.3 or later** (check: **Help → About**)
- Try re-building: `gradle clean buildPlugin`
- Check IDE logs: **Help → Show Log in Finder**

### Whip doesn't appear
- Restart the IDE completely
- Check hotkey isn't conflicting: **Settings → Keymap** (search "Whip")
- Press **⌘⇧↩** a few times

---

## Next Steps

Want to customize the plugin?

**Edit these files:**
- `src/main/kotlin/com/whip/WhipCanvas.kt` — animation & rendering
- `src/main/kotlin/com/whip/WhipSound.kt` — sound synthesis
- `src/main/kotlin/com/whip/VerletRope.kt` — physics

Then rebuild:
```bash
gradle clean buildPlugin
```

---

## Support

For issues or feature requests, see the main repo README.

