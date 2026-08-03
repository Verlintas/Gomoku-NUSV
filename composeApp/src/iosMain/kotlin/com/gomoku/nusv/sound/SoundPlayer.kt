package com.gomoku.nusv.sound

/**
 * iOS 音效：AudioToolbox 系统音效集成留待后续版本，
 * 当前静音（不影响其他功能）。
 */
actual class SoundPlayer actual constructor() {
    actual fun play(type: SoundType) {}
    actual fun dispose() {}
}
