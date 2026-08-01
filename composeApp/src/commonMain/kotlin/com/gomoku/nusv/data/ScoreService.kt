package com.gomoku.nusv.data

import com.gomoku.nusv.model.GameStatus

data class ScoreResult(
    val scoreGained: Int,
    val breakdown: List<String>,
    val winStreak: Int,
    val bestWinStreak: Int
)

object ScoreService {

    const val WIN_BASE = 100
    const val WIN_STREAK_BONUS = 20
    const val WIN_STREAK_MAX = 5
    const val DRAW_BASE = 10
    const val TIME_BONUS_PER_SECOND = 1

    /**
     * 结算一局游戏的积分。
     * winner: 玩家（黑棋视角）是否获胜；null 表示平局。
     * playerSecondsLeft: 玩家剩余思考时间（秒），用于时间奖励。
     */
    fun settle(profile: PlayerProfile, winner: GameStatus?, playerSecondsLeft: Int?): ScoreResult {
        var gained = 0
        val breakdown = mutableListOf<String>()

        return when (winner) {
            GameStatus.BLACK_WIN, GameStatus.WHITE_WIN -> {
                val isPlayerWin = winner == GameStatus.BLACK_WIN
                val newStreak = if (isPlayerWin) profile.winStreak + 1 else 0
                if (isPlayerWin) {
                    gained += WIN_BASE
                    breakdown.add("获胜 +${WIN_BASE}")
                    if (newStreak > 1) {
                        val streakBonus = minOf(newStreak - 1, WIN_STREAK_MAX) * WIN_STREAK_BONUS
                        gained += streakBonus
                        breakdown.add("连胜x$newStreak +$streakBonus")
                    }
                    playerSecondsLeft?.let {
                        if (it > 0) {
                            val bonus = it * TIME_BONUS_PER_SECOND
                            gained += bonus
                            breakdown.add("时间奖励 +$bonus")
                        }
                    }
                    ScoreResult(
                        scoreGained = gained,
                        breakdown = breakdown,
                        winStreak = newStreak,
                        bestWinStreak = maxOf(profile.bestWinStreak, newStreak)
                    )
                } else {
                    ScoreResult(
                        scoreGained = 0,
                        breakdown = emptyList(),
                        winStreak = 0,
                        bestWinStreak = profile.bestWinStreak
                    )
                }
            }

            GameStatus.DRAW -> {
                gained += DRAW_BASE
                breakdown.add("平局 +${DRAW_BASE}")
                ScoreResult(
                    scoreGained = gained,
                    breakdown = breakdown,
                    winStreak = 0,
                    bestWinStreak = profile.bestWinStreak
                )
            }

            else -> ScoreResult(0, emptyList(), profile.winStreak, profile.bestWinStreak)
        }
    }

    fun apply(profile: PlayerProfile, result: ScoreResult, isPlayerWin: Boolean, isDraw: Boolean): PlayerProfile =
        profile.copy(
            score = profile.score + result.scoreGained,
            wins = profile.wins + if (isPlayerWin) 1 else 0,
            losses = profile.losses + if (!isPlayerWin && !isDraw) 1 else 0,
            draws = profile.draws + if (isDraw) 1 else 0,
            winStreak = result.winStreak,
            bestWinStreak = result.bestWinStreak,
            gamesPlayed = profile.gamesPlayed + 1
        )
}
