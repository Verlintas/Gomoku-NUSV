package com.gomoku.nusv.data

import com.gomoku.nusv.i18n.I18n
import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.model.GameStatus

data class ScoreResult(
    val scoreGained: Int,
    val coinsGained: Int,
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
    const val WIN_COINS = 50
    const val DRAW_COINS = 10

    /**
     * 结算一局游戏的积分与金币。
     * winner: 本局结果；playerSecondsLeft: 玩家剩余思考时间（秒），用于时间奖励。
     */
    fun settle(profile: PlayerProfile, winner: GameStatus?, playerSecondsLeft: Int?): ScoreResult {
        var gained = 0
        var coins = 0
        val breakdown = mutableListOf<String>()

        return when (winner) {
            GameStatus.BLACK_WIN, GameStatus.WHITE_WIN -> {
                val isPlayerWin = winner == GameStatus.BLACK_WIN
                val newStreak = if (isPlayerWin) profile.winStreak + 1 else 0
                if (isPlayerWin) {
                    gained += WIN_BASE
                    breakdown.add(I18n.t("settle_win", "n" to "$WIN_BASE"))
                    coins += WIN_COINS
                    breakdown.add(I18n.t("settle_coins", "n" to "$WIN_COINS"))
                    if (newStreak > 1) {
                        val streakBonus = minOf(newStreak - 1, WIN_STREAK_MAX) * WIN_STREAK_BONUS
                        gained += streakBonus
                        breakdown.add(I18n.t("settle_streak", "n" to "$newStreak", "b" to "$streakBonus"))
                    }
                    playerSecondsLeft?.let {
                        if (it > 0) {
                            val bonus = it * TIME_BONUS_PER_SECOND
                            gained += bonus
                            breakdown.add(I18n.t("settle_time", "n" to "$bonus"))
                        }
                    }
                    ScoreResult(
                        scoreGained = gained,
                        coinsGained = coins,
                        breakdown = breakdown,
                        winStreak = newStreak,
                        bestWinStreak = maxOf(profile.bestWinStreak, newStreak)
                    )
                } else {
                    ScoreResult(
                        scoreGained = 0,
                        coinsGained = 0,
                        breakdown = emptyList(),
                        winStreak = 0,
                        bestWinStreak = profile.bestWinStreak
                    )
                }
            }

            GameStatus.DRAW -> {
                gained += DRAW_BASE
                breakdown.add(I18n.t("settle_draw", "n" to "$DRAW_BASE"))
                coins += DRAW_COINS
                breakdown.add(I18n.t("settle_coins", "n" to "$DRAW_COINS"))
                ScoreResult(
                    scoreGained = gained,
                    coinsGained = coins,
                    breakdown = breakdown,
                    winStreak = 0,
                    bestWinStreak = profile.bestWinStreak
                )
            }

            else -> ScoreResult(0, 0, emptyList(), profile.winStreak, profile.bestWinStreak)
        }
    }

    fun apply(
        profile: PlayerProfile,
        result: ScoreResult,
        isPlayerWin: Boolean,
        isDraw: Boolean,
        difficulty: Difficulty?
    ): PlayerProfile {
        val winsByDifficulty = profile.winsByDifficulty.toMutableMap()
        if (isPlayerWin && difficulty != null) {
            winsByDifficulty[difficulty.name] = winsByDifficulty[difficulty.name]?.plus(1) ?: 1
        }
        return profile.copy(
            score = profile.score + result.scoreGained,
            coins = profile.coins + result.coinsGained,
            wins = profile.wins + if (isPlayerWin) 1 else 0,
            losses = profile.losses + if (!isPlayerWin && !isDraw) 1 else 0,
            draws = profile.draws + if (isDraw) 1 else 0,
            winStreak = result.winStreak,
            bestWinStreak = result.bestWinStreak,
            gamesPlayed = profile.gamesPlayed + 1,
            winsByDifficulty = winsByDifficulty
        )
    }
}
