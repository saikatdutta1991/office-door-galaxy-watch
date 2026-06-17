# Office Door — Galaxy Watch 4 Unlock

A minimal Wear OS app: one full-screen circular **UNLOCK** button. Tapping it
POSTs to the unlock API, then flashes the screen **green ×3** on success or
**red ×3** on failure.

- UI: Jetpack Compose for Wear OS
- Target: Galaxy Watch 4 (Wear OS 3, `minSdk 30`), circular dial
- Network: `POST` to `https://closing-skink-socially.ngrok-free.app/unlock/`
- Built in GitHub Codespaces (no Android Studio needed)

## Build in Codespaces

1. Push this repo to GitHub, then open it in a **Codespace** (green `Code` button → Codespaces → Create).
2. Wait for the container to finish setup. `.devcontainer/setup.sh` installs the Android SDK and generates the Gradle wrapper automatically.
3. Build the debug APK:

   ```bash
   ./gradlew assembleDebug
   ```

4. The installable APK lands at:

   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

   Right-click it in the VS Code file explorer → **Download** to your computer.

> Codespaces can compile the app but cannot run an emulator GUI or reach your
> physical watch. Building the APK here and installing it locally is the path.

## Install on your Galaxy Watch 4

You need `adb` locally (comes with Android Studio platform-tools, or `brew install android-platform-tools` on macOS).

1. On the watch: **Settings → About watch → Software** → tap **Software version** 7×
   to unlock **Developer options**.
2. **Settings → Developer options** → enable **ADB debugging** and **Debug over Wi-Fi**.
3. Note the IP shown under **Debug over Wi-Fi** (watch and computer on the same Wi-Fi).
4. From your computer:

   ```bash
   adb connect <WATCH_IP>:<PORT>
   adb install -r app-debug.apk
   ```

   Accept the pairing/debug prompt on the watch when it appears.
5. Launch **HelloWatch** from the watch app list. Tap **UNLOCK** → the screen
   flashes green (success) or red (failure).

## Add the Tile (quick access from the watch face)

So you don't have to open the app from the menu each time:

1. From the watch face, **swipe left** to the tiles carousel.
2. Swipe to the end and tap **+** (Add tile).
3. Pick **Unlock Door** from the list.

Now a swipe from the watch face shows the UNLOCK DOOR button. Tapping it fires
the unlock request, flashes green/red x3, and returns to the tile automatically.

## Project layout

```
app/src/main/java/com/example/hellowatch/MainActivity.kt   # the screen + button
app/src/main/AndroidManifest.xml                           # watch app declaration
app/build.gradle.kts                                       # app dependencies
.devcontainer/                                             # Codespaces Android setup
```
