# Gomoku-NUSV

A cross-platform Gomoku (Five in a Row) game for **macOS, Windows, iOS and
Android**, built with
[Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
and [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/).

Play against a local friend on the same screen, or challenge the built-in AI across
three difficulty levels. The AI is a self-contained on-device engine with no network
dependencies.

## Features

- **Two game modes** — local two-player on one screen, or player vs. AI.
- **Three AI difficulty levels** — Easy (heuristic with threat responses),
  Medium (threat-space search, 4 plies), Hard (threat-space search, up to 8 plies
  on desktop).
- **Platform-aware AI budget** — the search depth and per-move time limit adapt to
  the device. Desktop uses up to 8 plies with a 5-second budget; Android uses up to
  6 plies with a 3-second budget. If the budget is exceeded, the search is
  gracefully truncated and a notice is shown.
- **Move timer** — optional per-move countdown (10–300 seconds). Running out of time
  forfeits the game.
- **Undo / Resign / Restart** — undo removes both sides' last moves in AI mode.
- **Game persistence** — an unfinished game is saved automatically and a dialog
  offers to resume it on the next launch.
- **Statistics** — persistent profile tracking wins, losses, draws, win streak,
  and per-difficulty wins.
- **Board sizes** — 13×13, 15×15, or 19×19.
- **Color themes** — ten fully unlocked themes (wood, deep space, ocean, bamboo,
  mint, rose, violet, maple, midnight, and ink) with the selection persisted.
- **Polished board rendering** — gradient stones with highlights and shadows, drop-in
  animation for the last move, last-move marker, and hover preview.
- **Sound effects** — synthesized on-device (no audio assets): place, win, draw, and
  timeout sounds.
- **Sci-fi effects** — all four visual effects are permanently enabled: energy
  ripples on moves, particle bursts, a hologram scan beam, and a neon winning-line
  trail.
- **Power-ups (stock-based)** — an AI hint (recommended move highlighted on the
  board) and +30 s time boosts are consumed from your stock; new players start with
  3 hints and 5 boosts, and more can be purchased with points (1 / 5 / 10 packs).
- **Points economy** — every game awards points (win +100 with streak bonus,
  draw +30, loss +10) spent in the decoration shop and on power-ups.
- **Daily sign-in** — sign in daily for a growing streak bonus.
- **Daily tasks** — three random tasks per day (play, win, win vs AI, use a
  power-up) with point rewards.
- **Shop** — spend points on cosmetic upgrades (golden or amethyst effect
  colors, golden or azure stone glow, a dual-neon win line) and on power-up
  packs (hints 100 pts, time boosts 80 pts, packs of 1 / 5 / 10).
- **Titles** — eight rank titles earned from your stats, displayed on the Home page.
- **Minigame** — a Tic-Tac-Toe mode (vs AI or two-player) with its own win counter.
- **Multi-page UI** — Home, Game, Minigame, Achievements, Stats, Titles, and
  Settings pages with smooth transitions.
- **Accessibility** — the board exposes a semantic click action for screen readers
  and automated UI testing.

## AI Engine

The AI is implemented in pure Kotlin in `commonMain` and runs on both platforms
without any server or network access.

- **Threat-space search** — forced moves are detected and expanded first: an
  immediate win, blocking an opponent's immediate win, creating an open four, and
  intelligently blocking an opponent's open four (picking the point that leaves the
  fewest remaining threats).
- **Threat pruning** — when a forced move exists, only it is searched, so the same
  time budget reaches far deeper lines (VCF-style continuation past the depth limit).
- **Incremental evaluation** — only the lines affected by a move are re-scored,
  keeping search fast on low-end phones.
- **Refined line scoring** — open vs. blocked gradients for twos, threes, and fours.
- Search: minimax with alpha-beta pruning; deterministic candidate ordering.

## Platform Support

| Platform | Min version | Notes |
|---|---|---|
| macOS | 11 (Big Sur)+ | Distributed as a `.dmg` |
| Windows | 10 (64-bit)+ | Distributed as an `.msi` (built on Windows) |
| Linux | Ubuntu 20.04+ (x64) | Distributed as a `.deb` (built on Linux) |
| iOS | 14+ | Built with Xcode (`iosApp`) |
| Android | API 26 (Android 8.0)+ | Distributed as an `.apk` |

All targets share one codebase. The desktop targets (macOS and Windows) run on the
JVM, so no Xcode is required; Windows installers are produced with `jpackage` and
must be built on a Windows machine.

