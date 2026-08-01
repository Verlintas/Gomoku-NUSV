# Gomoku-NUSV

A cross-platform Gomoku (Five in a Row) game for **macOS and Android**, built with
[Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) and
[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/).

Play against a local friend on the same screen, or challenge the built-in AI across
three difficulty levels. The AI is a self-contained on-device engine with no network
dependencies.

## Features

- **Two game modes** — local two-player on one screen, or player vs. AI.
- **Three AI difficulty levels** — Easy (heuristic with randomness), Medium (3-ply
  search), Hard (6-ply search with iterative deepening).
- **Platform-aware AI budget** — the search depth and per-move time limit adapt to
  the device. Desktop uses up to 6-ply with a 5-second budget; Android uses up to
  4-ply with a 3-second budget. If the budget is exceeded, the search is gracefully
  truncated and a notice is shown.
- **Move timer** — optional per-move countdown (10–300 seconds). Running out of time
  forfeits the game.
- **Undo / Resign / Restart** — undo removes both sides' last moves in AI mode.
- **Game persistence** — an unfinished game is saved automatically and a dialog
  offers to resume it on the next launch.
- **Score and statistics** — persistent profile tracking score, wins, losses, draws,
  and win streak. Score awards include win base, streak bonus, and time bonus.
- **Board sizes** — 13×13, 15×15, or 19×19.
- **Color themes** — wood, deep space, and ocean themes, with the selection persisted.
- **Polished board rendering** — gradient stones with highlights and shadows, drop-in
  animation for the last move, last-move marker, and hover preview.
- **Sound effects** — synthesized on-device (no audio assets): place, win, draw, and
  timeout sounds.
- **Accessibility** — the board exposes a semantic click action for screen readers
  and automated UI testing.

## AI Engine

The AI is implemented in pure Kotlin in `commonMain` and runs on both platforms
without any server or network access.

- Position evaluation: four-direction line scoring covering open/broken twos, threes,
  and fours, plus threat aggregation (double-three / four-three patterns).
- Search: minimax with alpha-beta pruning and iterative deepening. Move ordering uses
  a full-board evaluation, so winning moves are always expanded first.
- Candidate generation: only empty cells within a radius of the existing stones are
  considered, keeping the branching factor small on low-end devices.
- Deterministic ordering of candidates avoids randomness between identical states.

## Platform Support

| Platform | Min version | Notes |
|---|---|---|
| macOS | 11 (Big Sur)+ | Distributed as a `.dmg` |
| Android | API 26 (Android 8.0)+ | Distributed as an `.apk` |

Android is built with the classic Android application plugin; the desktop target runs
on the JVM, so no Xcode is required to build the macOS version.

### Opening the macOS app

The macOS build is signed with an ad-hoc signature but is **not notarized** by Apple,
so Gatekeeper may show a warning the first time you open it:

- If the system reports that the app **cannot be verified** (or is blocked), choose
  **Control-click (right-click) → Open** on the `.app` (or on the extracted app from
  the `.dmg`) and confirm, or
- Remove the downloaded-from-internet quarantine attribute once:

  ```bash
  xattr -dr com.apple.quarantine /path/to/Gomoku-NUSV.app
  ```

After that the app opens normally. The ad-hoc signature is verified with
`codesign --verify --deep --strict` after every packaging run.

## Building from Source

### Prerequisites

- JDK 17 or later
- Android SDK (platform 36 and build-tools 36) — only needed for the Android target
- No full Xcode installation is required (the desktop target is JVM-based)

### Commands

```bash
# Run the desktop app
./gradlew :composeApp:run

# Run all unit tests (common logic + AI engine)
./gradlew :composeApp:desktopTest

# Build the Android debug APK
./gradlew :composeApp:assembleDebug

# Build the macOS .dmg installer
./gradlew :composeApp:packageDmg
```

Artifacts are produced under:

- `composeApp/build/outputs/apk/debug/` — Android APK
- `composeApp/build/compose/binaries/main/dmg/` — macOS disk image
- `composeApp/build/compose/binaries/main/app/` — unpackaged macOS `.app`

The macOS icon is injected into the `.app` bundle by a dedicated Gradle task
(`applyAppIcon`) which also registers the `CFBundleIconFile` key in `Info.plist`.
`packageApp` and `packageDmg` depend on it automatically.

## Project Structure

```
composeApp/
├── src/
│   ├── commonMain/kotlin/com/gomoku/nusv/
│   │   ├── model/      # Board, stones, game configuration
│   │   ├── logic/      # WinChecker (five-in-a-row detection), GomokuAI engine
│   │   ├── data/       # ProfileStore (persistence), ScoreService (scoring rules)
│   │   ├── sound/      # Cross-platform synthesized sound player
│   │   └── ui/         # GameController, board rendering, screens and dialogs
│   ├── androidMain/    # MainActivity, manifest, launcher icons, platform AI budget
│   ├── desktopMain/    # Desktop entry point, packaging icon, platform AI budget
│   └── commonTest/     # Unit tests: win detection, AI tactics, self-play, benchmarks
└── build.gradle.kts    # KMP + Android + Compose Desktop configuration
```

Platform-dependent AI limits (search depth and time budget) are declared as
`expect`/`actual` functions in `Platform.kt`, so tuning the per-device performance
profile is a one-file change per target.

## Persistence

User data is stored via the multiplatform-settings library (SharedPreferences on
Android, Preferences on the desktop JVM):

- player profile (score, stats, streaks)
- selected theme
- game configuration (mode, difficulty, board size, timer)
- the most recent unfinished game, restored on demand

## License

Released under the [MIT License](LICENSE).
