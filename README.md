# Just Music 🎵

Hey there! 👋 Welcome to **Just Music**!

I built this project because I was tired of all the bloated, ad-filled, subscription-heavy music players out there. Sometimes you just want to listen to the MP3s and FLAC files you actually own, without needing an internet connection, and you want it to look *good* while doing it.

So, I made an aesthetic, offline-first audio player that focuses entirely on your local media and gives it a premium UI treatment.

## What makes it cool? ✨

*   **100% Offline & Private:** No streaming, no tracking. It scans your device for local audio files and plays them instantly.
*   **Aesthetic UI:** Designed with pure Jetpack Compose. When you play a song, the app dynamically extracts the colors from your album art and renders a gorgeous full-screen gradient.
*   **Custom Audio Visualizers:** I didn't just stop at a boring slider. You can choose between 4 different seek bar styles:
    *   *Waveform Bars* (animated amplitude visualizers)
    *   *Fluid Wave* (a clean, moving sine wave)
    *   *Segmented Dashed Pulse*
    *   *Smooth Capsule*
*   **Smart "Skip Silence":** Powered by ExoPlayer, you can toggle a feature that automatically skips dead air at the beginning or end of your tracks.
*   **Weekly Top Tracks & History:** The app learns what you listen to and automatically curates a "Top Listened This Week" section right on your home screen.

## Tech Stack 🛠

*   **Kotlin & Jetpack Compose:** 100% modern declarative UI.
*   **Jetpack Media3 (ExoPlayer):** The absolute gold standard for robust media playback on Android, complete with `MediaSessionService` for background playback and lock-screen controls.
*   **Room Database:** Handles your custom playlists, playback history, and liked songs locally.
*   **Palette API:** Extracts those sweet vibrant and muted colors from your album artwork.

## Installation 🚀

You can grab the latest APK from the [Releases](#) tab, or clone the repo and build it yourself via Android Studio:

```bash
git clone https://github.com/yourusername/JustMusic.git
```
Then just open the folder in Android Studio and hit `Run`.

## Updates

I've built an auto-update checker right into the app's settings menu! It pings the GitHub Releases API to let you know if I've dropped a new version.

---
Built with ❤️ for pure music lovers.
