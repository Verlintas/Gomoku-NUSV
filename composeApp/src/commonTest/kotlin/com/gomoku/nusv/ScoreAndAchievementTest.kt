package com.gomoku.nusv

import com.gomoku.nusv.data.Achievements
import com.gomoku.nusv.data.PlayerProfile
import com.gomoku.nusv.data.ScoreService
import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.model.GameStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScoreAndAchievementTest {

    @Test
    fun winGrantsScoreAndCoins() {
        val profile = PlayerProfile()
        val result = ScoreService.settle(profile, GameStatus.BLACK_WIN, playerSecondsLeft = null)
        assertEquals(100, result.scoreGained)
        assertEquals(50, result.coinsGained)
        val updated = ScoreService.apply(profile, result, isPlayerWin = true, isDraw = false, difficulty = Difficulty.MEDIUM)
        assertEquals(100, updated.score)
        assertEquals(50, updated.coins)
        assertEquals(1, updated.wins)
        assertEquals(1, updated.gamesPlayed)
        assertEquals(1, updated.winsByDifficulty["MEDIUM"])
    }

    @Test
    fun lossGrantsNothing() {
        val profile = PlayerProfile()
        val result = ScoreService.settle(profile, GameStatus.WHITE_WIN, playerSecondsLeft = null)
        assertEquals(0, result.scoreGained)
        assertEquals(0, result.coinsGained)
        val updated = ScoreService.apply(profile, result, isPlayerWin = false, isDraw = false, difficulty = Difficulty.EASY)
        assertEquals(1, updated.losses)
        assertEquals(0, updated.coins)
    }

    @Test
    fun drawGrantsCoins() {
        val profile = PlayerProfile()
        val result = ScoreService.settle(profile, GameStatus.DRAW, playerSecondsLeft = null)
        assertEquals(10, result.coinsGained)
    }

    @Test
    fun firstWinUnlocksAchievements() {
        var profile = PlayerProfile()
        // 第 1 胜（人机模式）
        val result = ScoreService.settle(profile, GameStatus.BLACK_WIN, null)
        profile = ScoreService.apply(profile, result, true, false, Difficulty.EASY)
        val unlocked = Achievements.newlyUnlocked(profile, wonAgainstAi = true, difficulty = Difficulty.EASY)
        val ids = unlocked.map { it.id }
        assertTrue("first_win" in ids)
        assertTrue("ai_first_win" in ids)
        // 奖励金币
        val reward = unlocked.sumOf { it.coinReward }
        assertEquals(100, reward)
    }

    @Test
    fun achievementsNotRepeated() {
        var profile = PlayerProfile()
        val result = ScoreService.settle(profile, GameStatus.BLACK_WIN, null)
        profile = ScoreService.apply(profile, result, true, false, Difficulty.EASY)
        val unlocked1 = Achievements.newlyUnlocked(profile, true, Difficulty.EASY)
        profile = profile.copy(achievements = unlocked1.map { it.id })
        // 再赢一局，不应重复解锁
        val result2 = ScoreService.settle(profile, GameStatus.BLACK_WIN, null)
        profile = ScoreService.apply(profile, result2, true, false, Difficulty.EASY)
        val unlocked2 = Achievements.newlyUnlocked(profile, true, Difficulty.EASY)
        assertTrue(unlocked2.isEmpty())
    }

    @Test
    fun hardWinUnlocksHardAchievement() {
        var profile = PlayerProfile(winStreak = 1, wins = 1, bestWinStreak = 1, gamesPlayed = 1)
        val result = ScoreService.settle(profile, GameStatus.BLACK_WIN, null)
        profile = ScoreService.apply(profile, result, true, false, Difficulty.HARD)
        val unlocked = Achievements.newlyUnlocked(profile, true, Difficulty.HARD)
        assertTrue(unlocked.any { it.id == "hard_win" })
    }

    @Test
    fun statsTrackedAcrossDifficulties() {
        var profile = PlayerProfile()
        fun win(d: Difficulty) {
            val r = ScoreService.settle(profile, GameStatus.BLACK_WIN, null)
            profile = ScoreService.apply(profile, r, true, false, d)
        }
        win(Difficulty.EASY)
        win(Difficulty.EASY)
        win(Difficulty.HARD)
        assertEquals(2, profile.winsByDifficulty["EASY"])
        assertEquals(1, profile.winsByDifficulty["HARD"])
        assertEquals(3, profile.gamesPlayed)
        assertFalse("hard_win" in profile.achievements)
    }
}
