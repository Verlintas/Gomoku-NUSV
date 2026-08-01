# Changelog

All notable changes to Gomoku-NUSV are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
