#!/usr/bin/env bash
# Installs the Android command-line SDK and bootstraps the Gradle wrapper
# so that `./gradlew assembleDebug` works inside the Codespace.
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-/usr/local/android-sdk}"
CMDLINE_TOOLS_VERSION="11076708"   # cmdline-tools 12.0
PLATFORM="android-35"
BUILD_TOOLS="35.0.0"

echo "==> Installing Android SDK into ${ANDROID_HOME}"
sudo mkdir -p "${ANDROID_HOME}/cmdline-tools"
sudo chown -R "$(whoami)" "${ANDROID_HOME}"

if [ ! -d "${ANDROID_HOME}/cmdline-tools/latest" ]; then
    TMP_ZIP="/tmp/cmdline-tools.zip"
    curl -fsSL -o "${TMP_ZIP}" \
        "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip"
    unzip -q "${TMP_ZIP}" -d "${ANDROID_HOME}/cmdline-tools"
    # The zip extracts to a folder named "cmdline-tools"; rename to "latest".
    mv "${ANDROID_HOME}/cmdline-tools/cmdline-tools" "${ANDROID_HOME}/cmdline-tools/latest"
    rm -f "${TMP_ZIP}"
fi

export PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}"

echo "==> Accepting licenses"
yes | sdkmanager --licenses >/dev/null 2>&1 || true

echo "==> Installing platform-tools, ${PLATFORM}, build-tools;${BUILD_TOOLS}"
sdkmanager "platform-tools" "platforms;${PLATFORM}" "build-tools;${BUILD_TOOLS}" >/dev/null

# Make the SDK location discoverable by Gradle.
echo "sdk.dir=${ANDROID_HOME}" > local.properties

echo "==> Generating Gradle wrapper (gradle 8.9)"
if [ ! -f gradlew ]; then
    # The base image ships SDKMAN with a Gradle; use it to create the wrapper.
    if command -v gradle >/dev/null 2>&1; then
        gradle wrapper --gradle-version 8.9
    else
        echo "WARNING: 'gradle' not on PATH; install it or run 'gradle wrapper' manually."
    fi
fi

echo "==> Done. Build with:  ./gradlew assembleDebug"
