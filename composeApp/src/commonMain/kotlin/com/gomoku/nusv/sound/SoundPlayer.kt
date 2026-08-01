package com.gomoku.nusv.sound

enum class SoundType { PLACE, WIN, DRAW, TIMEOUT }

expect class SoundPlayer() {
    fun play(type: SoundType)
    fun dispose()
}
