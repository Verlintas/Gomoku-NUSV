package com.gomoku.nusv

import com.gomoku.nusv.data.Achievements
import com.gomoku.nusv.data.PlayerProfile
import com.gomoku.nusv.data.ScoreService
import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.model.GameStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScoreAndAchievementTest {

    @Test
    fun winUpdatesStatsAndDifficulty() {
        val profile = PlayerProfile()
        val updated = ScoreService.apply(profile, GameStatus.BLACK_WIN, Difficulty.MEDIUM)
        assertEquals(1, updated.wins)
        assertEquals(0, updated.losses)
        assertEquals(1, updated.gamesPlayed)
        assertEquals(1, updated.winStreak)
        assertEquals(1, updated.bestWinStreak)
        assertEquals(1, updated.winsByDifficulty["MEDIUM"])
    }

    @Test
    fun lossUpdatesLossesAndResetsStreak() {
        val profile = PlayerProfile(winStreak = 3, bestWinStreak = 3, wins = 3, gamesPlayed = 3)
        val updated = ScoreService.apply(profile, GameStatus.WHITE_WIN, Difficulty.EASY)
        assertEquals(1, updated.losses)
        assertEquals(0, updated.winStreak)
        assertEquals(3, updated.bestWinStreak)
    }

    @Test
    fun drawDoesNotAffectWinLoss() {
        val profile = PlayerProfile()
        val updated = ScoreService.apply(profile, GameStatus.DRAW, null)
        assertEquals(0, updated.wins)
        assertEquals(0, updated.losses)
        assertEquals(1, updated.draws)
        assertEquals(1, updated.gamesPlayed)
    }

    @Test
    fun firstWinUnlocksAchievements() {
        var profile = PlayerProfile()
        profile = ScoreService.apply(profile, GameStatus.BLACK_WIN, Difficulty.EASY)
        val unlocked = Achievements.newlyUnlocked(profile, wonAgainstAi = true, difficulty = Difficulty.EASY)
        val ids = unlocked.map { it.id }
        assertTrue("first_win" in ids)
        assertTrue("ai_first_win" in ids)
    }

    @Test
    fun achievementsNotRepeated() {
        var profile = PlayerProfile()
        profile = ScoreService.apply(profile, GameStatus.BLACK_WIN, Difficulty.EASY)
        val unlocked1 = Achievements.newlyUnlocked(profile, true, Difficulty.EASY)
        profile = profile.copy(achievements = unlocked1.map { it.id })
        val result2 = ScoreService.apply(profile, GameStatus.BLACK_WIN, Difficulty.EASY)
        val unlocked2 = Achievements.newlyUnlocked(result2, true, Difficulty.EASY)
        assertTrue(unlocked2.isEmpty())
    }

    @Test
    fun hardWinUnlocksHardAchievement() {
        var profile = PlayerProfile(winStreak = 1, wins = 1, bestWinStreak = 1, gamesPlayed = 1)
        profile = ScoreService.apply(profile, GameStatus.BLACK_WIN, Difficulty.HARD)
        val unlocked = Achievements.newlyUnlocked(profile, true, Difficulty.HARD)
        assertTrue(unlocked.any { it.id == "hard_win" })
    }

    @Test
    fun statsTrackedAcrossDifficulties() {
        var profile = PlayerProfile()
        fun win(d: Difficulty) {
            profile = ScoreService.apply(profile, GameStatus.BLACK_WIN, d)
        }
        win(Difficulty.EASY)
        win(Difficulty.EASY)
        win(Difficulty.HARD)
        assertEquals(2, profile.winsByDifficulty["EASY"])
        assertEquals(1, profile.winsByDifficulty["HARD"])
        assertEquals(3, profile.gamesPlayed)
    }
}
