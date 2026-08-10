#!/usr/bin/env bash
set -e

# Set target Android SDK directory
SDK_DIR="/usr/local/lib/android/sdk"
sudo mkdir -p "$SDK_DIR/cmdline-tools"
sudo chown -R vscode:vscode "$SDK_DIR"

# Download Android Command-Line Tools
CMDLINE_VERSION="11076708" # Latest command-line tools version
cd /tmp
wget -q "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_VERSION}_latest.zip"
unzip -q "commandlinetools-linux-${CMDLINE_VERSION}_latest.zip"

# Move tools to the expected structure ($ANDROID_HOME/cmdline-tools/latest/bin)
mkdir -p "$SDK_DIR/cmdline-tools/latest"
mv cmdline-tools/* "$SDK_DIR/cmdline-tools/latest/" 2>/dev/null || true
rm -rf "commandlinetools-linux-${CMDLINE_VERSION}_latest.zip" cmdline-tools

# Accept licenses & install platforms/build-tools
yes | "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" --licenses
"$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Make Gradle wrapper executable if present
if [ -f "./gradlew" ]; then
  chmod +x ./gradlew
fi