package com.gomoku.nusv.data

import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.model.GameStatus

object ScoreService {

    /**
     * 结算一局游戏的统计（胜负平、连胜、局数、难度胜场）。
     * 积分与金币已移除：所有主题与特效零门槛可用。
     */
    fun apply(
        profile: PlayerProfile,
        finalStatus: GameStatus,
        difficulty: Difficulty?
    ): PlayerProfile {
        val isPlayerWin = finalStatus == GameStatus.BLACK_WIN
        val isDraw = finalStatus == GameStatus.DRAW
        val newStreak = if (isPlayerWin) profile.winStreak + 1 else 0
        val winsByDifficulty = profile.winsByDifficulty.toMutableMap()
        if (isPlayerWin && difficulty != null) {
            winsByDifficulty[difficulty.name] = winsByDifficulty[difficulty.name]?.plus(1) ?: 1
        }
        return profile.copy(
            wins = profile.wins + if (isPlayerWin) 1 else 0,
            losses = profile.losses + if (!isPlayerWin && !isDraw) 1 else 0,
            draws = profile.draws + if (isDraw) 1 else 0,
            winStreak = newStreak,
            bestWinStreak = maxOf(profile.bestWinStreak, newStreak),
            gamesPlayed = profile.gamesPlayed + 1,
            winsByDifficulty = winsByDifficulty
        )
    }
}
