#!/usr/bin/env bash
set -euo pipefail

SDK_DIR="/usr/local/lib/android/sdk"
CMDLINE_VERSION="11076708"
GRADLE_VERSION="8.7"

echo "──────────────────────────────────────────"
echo " Android SDK setup"
echo "──────────────────────────────────────────"

# ── 1. SDK directory ────────────────────────────────────────────────────────
sudo mkdir -p "$SDK_DIR/cmdline-tools"
sudo chown -R vscode:vscode /usr/local/lib/android

# ── 2. Command-line tools ───────────────────────────────────────────────────
echo "→ Downloading Android cmdline-tools ${CMDLINE_VERSION}…"
cd /tmp
wget -q "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_VERSION}_latest.zip" \
     -O cmdline-tools.zip
unzip -q cmdline-tools.zip
mkdir -p "$SDK_DIR/cmdline-tools/latest"
mv cmdline-tools/* "$SDK_DIR/cmdline-tools/latest/"
rm -rf cmdline-tools.zip cmdline-tools

# ── 3. SDK packages ─────────────────────────────────────────────────────────
SDKMANAGER="$SDK_DIR/cmdline-tools/latest/bin/sdkmanager"
echo "→ Accepting licenses…"
yes | "$SDKMANAGER" --licenses > /dev/null 2>&1 || true

echo "→ Installing SDK packages…"
"$SDKMANAGER" \
  "platform-tools" \
  "platforms;android-34" \
  "build-tools;34.0.0"

# ── 4. Gradle (standalone, no SDKMAN needed) ────────────────────────────────
if ! command -v gradle &> /dev/null; then
  echo "→ Installing Gradle ${GRADLE_VERSION}…"
  cd /tmp
  wget -q "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
       -O gradle.zip
  sudo unzip -q gradle.zip -d /opt
  sudo ln -sf "/opt/gradle-${GRADLE_VERSION}/bin/gradle" /usr/local/bin/gradle
  rm gradle.zip
  echo "   Gradle $(gradle --version | head -1) installed."
else
  echo "→ Gradle already available: $(gradle --version | head -1)"
fi

# ── 5. Gradle wrapper ───────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if [ -f "$SCRIPT_DIR/gradlew" ]; then
  chmod +x "$SCRIPT_DIR/gradlew"
  echo "→ gradlew made executable."
fi

echo ""
echo "✓ Android dev environment ready."
echo "  Java   : $(java -version 2>&1 | head -1)"
echo "  Gradle : $(gradle --version 2>/dev/null | head -1)"
echo "  SDK    : $SDK_DIR"