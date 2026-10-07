# Just Music 🎵

> *Enjoy your music, enjoy your life.*

An offline Android music player focused on clean design, smooth animations, and zero distractions. No subscriptions, no ads, no cloud logins — just your local library and an interface that feels good to use.

[![Release](https://img.shields.io/github/v/release/astrotyrant30/Just-Music?color=blue&label=Latest%20Release)](https://github.com/astrotyrant30/Just-Music/releases)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-green)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-orange)](LICENSE)

---

## 📥 Download

Grab the latest APK directly from the [Releases](https://github.com/astrotyrant30/Just-Music/releases) page:

👉 **[Download JustMusic-v1.0.apk](https://github.com/astrotyrant30/Just-Music/releases/latest)**

*Requires Android 8.0 (Oreo / API 26) or newer.*

---

## ✨ Why I built this

Most music apps today are bloated with podcasts, social feeds, audio ads, and streaming services pushing algorithms onto you. I just wanted a simple, good-looking player for the audio files already sitting on my phone — something with:

- Instant playback without waiting on servers
- A dark, modern aesthetic with fluid animations
- Fun visual touches like animated waveform seekbars and ambient album lighting
- Complete privacy (your listening habits stay on your device)

---

## 🎧 Features

- **Dynamic Ambient Theme:** The player background smoothly adapts its gradient colors based on the artwork of whatever song is currently playing.
- **Custom Seekbar Styles:** Standard scrubbers get boring. Pick between 4 animated styles in settings:
  - 🌊 *Waveform Bars* — Animated audio amplitude bars that bounce with the groove.
  - 💊 *Smooth Capsule* — Sleek rounded bar with a glowing thumb.
  - ╌ *Dashed Pulse* — Modern segmented dots with active progress pulsing.
  - 〰️ *Fluid Wave* — Clean thin line with dynamic wave motion.
- **Smart Audio Output Detection:** A small badge in the player tells you where your audio is actually going — whether that's your phone's speaker, wired headphones, or your specific Bluetooth device (e.g. AirPods, Sony WH-1000XM, Galaxy Buds).
- **Auto Skip Silence:** Optionally trims dead quiet segments at the beginning and end of tracks automatically.
- **Favorites & Playlists:** Heart songs on the fly or organize them into custom playlists backed by local SQLite storage.
- **Background Playback & Lock Screen:** Runs on Android's native MediaSession service, so you get system notifications, lock screen controls, and hardware button support out of the box.
- **In-App GitHub Updates:** Tap "Check for Updates" in Settings to quickly see if a new APK has been published without having to keep checking GitHub manually.

---

## 🛠️ Tech Stack

Just Music is built from scratch using modern Android standards:

| Layer | Technologies |
|---|---|
| **UI** | 100% Jetpack Compose + Material 3 |
| **Audio Engine** | AndroidX Media3 (ExoPlayer) + MediaSessionService |
| **Local Database** | Room SQLite (Playlists, Favorites) |
| **Media Scanning** | Android MediaStore API |
| **Image Loading** | Coil (Async album artwork loading & palette extraction) |
| **Concurrency** | Kotlin Coroutines & StateFlow |

---

## 🔨 Building from Source

If you want to build the APK yourself or tweak the code:

1. **Clone the repo:**
   ```bash
   git clone https://github.com/astrotyrant30/Just-Music.git
   cd Just-Music
   ```

2. **Open in Android Studio:**
   - Open Android Studio (Ladybug or newer recommended).
   - Let Gradle sync.

3. **Or build from terminal:**
   ```bash
   # On Linux / macOS:
   ./gradlew assembleDebug

   # On Windows:
   .\gradlew.bat assembleDebug
   ```

The compiled APK will be output to:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔒 Permissions Explained

- `READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE`: Needed so the app can scan and load audio files stored on your device.
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: Keeps playback running smoothly when the screen is off or you switch apps.
- `BLUETOOTH_CONNECT`: Detects connected Bluetooth headphone names for the output badge (optional).
- `INTERNET`: Only used if you tap "Check for Updates" in Settings to query the GitHub releases API. The music player itself runs 100% offline.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — feel free to fork it, learn from it, or make it your own!
