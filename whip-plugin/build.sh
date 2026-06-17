#!/bin/bash

set -euo pipefail

PLUGIN_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOLS_DIR="$PLUGIN_DIR/.local-tools"
GRADLE_VERSION="8.4"
GRADLE_DIR="$TOOLS_DIR/gradle-$GRADLE_VERSION"
GRADLE_BIN="$GRADLE_DIR/bin/gradle"

echo "Building Whip plugin from $PLUGIN_DIR"

mkdir -p "$TOOLS_DIR"

if [ ! -x "$GRADLE_BIN" ]; then
  echo "Downloading Gradle $GRADLE_VERSION..."
  curl -L --fail "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$TOOLS_DIR/gradle-$GRADLE_VERSION-bin.zip"
  unzip -oq "$TOOLS_DIR/gradle-$GRADLE_VERSION-bin.zip" -d "$TOOLS_DIR"
fi

if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME}/bin/java" ]; then
  JDK_DIR="$(find "$TOOLS_DIR" -maxdepth 2 -type d -name "jdk-17*" | head -1 || true)"
  if [ -z "$JDK_DIR" ]; then
    echo "Downloading local JDK 17..."
    curl -L --fail "https://api.adoptium.net/v3/binary/latest/17/ga/mac/aarch64/jdk/hotspot/normal/eclipse?project=jdk" -o "$TOOLS_DIR/jdk17.tar.gz"
    tar -xzf "$TOOLS_DIR/jdk17.tar.gz" -C "$TOOLS_DIR"
    JDK_DIR="$(find "$TOOLS_DIR" -maxdepth 2 -type d -name "jdk-17*" | head -1)"
  fi
  export JAVA_HOME="$JDK_DIR/Contents/Home"
fi

export PATH="$JAVA_HOME/bin:$PATH"

echo "Using Java:"
java -version | cat

echo "Running buildPlugin..."
"$GRADLE_BIN" -p "$PLUGIN_DIR" buildPlugin

echo ""
echo "✅ Build complete!"
echo ""
JAR_PATH=$(find "$PLUGIN_DIR/build/distributions" -name "*.zip" | head -1)
if [ -f "$JAR_PATH" ]; then
    echo "📦 Plugin JAR: $JAR_PATH"
    echo ""
    echo "To install:"
    echo "1. Open your JetBrains IDE"
    echo "2. Go to Settings → Plugins"
    echo "3. Click ⚙️ → Install plugin from disk"
    echo "4. Select: $JAR_PATH"
    echo "5. Restart the IDE"
fi

