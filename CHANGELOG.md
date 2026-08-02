# Changelog

All notable changes to Gomoku-NUSV are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.4.1] - 2026-08-01

### Changed

- **Much stronger AI** — the engine was rebuilt around threat-space search
  (inspired by the NUSV-Lite Gomoku mini app):
  - Forced moves are detected and expanded first: immediate win, blocking an
    immediate opponent win, creating an open four, and intelligently blocking an
    opponent's open four (choosing the point that leaves the fewest remaining
    threats).
  - When a forced move exists, only it is searched — the same budget now reaches
    far deeper lines (VCF-style continuation past the depth limit).
  - Incremental evaluation (delta evaluation on the affected lines) keeps search
    fast on low-end phones.
  - Refined line scoring (open vs. blocked two/three/four gradients).
  - Easy mode also wins/blocks immediate threats before playing greedily.
- Search depths: Medium 3→4, Hard 6→8 on desktop; mobile caps raised to 6 plies
  (was 4) with the existing 3-second budget.

### Notes

- Medium on phones now uses the same threat-space search as Hard, making it a
  reasonable challenge; Hard is substantially stronger.

## [1.4.0] - 2026-08-01

### Added

- **Points economy (engagement)** — every game now awards points (win +100 with
  streak bonuses, draw +30, loss +10) that are spent in the decoration shop.
- **Daily sign-in** — sign in every day for a growing streak bonus (50 base,
  +10 per consecutive day, capped at 10), with streak and total counters.
- **Daily tasks** — three random tasks per day (play a game, win a game, win vs
  AI, use a power-up) with point rewards; progress is tracked and persisted.
- **Decoration shop** — spend points on cosmetic upgrades that do not affect
  gameplay: effect particle colors (golden / amethyst), stone glow (golden /
  azure), and a dual-neon win line. Purchases and equipped choices persist and
  sync via save export/import.
- **Effects master switch** — a single toggle in Settings turns all board
  effects on or off.
- Stats page now shows points, sign-in totals/streak, and completed daily tasks.

### Changed

- Settings theme grid now flows automatically (1-3 columns) on any screen width.
- Home page cards and mode buttons adapt to narrow screens.

## [1.3.2] - 2026-08-01

### Fixed

- **Board invisible on phones (narrow screens)** — the settings panel took all
  available height in the portrait layout, squeezing the board to zero. The board
  now gets up to 55% of the height and the panel is capped at 45% with internal
  scrolling.
- Content now respects the system bars (safe-drawing insets) on Android.

## [1.3.1] - 2026-08-01

### Fixed

- **Save encryption round-trip bug (critical)** — the base64 encoder produced wrong
  characters whenever the payload length was not a multiple of 3, so encrypted
  saves could not be decrypted and profiles were reset. All save data now round-trips
  correctly; tamper checks still work.
- White player's winning line was not highlighted (highlight always checked black).
- Undo could leave the game stuck on the AI's turn when the player played White.
- The move timer was not restarted after Restart, so timeout forfeits stopped
  working after the first game.
- The move timer ran while the "resume game" dialog was shown.
- Tic-Tac-Toe: in two-player mode the board did not refresh after a move, and in
  vs-AI mode an AI win counted as a player win.
- Undo now also clears the hint marker.

## [1.3.0] - 2026-08-01

### Added

- **Multi-page app** — a proper navigation structure instead of a single board
  screen: Home (title card, mode selection, entries), Game, Tic-Tac-Toe minigame,
  Achievements, Stats, Titles, and Settings pages with fade transitions.
- **Save encryption** — local saves are stored obfuscated with a rolling XOR
  cipher and an integrity checksum, so editing the save file by hand no longer
  works (legacy plain-text saves still load).
- **Save export / import** — export copies a versioned JSON (with public checksum)
  to the clipboard; import validates and restores it. Works across macOS and
  Android, enabling cross-device save sync.
- **Power-ups** — during a Gomoku game: a Hint power-up (AI recommends the best
  move, highlighted on the board, once per game) and a +30 s time boost (twice
  per game).
- **Title system** — eight rank titles earned from stats (first win, 3-streak,
  10/25/50 wins, 100 games, 10 Hard wins, Gomoku Master), shown on the Home page
  and in a dedicated Titles page.
