# 🧩 Tile Match

<p align="center">
  <a href="https://f-droid.org/packages/infrared.tilematch/">
    <img src="metadata/en-US/images/icon.png" width="120" alt="Tile Match icon">
  </a>
</p>

**Tile Match** is a free & open-source tile-collecting puzzle game for Android. Pick tiles from the board and match 3 of the same color in the bar below — but don't let the bar fill up!

[![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=android&logoColor=white)](https://developer.android.com/compose)
[![F-Droid](https://img.shields.io/badge/Available%20on-F--Droid-1976D2)](https://f-droid.org/packages/infrared.tilematch/)
[![License](https://img.shields.io/badge/Code-GPLv3-blue.svg)](LICENSE.md)
[![License](https://img.shields.io/badge/Assets-CC0%201.0-green.svg)](LICENSE.md)

---

## 📱 Screenshots

| Menu | Gameplay |
|:---:|:---:|
| ![Menu](metadata/en-US/images/phoneScreenshots/1.png) | ![Game](metadata/en-US/images/phoneScreenshots/2.png) |

## 🎮 How to Play

1. **Tap a tile** on the board — it moves to the collection bar at the bottom
2. **Match 3 tiles** of the same color in the bar to clear them
3. **Don't fill the bar!** It only has 7 slots — if all slots are occupied with no match, it's game over
4. **Clear all tiles** from the board to complete the level
5. **Think strategically** — the order you pick tiles matters!

## ✨ Features

- 🧠 Endlessly generated puzzle boards (no two levels alike)
- 🔊 Original sound effects (all CC0, bundled in the app)
- 📴 **100% offline** — no ads, no trackers, no permissions
- 🐦 Small, lightweight APK
- 🛠 Built with modern Android tooling

## 🛠 Tech Stack

| | |
|---|---|
| **Language** | Kotlin |
| **UI** | Jetpack Compose |
| **Min SDK / Target SDK** | 24 / 35 |
| **Persistence** | Local progress & settings managers |
| **Tracking** | None — fully offline, FOSS release |

## 📦 Installation

### Via F-Droid (Recommended)

1. Install [F-Droid](https://f-droid.org) on your Android device
2. Search for **"Tile Match"**
3. Tap **Install**

Or use the direct link: [Tile Match on F-Droid](https://f-droid.org/packages/infrared.tilematch/)

### Build from source

```bash
git clone https://github.com/awaisdev5765/tile-match.git
cd tile-match
./gradlew assembleRelease
```

Requires **Android Studio** with the Android SDK 35+.

## 📂 Project Structure

```
app/src/main/java/infrared/tilematch/
├── gamelogic/       # Map generator & game-state coder
├── data/            # Game state model
├── managers/        # Progress, settings & sound managers
├── ui/              # Compose screens & components
├── utils/           # Helpers
└── constants/       # Game constants
```

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a pull request

## ☕ Support

If you enjoy the game, please consider supporting my work:

<p align="center">
  <a href="https://buymeacoffee.com/awaisdev572">
    <img src="https://img.buymeacoffee.com/button-api/?text=Buy%20me%20a%20coffee&emoji=%E2%98%95&slug=awaisdev572&button_colour=FFDD00&font_colour=000000&font_family=Lato&outline_colour=000000&coffee_colour=ffffff" alt="Buy Me A Coffee">
  </a>
</p>

Or [become a supporter on Buy Me a Coffee](https://buymeacoffee.com/awaisdev572) 💛

## 📄 License

This project uses multiple licenses for different components — see [LICENSE.md](LICENSE.md) for details:

- **Source code:** [GNU GPLv3](LICENSE.md)
- **Assets (art & sounds):** [CC0 1.0 Universal](LICENSE.md) (Public Domain)

## 📬 Contact

Having issues or questions? awaisdev5765@gmail.com
