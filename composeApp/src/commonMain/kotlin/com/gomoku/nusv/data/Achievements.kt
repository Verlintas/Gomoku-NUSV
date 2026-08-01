package com.gomoku.nusv.data

import com.gomoku.nusv.model.Difficulty

data class Achievement(
    val id: String,
    val nameKey: String,
    val descKey: String,
    val coinReward: Int
)

object Achievements {

    val all: List<Achievement> = listOf(
        Achievement("first_win", "ach_first_win", "ach_first_win_desc", 50),
        Achievement("wins_10", "ach_wins_10", "ach_wins_10_desc", 100),
        Achievement("streak_5", "ach_streak_5", "ach_streak_5_desc", 150),
        Achievement("hard_win", "ach_hard_win", "ach_hard_win_desc", 150),
        Achievement("games_50", "ach_games_50", "ach_games_50_desc", 200),
        Achievement("draws_3", "ach_draws_3", "ach_draws_3_desc", 100),
        Achievement("ai_first_win", "ach_ai_first_win", "ach_ai_first_win_desc", 50)
    )

    fun byId(id: String): Achievement? = all.find { it.id == id }

    /**
     * 根据更新后的档案与本次对局信息，返回本次新解锁的成就。
     * @param profile 已应用对局结果的档案
     * @param wonAgainstAi 本局是否为人机模式且玩家获胜
     * @param difficulty 本局难度（人机模式）
     */
    fun newlyUnlocked(profile: PlayerProfile, wonAgainstAi: Boolean, difficulty: Difficulty?): List<Achievement> {
        val unlocked = profile.achievements.toSet()
        val result = mutableListOf<Achievement>()

        fun unlockIf(id: String, condition: () -> Boolean) {
            if (condition() && id !in unlocked) byId(id)?.let(result::add)
        }

        unlockIf("first_win") { profile.wins >= 1 }
        unlockIf("wins_10") { profile.wins >= 10 }
        unlockIf("streak_5") { profile.bestWinStreak >= 5 }
        unlockIf("hard_win") { difficulty == Difficulty.HARD && wonAgainstAi }
        unlockIf("games_50") { profile.gamesPlayed >= 50 }
        unlockIf("draws_3") { profile.draws >= 3 }
        unlockIf("ai_first_win") { wonAgainstAi }
        return result
    }
}
