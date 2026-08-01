package com.gomoku.nusv

import com.gomoku.nusv.data.DailyTaskSystem
import com.gomoku.nusv.data.PlayerProfile
import com.gomoku.nusv.data.ScoreRules
import com.gomoku.nusv.data.SignInSystem
import com.gomoku.nusv.data.TaskType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailySystemTests {

    @Test
    fun scoreRules() {
        assertEquals(100, ScoreRules.gamePoints(isPlayerWin = true, isDraw = false, winStreak = 1))
        assertEquals(100 + 15, ScoreRules.gamePoints(isPlayerWin = true, isDraw = false, winStreak = 2))
        assertEquals(100 + 75, ScoreRules.gamePoints(isPlayerWin = true, isDraw = false, winStreak = 6))
        assertEquals(10, ScoreRules.gamePoints(isPlayerWin = false, isDraw = false, winStreak = 0))
        assertEquals(30, ScoreRules.gamePoints(isPlayerWin = false, isDraw = true, winStreak = 0))
    }

    @Test
    fun signInFirstDay() {
        val (updated, ok) = SignInSystem.signIn(PlayerProfile(), today = "2026-08-01")
        assertTrue(ok)
        assertEquals(50, updated.score)
        assertEquals(1, updated.signInStreak)
        assertEquals(1, updated.totalSignIns)
        assertEquals("2026-08-01", updated.signInDate)
    }

    @Test
    fun signInConsecutiveDaysIncreasesReward() {
        var p = PlayerProfile()
        val (d1, _) = SignInSystem.signIn(p, "2026-08-01")
        p = d1
        val (d2, _) = SignInSystem.signIn(p, "2026-08-02")
        p = d2
        val (d3, ok3) = SignInSystem.signIn(p, "2026-08-03")
        assertTrue(ok3)
        assertEquals(3, d3.signInStreak)
        assertEquals(50 + 60 + 70, d3.score)
    }

    @Test
    fun signInTwiceSameDayRejected() {
        val (d1, ok1) = SignInSystem.signIn(PlayerProfile(), "2026-08-01")
        assertTrue(ok1)
        val (d2, ok2) = SignInSystem.signIn(d1, "2026-08-01")
        assertFalse(ok2)
        assertEquals(50, d2.score)
    }

    @Test
    fun signInAfterGapResetsStreak() {
        val (d1, _) = SignInSystem.signIn(PlayerProfile(), "2026-08-01")
        val (d2, _) = SignInSystem.signIn(d1, "2026-08-02")
        val (d3, _) = SignInSystem.signIn(d2, "2026-08-05")
        assertEquals(1, d3.signInStreak)
        assertEquals(3, d3.totalSignIns)
    }

    @Test
    fun dailyTasksRollAndEvent() {
        val rolled = DailyTaskSystem.rollTasks(PlayerProfile(), "2026-08-01")
        assertEquals(3, rolled.taskIds.size)
        assertEquals(3, rolled.taskProgress.size)
        // 幂等：同一天再 roll 不换任务
        val rolledAgain = DailyTaskSystem.rollTasks(rolled, "2026-08-01")
        assertEquals(rolled.taskIds, rolledAgain.taskIds)
        // 次日重新 roll
        val nextDay = DailyTaskSystem.rollTasks(rolled, "2026-08-02")
        assertTrue(nextDay.taskIds.isNotEmpty())
        // 构造含 PLAY_GAME 的任务，验证事件推进加分
        val custom = rolled.copy(
            taskDate = "2026-08-03",
            taskIds = listOf("0:${TaskType.PLAY_GAME.key}"),
            taskProgress = listOf(0),
            taskDone = listOf(false)
        )
        val (afterPlay, reward) = DailyTaskSystem.onEvent(custom, TaskType.PLAY_GAME)
        assertTrue(reward > 0)
        assertTrue(afterPlay.score > 0)
        assertTrue(afterPlay.taskDone[0])
    }

    @Test
    fun taskRewardAwardedOnlyOnce() {
        var p = PlayerProfile(
            taskDate = "2026-08-01",
            taskIds = listOf("4:${TaskType.WIN_VS_AI.key}"),
            taskProgress = listOf(0),
            taskDone = listOf(false)
        )
        // 完成 win_vs_ai 类型任务，奖励一次
        val (p1, r1) = DailyTaskSystem.onEvent(p, TaskType.WIN_VS_AI, amount = 2)
        assertTrue(r1 > 0)
        assertTrue(p1.taskDone[0])
        // 再触发不重复奖励
        val (p2, r2) = DailyTaskSystem.onEvent(p1, TaskType.WIN_VS_AI, amount = 1)
        assertEquals(0, r2)
        assertEquals(p1.score, p2.score)
    }

    @Test
    fun signInStreakAcrossMonthBoundary() {
        val (d1, _) = SignInSystem.signIn(PlayerProfile(), "2026-07-31")
        val (d2, _) = SignInSystem.signIn(d1, "2026-08-01")
        assertEquals(2, d2.signInStreak)
    }
}
