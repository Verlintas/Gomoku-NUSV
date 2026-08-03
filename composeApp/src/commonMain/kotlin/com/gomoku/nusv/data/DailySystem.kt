package com.gomoku.nusv.data

import com.gomoku.nusv.todayStr
import kotlin.random.Random

/** 对局积分规则 */
object ScoreRules {
    const val WIN = 100
    const val LOSS = 10
    const val DRAW = 30
    const val STREAK_BONUS = 15
    const val STREAK_MAX = 5

    fun gamePoints(isPlayerWin: Boolean, isDraw: Boolean, winStreak: Int): Int {
        return when {
            isPlayerWin -> WIN + minOf(maxOf(winStreak - 1, 0), STREAK_MAX) * STREAK_BONUS
            isDraw -> DRAW
            else -> LOSS
        }
    }
}

/** 每日签到 */
object SignInSystem {

    fun rewardForStreak(streak: Int): Int = 50 + maxOf(streak - 1, 0) * 10

    /** @return 是否签到成功（今天未签） */
    fun signIn(profile: PlayerProfile, today: String = todayStr()): Pair<PlayerProfile, Boolean> {
        if (profile.signInDate == today) return profile to false
        val yesterday = previousDay(today)
        val newStreak = if (profile.signInDate == yesterday) profile.signInStreak + 1 else 1
        val reward = rewardForStreak(newStreak)
        return profile.copy(
            score = profile.score + reward,
            signInDate = today,
            signInStreak = newStreak,
            totalSignIns = profile.totalSignIns + 1
        ) to true
    }

    private fun previousDay(date: String): String {
        val parts = date.split("-")
        if (parts.size != 3) return ""
        var y = parts[0].toInt()
        var m = parts[1].toInt()
        var d = parts[2].toInt() - 1
        if (d <= 0) {
            m--
            if (m <= 0) {
                y--
                m = 12
            }
            d = daysInMonth(y, m)
        }
        val mm = if (m < 10) "0$m" else "$m"
        val dd = if (d < 10) "0$d" else "$d"
        return "$y-$mm-$dd"
    }

    private fun daysInMonth(y: Int, m: Int): Int = when (m) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        else -> if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) 29 else 28
    }
}

/** 每日任务 */
enum class TaskType(val key: String) {
    PLAY_GAME("task_play"),
    WIN_GAME("task_win"),
    WIN_VS_AI("task_win_ai"),
    USE_POWERUP("task_powerup")
}

data class DailyTask(val type: TaskType, val target: Int, val reward: Int)

object DailyTaskSystem {

    private val tasksPool = listOf(
        DailyTask(TaskType.PLAY_GAME, 1, 30),
        DailyTask(TaskType.PLAY_GAME, 3, 60),
        DailyTask(TaskType.WIN_GAME, 1, 40),
        DailyTask(TaskType.WIN_GAME, 2, 70),
        DailyTask(TaskType.WIN_VS_AI, 1, 40),
        DailyTask(TaskType.WIN_VS_AI, 2, 70),
        DailyTask(TaskType.USE_POWERUP, 1, 30),
        DailyTask(TaskType.USE_POWERUP, 2, 55)
    )

    fun isTaskActive(profile: PlayerProfile, today: String = todayStr()): Boolean =
        profile.taskDate == today && profile.taskIds.size == 3

    fun currentIndexes(profile: PlayerProfile, today: String = todayStr()): List<Int> {
        if (!isTaskActive(profile, today)) return emptyList()
        return profile.taskIds.mapNotNull { id ->
            id.substringBefore(":").toIntOrNull()
        }
    }

    fun tasksForToday(profile: PlayerProfile, today: String = todayStr()): List<DailyTask> =
        currentIndexes(profile, today).mapNotNull { tasksPool.getOrNull(it) }

    /** 若今天还没任务则生成 3 个随机任务（幂等） */
    fun rollTasks(profile: PlayerProfile, today: String = todayStr()): PlayerProfile {
        if (isTaskActive(profile, today)) return profile
        val indices = tasksPool.indices.shuffled(Random.Default).take(3).sorted()
        return profile.copy(
            taskDate = today,
            taskIds = indices.map { "$it:${tasksPool[it].type.key}" },
            taskProgress = indices.map { 0 },
            taskDone = indices.map { false }
        )
    }

