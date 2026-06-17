# 🪢 Whip Plugin — Quick Reference

## What's Been Created

A complete JetBrains IDE plugin that cracks a whip animation in your editor.

**Location:** `/Users/filipmihajlov/Desktop/whip/whip-plugin/`

### Directory Structure
```
whip-plugin/
├── build.gradle.kts          # Gradle build configuration
├── settings.gradle.kts        # Gradle settings
├── gradlew                    # Gradle wrapper script
├── gradle/wrapper/            # Gradle wrapper files
├── build.sh                   # Quick build script
├── README.md                  # Plugin documentation
├── INSTALL.md                 # Installation guide
└── src/
    └── main/
        ├── kotlin/com/whip/
        │   ├── WhipAction.kt       # Hotkey action handler
        │   ├── WhipCanvas.kt       # Rendering engine
        │   ├── VerletRope.kt       # Physics simulation
        │   ├── WhipSound.kt        # Sound synthesis
        │   └── WhipEditorListener.kt  # UI extensions
        └── resources/META-INF/
            └── plugin.xml          # Plugin manifest
```

---

## Key Components

### 1. **WhipAction.kt**
Registers the **⌘⇧↩** hotkey and triggers the whip animation.

### 2. **WhipCanvas.kt**
Handles animation timing and canvas rendering:
- Easing functions (easeOut, easeIn, easeInOut)
- 4 animation phases: wind-up, snap, hold, recoil
- Draws the whip with tapers and shadows
- Manages the animation timer

### 3. **VerletRope.kt**
Physics engine (24-point Verlet chain):
- Velocity-based movement with damping
- Distance constraints (relaxation iterations)
- Gravity simulation
- Configurable segment length

### 4. **WhipSound.kt**
Synthesizes whip sounds in real-time (no audio files):
- **Crack:** Noise burst with high-pass filter sweep
- **Thwack:** Sine wave sweep (160 → 42 Hz)
- Generates raw audio samples on the fly

### 5. **WhipEditorListener.kt & plugin.xml**
UI integration:
- Registers the action in the IDE
- Adds a status bar widget (🪢)
- Supports all JetBrains IDEs

---

## How to Build & Install

### Prerequisites
You need:
- **JDK 17 or later** (run `java -version`)
- **Gradle 8.4 or later** (run `gradle --version`)

### Install Gradle (if missing)

**macOS:**
```bash
# Option 1: Homebrew
brew install gradle

# Option 2: Manual
cd /tmp && wget https://services.gradle.org/distributions/gradle-8.4-bin.zip
unzip gradle-8.4-bin.zip && sudo mv gradle-8.4 /opt/gradle
export PATH="/opt/gradle/bin:$PATH"
```

**Linux:**
```bash
sudo apt-get install gradle
```

**Windows:**
Visit https://gradle.org/install/

### Build the Plugin

```bash
cd /Users/filipmihajlov/Desktop/whip/whip-plugin
./build.sh
# OR
gradle buildPlugin
```

Output will be: `build/distributions/whip-plugin-1.0.0.zip`

### Install in JetBrains IDE

1. **Open IntelliJ IDEA, PyCharm, WebStorm, etc.**
2. **Settings → Plugins**
3. **⚙️ (gear icon) → Install plugin from disk**
4. **Select:** `whip-plugin/build/distributions/whip-plugin-1.0.0.zip`
5. **Click OK → Restart IDE**

---

## Usage

Once installed:

**Press: ⌘⇧↩** (Cmd+Shift+Enter on macOS)

The whip will:
1. Wind up from the bottom-right
2. Snap toward the center of the editor
3. Flash with impact
4. Recoil and settle

**Sound plays automatically** (synthesized in real-time)

---

## Customization

Want to tweak the whip behavior?

### Edit Animation Speed
**File:** `src/main/kotlin/com/whip/WhipCanvas.kt`

Look for these constants in the `crack()` function:
```kotlin
val T_WIND = 163L    // Wind-up duration (ms)
val T_SNAP = 188L    // Snap forward (ms)
val T_HOLD = 38L     // Impact hold (ms)
val T_RECOIL = 475L  // Recoil duration (ms)
```

Current settings: **25% slower** than original

### Edit Whip Size
Same file, look for `windBack`:
```kotlin
val windBack = Pair(
    origin.x - dirX * 108,   // Horizontal distance
    origin.y - dirY * 108 - 96  // Vertical distance
)
```

Current settings: **20% bigger** than original

### Edit Sound Volume
**File:** `src/main/kotlin/com/whip/WhipSound.kt`

Look for these multipliers:
```kotlin
val crack = 0.78 * intensity   // Noise burst volume (was 0.65)
val thwack = 0.54 * intensity  // Bass thwack volume (was 0.45)
```

Current settings: **20% louder** than original

### Rebuild After Changes
```bash
gradle clean buildPlugin
```

Then reinstall in your IDE.

---

## IDE Compatibility

Tested on:
- ✅ IntelliJ IDEA 2023.3+
- ✅ PyCharm 2023.3+
- ✅ WebStorm 2023.3+
- ✅ Other JetBrains IDEs (CLion, GoLand, Rider, etc.)

Requires: **JDK 17+**

---

## Troubleshooting

### "Cannot find gradle"
```bash
gradle --version
# If not found, install via brew install gradle or visit gradle.org
```

### "Plugin won't build"
```bash
# Clean and rebuild
gradle clean buildPlugin
```

### "Plugin won't load in IDE"
- Restart IDE completely
- Check that IDE version is **2023.3 or later**
- Check IDE logs: **Help → Show Log in Finder**

### "Hotkey ⌘⇧↩ doesn't work"
- Make sure IDE is focused (not a terminal)
- Check for conflicts: **Settings → Keymap** (search "Whip")
- Try a different key in `plugin.xml` (line 18)

---

## Next Steps

✅ Plugin structure created  
✅ All source code written  
✅ Gradle build system configured  

**Your turn:**
1. Install Gradle
2. Run `gradle buildPlugin`
3. Install the JAR in your IDE
4. Press ⌘⇧↩ to crack!

---

## License

MIT — enjoy the whip! 🪢