### Installing on your iPhone

iOS apps are normally distributed only through the App Store. Without a paid
developer account you can still install this app on your own iPhone using a free
Apple ID — see the full guide: **[docs/ios-installation.md](docs/ios-installation.md)**.

Quick options:

1. **Xcode free signing** — open `iosApp/iosApp.xcodeproj`, enable *Automatically
   manage signing* with your free Personal Team, connect the iPhone, Cmd+R
   (renews every 7 days by re-running).
2. **AltStore sideload** — install AltStore via AltServer, import the unsigned
   `.ipa` from the Releases page; auto-renews while your computer's AltServer is
   online.
3. **爱思助手** — the unsigned `.ipa` can be auto-signed and installed via i4.cn.

The unsigned `Gomoku-NUSV-1.4.6-compatibility.ipa` is attached to the Releases.

> Note: the iOS build targets arm64 devices and the arm64 simulator; the Intel
> (x86_64) simulator is not supported by the current Compose toolchain.

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

# Build the Windows .msi installer (run on Windows)
./gradlew :composeApp:packageMsi

# Build the iOS app (macOS with Xcode)
xcodebuild -project iosApp/iosApp.xcodeproj -target iosApp \
  -sdk iphonesimulator -configuration Debug ARCHS=arm64 ONLY_ACTIVE_ARCH=YES build
```

### Windows installer via GitHub Actions

A Windows machine is required to produce the `.msi` (jpackage cannot cross-build
Windows installers from macOS). This repository ships a ready-made builder:

- Workflow: `.github/workflows/windows-build.yml` (Windows Installer Builder)
- Trigger it from the **Actions** tab (Run workflow) — or push a `v*` tag, and
  the produced `.msi` is attached to that tag's GitHub Release automatically.
- The workflow installs JDK 17, the Android SDK (required at configuration
  time), builds the MSI and uploads it as a build artifact / release asset.
- Note: the Windows build is unsigned, so SmartScreen may warn on first run
  (More info → Run anyway).
- The same pattern applies to Linux: `.github/workflows/linux-build.yml`
  (Linux Installer Builder) produces the `.deb` on an Ubuntu runner and attaches
  it to the release.

Artifacts are produced under:

- `composeApp/build/outputs/apk/debug/` — Android APK
- `composeApp/build/compose/binaries/main/dmg/` — macOS disk image
- `composeApp/build/compose/binaries/main/msi/` — Windows installer (built on Windows)
- `composeApp/build/compose/binaries/main/app/` — unpackaged macOS `.app`

The macOS icon is injected into the `.app` bundle by a dedicated Gradle task
(`applyAppIcon`) which also registers the `CFBundleIconFile` key in `Info.plist`.
`packageApp` and `packageDmg` depend on it automatically. The Windows `.msi` uses
the default Compose icon (custom `.ico` requires building on Windows).

## Project Structure

```
composeApp/
├── src/
│   ├── commonMain/kotlin/com/gomoku/nusv/
│   │   ├── model/      # Board, stones, game configuration
│   │   ├── logic/      # WinChecker (five-in-a-row detection), GomokuAI engine
│   │   ├── data/       # ProfileStore (persistence), scoring, sign-in, tasks, power-ups
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

- player profile (stats, streaks, points, unlocked achievements, power-up stock,
  daily sign-in and task progress, purchased decorations) — stored with light
  obfuscation and an integrity checksum to prevent manual save-file tampering
- selected theme and equipped decorations
- game configuration (mode, difficulty, board size, timer, language)
- the most recent unfinished game, restored on demand
- legacy keys from older versions are cleaned up automatically after migration,
  so no stale version data is ever read

### Save export / import

The Settings page can export the save as a versioned JSON document to the
clipboard or paste it into the import text box (checksum-verified). This works on
both macOS and Android, so a save can be moved between devices.

> **Upgrade note: updates reset the save (by design).** Every app version starts
> fresh — updating the app clears local progress. To keep your progress:
> 1. Before updating: Settings → Save backup → Export, and keep the text.
> 2. After updating: Settings → Save backup → paste the text into the import box
>    (or copy it and press Import) to restore.
>
> See the [CHANGELOG](CHANGELOG.md) for per-version upgrade notes.

## HarmonyOS NEXT

A porting guide for pure HarmonyOS (ArkUI shell + shared Kotlin logic) is
available at **[docs/harmonyos-porting.md](docs/harmonyos-porting.md)**.

## License

Released under the [MIT License](LICENSE).
