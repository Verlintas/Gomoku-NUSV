# Changelog

All notable changes to Gomoku-NUSV are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 升级须知（Upgrade Notes）

> **更新会重置本地存档（设计如此）。**
> 每个版本号是一次全新开始：安装新版本后，旧存档不会被保留，游戏会以新玩家状态启动。
> 想延续存档进度，请按以下步骤操作：
> 1. **更新前**：打开 设置 → 存档备份 → 导出存档，保存导出的文本。
> 2. **更新后**：打开 设置 → 存档备份 → 将文本粘贴到输入框（或复制后点"导入存档"），
>    校验通过即恢复。
>
> 历史已知问题：v1.3.0 曾存在存档加密往返缺陷（v1.3.1 已修复）。

## [1.4.6] - 2026-08-01

### Fixed

- **Bogus timeout forfeit on an empty board** — the move timer ran everywhere,
  so staying on the Home screen (or starting a game without moving) for longer
  than the per-move limit silently forfeited the game (e.g. a brand-new install
  suddenly showing 1 loss). The timeout countdown now starts only after the first
  stone is placed.
- Removed the leftover hardcoded Chinese accessibility label on the board canvas.

### Added

- **Reset Save button in Settings** with a two-step confirmation dialog:
  first confirmation, then a second "cannot be undone" confirmation. It restores
  the game to a fresh state (stats, points, power-ups, sign-in, tasks).

### 升级须知（重要）

- **本次更新会重置存档**：升级到 1.4.6 后本地存档清空（新玩家状态）。
- 想保留进度：更新前 设置 → 存档备份 → 导出存档；更新后导入恢复。

## [1.4.5] - 2026-08-01

### Changed

- The shop is renamed to **积分商店 / Shop** (it sells both decorations and
  power-ups, so "Decoration Shop" no longer fit).

### Fixed

- **Daily-task rewards were awarded twice** — `onEvent` already adds the reward
  to the score, and callers added it again, doubling every task reward. Fixed in
  game end and both power-up paths.
- **Power-up stock was not persisted** — consuming a hint/time boost updated the
  in-memory stock but never saved it; a restart restored the old stock. Both
  power-up uses now save immediately.
- **Effects master switch was not persisted** — the toggle now survives restarts.
- The shop no longer shows a decoration as "in use" when the imported save
  references an unowned item.
- Removed dead per-effect toggle code.

### 升级须知（重要）

- **本次更新会重置存档**：升级到 1.4.5 后本地存档清空（新玩家状态）。
- 想保留进度：更新前 设置 → 存档备份 → 导出存档；更新后导入恢复。

## [1.4.4] - 2026-08-01

### Changed

- **Updates reset the save (intended)** — each app version starts fresh. When a
  save from another version is detected, it is reset to a new-player state and
  old unfinished games are cleared. To keep progress, export the save before
  updating and import it afterwards.
- Saves now record the app version they belong to; imported saves are tagged with
  the current version so they are never reset again.
- Legacy plain-text profile keys from 1.3.x are cleaned up after loading.

### 升级须知（重要）

- **本次更新会重置存档**：升级到 1.4.4 后本地存档将清空（新玩家状态）。
- 想保留进度：更新前先在 设置 → 存档备份 → 导出存档；更新后导入即可恢复。
- 从 1.4.3 升级的用户请注意提前备份。

## [1.4.3] - 2026-08-01

### Added

- **Import via text box** — the Settings page now has a paste field for save
  data; import works by pasting the exported JSON into the field (clipboard
  import remains available as a shortcut).
- **Purchasable power-ups** — hints and time boosts are now stock-based:
  - New players start with 3 hints and 5 time boosts (migration included).
  - Each use consumes one from the stock; the in-game buttons show remaining
    stock and disable when empty.
  - The shop sells hints (100 pts each) and time boosts (80 pts each) in packs
    of 1 / 5 / 10, giving points a direct gameplay use.

### 升级须知

- **更新会重置存档**：每个新版本号都会以新玩家状态开始，旧存档不保留。
- 想延续进度：更新前 设置 → 存档备份 → 导出存档，更新后导入。

## [1.4.2] - 2026-08-01

### Fixed

- Result dialog congratulated the wrong side in two-player mode (it always showed
  the opponent of the last mover). It now names the actual winner.
- The low-performance notice auto-dismissed too slowly (4 s); reduced to 2.5 s.
- The Achievements and Stats buttons in the in-game settings panel did nothing
  (leftover dialog wiring). They now navigate to the dedicated pages.
- Removed the obsolete in-game stats dialog and its unused controller state.

### Changed

- In-app version now reads from a single constant (`APP_VERSION`), shown as
  Gomoku-NUSV 1.4.2 in Settings.

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