- **Four new themes** — Violet Night, Maple Glow, Midnight Galaxy, and Ink Wash
  (10 themes total).
- **Detailed statistics** — total play time, fastest win, longest game, and theme
  usage counts, in addition to win rate, streaks, and per-difficulty wins.
- **Tic-Tac-Toe minigame** — play vs AI or two-player on the same screen, with its
  own win counter.

### Changed

- All sci-fi effects are now permanently enabled (per-effect switches removed).
- The app opens on a Home page instead of directly on the board.

## [1.2.0] - 2026-08-01

### Changed

- **Score and coin systems removed.** All themes and effects are now unlocked from
  the start — full customization with zero grind. The theme and effect shops were
  removed; every board style and sci-fi effect is available directly in the
  settings panel.
- Achievements are now pure collectibles (no coin rewards).
- The statistics dialog no longer shows score or owned-theme counters.

## [Unreleased]

### Added

- **Language switching** — Simplified Chinese and English, switchable in the
  settings panel and persisted per device. All UI strings, settlement breakdowns,
  achievements, effects and theme descriptions are localized.
- **Sci-fi board effects** — four animated effects, unlocked with coins and toggled
  per effect in the settings panel:
  - Energy Ripple: expanding energy rings on every stone placement *(free, enabled
    by default for new players)*
  - Particle Starfield: star-particle bursts on placement and along the winning line
  - Hologram Sweep: a periodic scanning beam sweeping across the board
  - Neon Trail: flowing neon energy along the winning line
- **Coin economy** — wins award 50 coins, draws 10; coins are spent in the theme
  and effect shops.
- **Achievement system** — seven achievements (first win, win counts, win streaks,
  Hard-mode wins, games played, draws) granting coin rewards; unlocked achievements
  are shown in a dedicated dialog and pop up as an animated toast.
- **Statistics dialog** — win rate, streaks, totals, and per-difficulty win bars.
- **Theme shop** — three new purchasable themes (Bamboo, Mint, Rose) alongside the
  three built-in free ones.
- Victory line highlighting on the board (gradient stroke over the winning stones).
- Theme crossfade animation when switching boards.

### Changed

- The low-performance AI notice now follows the active theme (surface colors and
  accent border) and uses a minimal layout.
- Effect coordinates are density-aware, so particle positions stay accurate on
  high-density Android screens.
- Chip groups wrap onto multiple lines when localized text is long (FlowRow), and
  the header compacts on narrow screens.

### Fixed

- AI search no longer mutates the displayed board: the engine now works on a
  private copy, so no temporary stones flash on the board while the AI is thinking.
- `GomokuAI.bestMove` guarantees the input board is left unchanged even when the
  search hits its time budget.

## [1.0.0] - 2026-08-01

First public release.

### Added

- Cross-platform game engine and UI for macOS and Android built with Kotlin
  Multiplatform and Compose Multiplatform.
- Local two-player mode and player-versus-AI mode with black/white side selection.
- Three AI difficulty levels (Easy / Medium / Hard) backed by a self-contained
  minimax engine with alpha-beta pruning, iterative deepening, and deterministic
  move ordering.
- Platform-aware AI budgets: search depth and per-move time limit are capped
  independently for desktop and mobile; when the budget is exceeded the search is
  truncated and a performance notice is displayed.
- Optional per-move countdown timer (10–300 seconds) with timeout forfeit.
- Undo (both sides in AI mode), resign with confirmation, and restart.
- Automatic persistence of unfinished games with a resume dialog on startup.
- Persistent player profile: score, wins, losses, draws, win streak, and best
  streak; scoring includes win base, streak bonus, and time bonus.
- Board sizes 13×13, 15×15, and 19×19 with star points.
- Three color themes (wood, deep space, ocean) with persisted selection.
- Board rendering with gradient stones, last-move marker, drop-in animation, and
  hover preview.
- On-device synthesized sound effects (place, win, draw, timeout).
- Accessibility: the board exposes a semantic click action.
- macOS packaging with a custom application icon injected into the `.app` bundle;
  Android launcher icons (legacy densities and adaptive icon).
- Unit tests covering win detection, AI tactics (win completion, block of a
  four-in-a-row), AI self-play performance, and search benchmarks.

### Notes

- macOS builds target the JVM desktop runtime and do not require Xcode.
- Android builds target API 26 and above.
