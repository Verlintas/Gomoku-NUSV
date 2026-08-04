# HarmonyOS NEXT Porting Guide

This project can be ported to **HarmonyOS NEXT (pure HarmonyOS, API 12+)**.
The approach is the officially supported one: **share Kotlin business logic with
KMP and build the UI with ArkUI (ArkTS)**. Compose Multiplatform has no
HarmonyOS target, so the UI layer is re-implemented in ArkUI while all game
logic, AI, persistence and economy code is reused as-is.

## What can be reused (zero changes)

The following code is pure Kotlin with **no Compose / no platform dependencies**
and can be compiled into a HarmonyOS library (`.har`) via the Kotlin
Multiplatform → HarmonyOS toolchain:

| Module | Contents |
|---|---|
| `model/` | `Board`, stones, game configuration |
| `logic/` | `WinChecker`, `GomokuAI` (threat-space search) |
| `data/` | `SaveCrypto`, `ScoreService`, `DailySystem` (points / sign-in / tasks / decorations), `Powerups` (stock & shop logic), `Achievements`, `Titles`, `ProfileStore` (storage-abstracted, version-reset semantics) |
| `i18n/Translations` | All zh/en translation data + pure `t()` lookups (already decoupled from Compose state) |
| `net/` | `LanProtocol` (message/JSON protocol, pure Kotlin) |

Dependencies used by the shared code (`kotlinx.serialization`, `kotlin.random`,
`kotlin.time`, coroutines) all have HarmonyOS support through the KMP toolchain.

## What must be re-implemented (UI layer)

The entire `ui/` directory uses Compose Multiplatform and cannot run on
HarmonyOS. Re-implement it in ArkUI (ArkTS):

- **Pages**: Home, Game (board + controls), LAN hub + lobby, Tic-Tac-Toe,
  Achievements, Stats, Titles, Shop, Settings
- **Board rendering**: Canvas with grid, stones, effects (particles, ripple,
  hologram sweep, neon win line)
- **State**: the `GameController` logic can be ported directly (it is plain
  Kotlin + coroutines), wiring ArkUI state to it

## Platform adapters needed (expect/actual)

| expect | HarmonyOS actual |
|---|---|
| `createSettings()` | HarmonyOS Preferences (via ArkTS interop or the KMP HarmonyOS preferences binding) |
| `todayStr()` | Date formatting through ArkTS interop |
| `platformMaxAiDepth()` / `platformAiTimeLimitMs()` | `6` / `3000` (same as Android) |
| `SoundPlayer` | AudioKit / AudioRenderer (or no-op initially) |
| `lanHost()` / `lanClient()` / `LanDiscovery` | HarmonyOS TCP/UDP (currently iOS marks these unsupported; the same stubs can be used initially) |

**Save versioning**: the app stores a save with an `appVersion` tag; when the
version differs, the save is reset to a new-player state (by design, to avoid
mixed-version data). The HarmonyOS port must keep this semantics — export before
updating, import after.

## Suggested repository layout

```
shared/                 # new KMP module (targets: android, jvm, ios, harmonyos)
  model/ logic/ data/ i18n/
harmonyos-app/          # DevEco Studio project (ArkTS UI)
  entry/src/main/ets/   # ArkUI pages calling shared logic
```

## Required tooling

- **DevEco Studio 5.x** (HarmonyOS IDE, macOS version available) — download
  from the Huawei developer site; includes the HarmonyOS SDK and the KMP
  support.
- A **HarmonyOS NEXT device** (API 12+) for on-device debugging, or the
  HarmonyOS emulator.
- A free Huawei developer account for device deployment.

## Milestones

1. Extract `shared/` module from `composeApp` (pure Kotlin; verified to compile
   for android/jvm/ios targets).
2. Configure the KMP → HarmonyOS target in DevEco Studio and produce the `.har`.
3. Write the ArkUI shell: navigation + Home page + Game page with Canvas board.
4. Wire `GameController` + AI + persistence adapters (settings/date/sound).
5. Port the remaining pages (Stats / Titles / Achievements / Shop / Settings /
   Tic-Tac-Toe), then the daily sign-in & task screens.
6. LAN battle: reuse `LanProtocol`; implement TCP/UDP adapters or start with
   the "unsupported" stubs like iOS.

> Note: this guide assumes the official KMP-for-HarmonyOS support in DevEco
> Studio 5.x. If Huawei's KMP toolchain is unavailable in your SDK version,
> the shared logic can alternatively be exposed via C ABI / OHOS FFI.
