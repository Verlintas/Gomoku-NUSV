# Changelog

All notable changes to Gomoku-NUSV are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
