package com.gomoku.nusv.model

enum class GameMode(val displayName: String) {
    VS_AI("人机对战"),
    PVP("双人对战")
}

enum class Difficulty(val displayName: String, val searchDepth: Int) {
    EASY("简单", 1),
    MEDIUM("中等", 3),
    HARD("困难", 6)
}

enum class GameStatus {
    PLAYING,
    BLACK_WIN,
    WHITE_WIN,
    DRAW;

    val isOver: Boolean get() = this != PLAYING
}

data class GameConfig(
    val boardSize: Int = 15,
    val mode: GameMode = GameMode.VS_AI,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val timerEnabled: Boolean = true,
    val secondsPerMove: Int = 60
) {
    val validBoardSize: Int get() = boardSize.coerceIn(BOARD_SIZES.first(), BOARD_SIZES.last())

    companion object {
        val BOARD_SIZES = listOf(13, 15, 19)
    }
}