    fun progressOf(type: TaskType, profile: PlayerProfile): Int {
        if (!isTaskActive(profile)) return 0
        var sum = 0
        profile.taskIds.forEachIndexed { i, id ->
            if (id.contains(type.key)) sum += profile.taskProgress.getOrElse(i) { 0 }
        }
        return sum
    }

    fun targetOf(type: TaskType, profile: PlayerProfile): Int {
        var sum = 0
        profile.taskIds.forEachIndexed { _, id ->
            if (id.contains(type.key)) {
                val poolIndex = id.substringBefore(":").toIntOrNull() ?: -1
                sum += tasksPool.getOrNull(poolIndex)?.target ?: 1
            }
        }
        return sum.coerceAtLeast(1)
    }

    /** 事件驱动更新：返回奖励积分（新完成的任务奖励总和） */
    fun onEvent(profile: PlayerProfile, type: TaskType, amount: Int = 1): Pair<PlayerProfile, Int> {
        var updated = profile
        var reward = 0
        profile.taskIds.forEachIndexed { i, id ->
            if (i >= updated.taskProgress.size) return@forEachIndexed
            if (id.contains(type.key) && !updated.taskDone.getOrElse(i) { false }) {
                val poolIndex = id.substringBefore(":").toIntOrNull() ?: return@forEachIndexed
                val task = tasksPool.getOrNull(poolIndex) ?: return@forEachIndexed
                if (task.type != type) return@forEachIndexed
                val newProgress = updated.taskProgress[i] + amount
                val newDone = newProgress >= task.target
                if (newDone) reward += task.reward
                updated = updated.copy(
                    taskProgress = updated.taskProgress.toMutableList().also { it[i] = newProgress },
                    taskDone = updated.taskDone.toMutableList().also { it[i] = newDone },
                    score = updated.score + if (newDone) task.reward else 0,
                    dailyTaskCompletions = updated.dailyTaskCompletions + if (newDone) 1 else 0
                )
            }
        }
        return updated to reward
    }
}

/** 装饰（积分购买，纯视觉） */
enum class DecorationType { EFFECT_COLOR, GLOW, WIN_LINE }

data class Decoration(
    val id: String,
    val nameKey: String,
    val descKey: String,
    val type: DecorationType,
    val price: Int
)

object DecorationRegistry {

    val all: List<Decoration> = listOf(
        Decoration("color_gold", "dec_color_gold", "dec_color_gold_desc", DecorationType.EFFECT_COLOR, 600),
        Decoration("color_violet", "dec_color_violet", "dec_color_violet_desc", DecorationType.EFFECT_COLOR, 800),
        Decoration("glow_gold", "dec_glow_gold", "dec_glow_gold_desc", DecorationType.GLOW, 500),
        Decoration("glow_blue", "dec_glow_blue", "dec_glow_blue_desc", DecorationType.GLOW, 500),
        Decoration("winline_neon", "dec_winline_neon", "dec_winline_neon_desc", DecorationType.WIN_LINE, 700)
    )

    fun byId(id: String): Decoration? = all.find { it.id == id }

    fun owned(profile: PlayerProfile, type: DecorationType): List<Decoration> =
        all.filter { it.type == type && it.id in profile.purchasedDecorations }

    fun isOwned(profile: PlayerProfile, id: String): Boolean = id in profile.purchasedDecorations

    /** 特效粒子配色（hex 色值列表） */
    fun effectColors(profile: PlayerProfile): List<Long> = when (profile.selectedEffectColor) {
        "gold" -> listOf(0xFFFFD54F, 0xFFFFFFFF, 0xFFFFAB40)
        "violet" -> listOf(0xFFCE93D8, 0xFFFFFFFF, 0xFFB388FF)
        else -> emptyList()
    }

    /** 棋子辉光色 */
    fun glowColor(profile: PlayerProfile): Long? = when (profile.selectedGlow) {
        "gold" -> 0xFFFFD54F
        "blue" -> 0xFF64B5F6
        else -> null
    }

    fun winLineColors(profile: PlayerProfile): Pair<Long, Long>? = when (profile.selectedWinLine) {
        "neon" -> 0xFFFF4081L to 0xFF40C4FFL
        else -> null
    }
}
