package com.gomoku.nusv.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gomoku.nusv.data.Achievement
import com.gomoku.nusv.data.Achievements
import com.gomoku.nusv.data.DailyTaskSystem
import com.gomoku.nusv.data.Decoration
import com.gomoku.nusv.data.DecorationRegistry
import com.gomoku.nusv.data.DecorationType
import com.gomoku.nusv.data.PowerupSystem
import com.gomoku.nusv.data.PowerupType
import com.gomoku.nusv.data.ProfileStore
import com.gomoku.nusv.data.ScoreRules
import com.gomoku.nusv.data.SignInSystem
import com.gomoku.nusv.data.TaskType
import com.gomoku.nusv.todayStr
import com.gomoku.nusv.data.SavedGame
import com.gomoku.nusv.data.ScoreService
import com.gomoku.nusv.logic.GomokuAI
import com.gomoku.nusv.logic.WinChecker
import com.gomoku.nusv.model.Board
import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.model.GameConfig
import com.gomoku.nusv.model.GameMode
import com.gomoku.nusv.model.GameStatus
import com.gomoku.nusv.model.Move
import com.gomoku.nusv.model.Position
import com.gomoku.nusv.model.Stone
import com.gomoku.nusv.i18n.I18n
import com.gomoku.nusv.sound.SoundPlayer
import com.gomoku.nusv.sound.SoundType
import com.gomoku.nusv.ui.effects.BoardEffect
import com.gomoku.nusv.ui.effects.EffectRegistry
import com.gomoku.nusv.ui.theme.BoardTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameController(
    private val store: ProfileStore,
    private val sound: SoundPlayer
) {

    var config by mutableStateOf(store.loadConfig())
    var profile by mutableStateOf(store.loadProfile())

    var board by mutableStateOf(Board(config.validBoardSize))
    var boardVersion by mutableIntStateOf(0)
    var moveHistory by mutableStateOf<List<Move>>(emptyList())
    var currentStone by mutableStateOf(Stone.BLACK)
    var status by mutableStateOf(GameStatus.PLAYING)
    var lastMove by mutableStateOf<Position?>(null)
    var aiThinking by mutableStateOf(false)
    var aiTimedOut by mutableStateOf(false)
    var turnSecondsLeft by mutableStateOf(config.secondsPerMove)
    var totalSeconds by mutableIntStateOf(0)
    var showResultDialog by mutableStateOf(false)
    var pendingSavedGame by mutableStateOf<SavedGame?>(null)
    var restoreRequested by mutableStateOf(false)
    var resignRequested by mutableStateOf(false)
    var winningLine by mutableStateOf<List<Pair<Int, Int>>?>(null)
    var newlyUnlocked by mutableStateOf<List<Achievement>>(emptyList())
    var showAchievementToast by mutableStateOf(false)
    var effectsEnabled by mutableStateOf(true)
    var aiHint by mutableStateOf<Position?>(null)
    var themeIdAtStart by mutableStateOf("")
    private var activeThemeId: String = ""

    var playerColor: Stone = Stone.BLACK

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var aiJob: Job? = null
    private var timerJob: Job? = null

    init {
        pendingSavedGame = store.loadSavedGame()
        restoreRequested = pendingSavedGame != null
        profile = DailyTaskSystem.rollTasks(profile)
        store.saveProfile(profile)
    }

    val isPlayerTurn: Boolean
        get() = status == GameStatus.PLAYING &&
            (config.mode == GameMode.PVP || currentStone == playerColor)

    val isVsAi: Boolean get() = config.mode == GameMode.VS_AI

    // ---------- 对局流程 ----------

    fun handleTap(row: Int, col: Int) {
        if (status.isOver || aiThinking) return
        if (config.mode == GameMode.VS_AI && currentStone != playerColor) return
        if (!board.isEmpty(row, col)) return
        place(row, col)
    }

    private fun place(row: Int, col: Int) {
        aiHint = null
        board.set(row, col, currentStone)
        val move = Move(Position(row, col), currentStone)
        moveHistory = moveHistory + move
        lastMove = move.pos
        boardVersion++
        sound.play(SoundType.PLACE)
        if (WinChecker.hasFive(board, row, col, currentStone)) {
            endGame(if (currentStone == Stone.BLACK) GameStatus.BLACK_WIN else GameStatus.WHITE_WIN)
            return
        }
        if (board.isFull()) {
            endGame(GameStatus.DRAW)
            return
        }
        currentStone = currentStone.opponent
        turnSecondsLeft = config.secondsPerMove
        persistGame()
        if (isVsAi && currentStone != playerColor) scheduleAiMove()
    }

    private fun endGame(finalStatus: GameStatus) {
        status = finalStatus
        timerJob?.cancel()
        val playerWon = finalStatus == GameStatus.BLACK_WIN
        val playerLost = finalStatus == GameStatus.WHITE_WIN
        val isDraw = finalStatus == GameStatus.DRAW
        val winnerStone = when (finalStatus) {
            GameStatus.BLACK_WIN -> Stone.BLACK
            GameStatus.WHITE_WIN -> Stone.WHITE
            else -> null
        }
        val last = lastMove
        winningLine = if (winnerStone != null && last != null) {
            WinChecker.winningLine(board, last.row, last.col, winnerStone)
        } else {
            null
        }
        val difficulty = if (isVsAi) config.difficulty else null
        profile = ScoreService.apply(profile, finalStatus, difficulty)
        if (playerWon && (profile.fastestWinSec == 0 || totalSeconds < profile.fastestWinSec)) {
            profile = profile.copy(fastestWinSec = totalSeconds)
        }
        profile = profile.copy(
            totalTimeSec = profile.totalTimeSec + totalSeconds,
            longestGameMoves = maxOf(profile.longestGameMoves, moveHistory.size)
        )
        if (themeIdAtStart.isNotBlank()) {
            val uses = profile.themeUses.toMutableMap()
            uses[themeIdAtStart] = (uses[themeIdAtStart] ?: 0) + 1
            profile = profile.copy(themeUses = uses)
        }
        val points = ScoreRules.gamePoints(playerWon, isDraw, profile.winStreak)
        profile = profile.copy(score = profile.score + points)
        val (taskProfile, taskReward) = DailyTaskSystem.onEvent(profile, TaskType.PLAY_GAME)
        profile = taskProfile.copy(score = taskProfile.score + taskReward)
        if (playerWon) {
            val (tp2, r2) = DailyTaskSystem.onEvent(profile, TaskType.WIN_GAME)
            profile = tp2.copy(score = tp2.score + r2)
            if (isVsAi) {
                val (tp3, r3) = DailyTaskSystem.onEvent(profile, TaskType.WIN_VS_AI)
                profile = tp3.copy(score = tp3.score + r3)
            }
        }
        val unlocked = Achievements.newlyUnlocked(profile, playerWon && isVsAi, difficulty)
        if (unlocked.isNotEmpty()) {
            profile = profile.copy(
                achievements = (profile.achievements + unlocked.map { it.id }).distinct()
            )
            newlyUnlocked = unlocked
            showAchievementToast = true
        }
        store.saveProfile(profile)
        showResultDialog = true
        sound.play(
            when {
                isDraw -> SoundType.DRAW
                playerWon -> SoundType.WIN
                else -> SoundType.TIMEOUT
            }
        )
        store.saveSavedGame(null)
    }

    fun undo() {
        if (aiThinking) return
        if (moveHistory.isEmpty()) return
        var count = if (isVsAi) 2 else 1
        var restored = false
        while (count > 0 && moveHistory.isNotEmpty()) {
            val last = moveHistory.last()
            board.cells[board.index(last.pos.row, last.pos.col)] = 0
            moveHistory = moveHistory.dropLast(1)
            currentStone = last.stone
            count--
            restored = true
        }
        if (restored) {
            status = GameStatus.PLAYING
            lastMove = moveHistory.lastOrNull()?.pos
            turnSecondsLeft = config.secondsPerMove
            aiHint = null
            boardVersion++
            persistGame()
            if (isVsAi && currentStone != playerColor) scheduleAiMove()
            startTimer()
        }
    }

    fun resign() {
        if (status.isOver || moveHistory.isEmpty()) {
            restart()
            return
        }
        resignRequested = true
    }

    fun confirmResign() {
        resignRequested = false
        val loser = currentStone
        endGame(if (loser == Stone.BLACK) GameStatus.WHITE_WIN else GameStatus.BLACK_WIN)
    }

    fun cancelResign() {
        resignRequested = false
    }

    fun restart() {
        aiJob?.cancel()
        timerJob?.cancel()
        board = Board(config.validBoardSize)
        moveHistory = emptyList()
        currentStone = Stone.BLACK
        status = GameStatus.PLAYING
        lastMove = null
        aiThinking = false
        turnSecondsLeft = config.secondsPerMove
        totalSeconds = 0
        aiTimedOut = false
        winningLine = null
        newlyUnlocked = emptyList()
        showAchievementToast = false
        aiHint = null
        themeIdAtStart = activeThemeId
        boardVersion++
        store.saveSavedGame(null)
        startTimer()
        if (isVsAi && playerColor != Stone.BLACK) scheduleAiMove()
    }

    fun resumeNewGame() {
        restart()
        pendingSavedGame = null
    }

    fun discardSavedGame() {
        store.saveSavedGame(null)
        pendingSavedGame = null
        restoreRequested = false
        restart()
    }
    fun continueSavedGame(game: SavedGame) {
        config = GameConfig(
            boardSize = game.boardSize,
            mode = GameMode.entries.find { it.name == game.modeName } ?: GameMode.VS_AI,
            difficulty = Difficulty.entries.find { it.name == game.difficultyName } ?: Difficulty.MEDIUM,
            timerEnabled = game.timerEnabled,
            secondsPerMove = game.secondsPerMove
        )
        board = Board(game.boardSize)
        game.cells.forEachIndexed { i, v -> board.cells[i] = v }
        moveHistory = game.history.map { it.toMove() }
        currentStone = Stone.fromId(game.currentStoneId) ?: Stone.BLACK
        status = GameStatus.PLAYING
        lastMove = moveHistory.lastOrNull()?.pos
        aiThinking = false
        aiTimedOut = false
        turnSecondsLeft = config.secondsPerMove
        totalSeconds = 0
        boardVersion++
        aiHint = null
        pendingSavedGame = null
        restoreRequested = false
        startTimer()
        if (isVsAi && currentStone != playerColor) scheduleAiMove()
    }

    // ---------- 设置 ----------

    fun setMode(mode: GameMode) {
        config = config.copy(mode = mode)
        store.saveConfig(config)
        restart()
    }

    fun setDifficulty(difficulty: Difficulty) {
        config = config.copy(difficulty = difficulty)
        store.saveConfig(config)
        restart()
    }

    fun setBoardSize(size: Int) {
        config = config.copy(boardSize = size)
        store.saveConfig(config)
        restart()
    }

    fun setTimerEnabled(enabled: Boolean) {
        config = config.copy(timerEnabled = enabled)
        store.saveConfig(config)
        if (!enabled) timerJob?.cancel()
        if (enabled && !status.isOver) startTimer()
    }

    fun setSecondsPerMove(seconds: Int) {
        config = config.copy(secondsPerMove = seconds.coerceIn(10, 300))
        store.saveConfig(config)
        turnSecondsLeft = config.secondsPerMove
    }

    fun setPlayerStone(stone: Stone) {
        playerColor = stone
        restart()
    }

    // ---------- 特效自定义开关 ----------

    fun isEffectEnabled(effect: BoardEffect): Boolean = effect.id in profile.enabledEffects

    fun setEffectEnabled(effect: BoardEffect, enabled: Boolean) {
        val current = profile.enabledEffects.toMutableList()
        if (enabled && effect.id !in current) current.add(effect.id)
        if (!enabled) current.remove(effect.id)
        profile = profile.copy(enabledEffects = current)
        store.saveProfile(profile)
    }

    // ---------- 道具（库存制） ----------

    fun powerupCount(type: PowerupType): Int = PowerupSystem.count(profile, type)

    fun purchasePowerup(type: PowerupType, amount: Int): Boolean {
        val updated = PowerupSystem.purchase(profile, type, amount) ?: return false
        profile = updated
        store.saveProfile(profile)
        return true
    }

    fun useHint() {
        if (status.isOver || aiThinking) return
        if (isVsAi && currentStone != playerColor) return
        if (powerupCount(PowerupType.HINT) <= 0) return
        profile = PowerupSystem.consume(profile, PowerupType.HINT)
        aiThinking = true
        aiHint = null
        val (tp, r) = DailyTaskSystem.onEvent(profile, TaskType.USE_POWERUP)
        profile = tp.copy(score = tp.score + r)
        aiJob?.cancel()
        aiJob = scope.launch {
            val hint = withContext(Dispatchers.Default) {
                val workBoard = Board(board.size)
                workBoard.copyFrom(board)
                GomokuAI.bestMove(workBoard, currentStone, config.difficulty).position
            }
            aiThinking = false
            if (status == GameStatus.PLAYING) aiHint = hint
        }
    }

    fun useTimeBoost() {
        if (status.isOver) return
        if (isVsAi && currentStone != playerColor) return
        if (powerupCount(PowerupType.TIMEBOOST) <= 0) return
        profile = PowerupSystem.consume(profile, PowerupType.TIMEBOOST)
        turnSecondsLeft += 30
        val (tp, r) = DailyTaskSystem.onEvent(profile, TaskType.USE_POWERUP)
        profile = tp.copy(score = tp.score + r)
    }

    // ---------- 主题记录 / 小游戏 ----------

    fun onThemeSelected(themeId: String) {
        activeThemeId = themeId
    }

    fun onMinigameWin() {
        profile = profile.copy(minigameWins = profile.minigameWins + 1)
        store.saveProfile(profile)
    }

    // ---------- 存档导出 / 导入 ----------

    fun exportSave(): String = store.exportProfileJson()

    fun importSave(text: String): Boolean {
        if (!store.importProfileJson(text)) return false
        profile = store.loadProfile()
        return true
    }

    // ---------- 签到 / 装饰 ----------

    fun signIn(): Boolean {
        val (updated, ok) = SignInSystem.signIn(profile)
        if (ok) {
            profile = updated
            store.saveProfile(profile)
        }
        return ok
    }

    fun purchaseDecoration(decoration: Decoration): Boolean {
        if (DecorationRegistry.isOwned(profile, decoration.id)) return true
        if (profile.score < decoration.price) return false
        profile = profile.copy(
            score = profile.score - decoration.price,
            purchasedDecorations = (profile.purchasedDecorations + decoration.id).distinct()
        )
        store.saveProfile(profile)
        return true
    }

    fun selectDecoration(id: String, type: DecorationType) {
        profile = when (type) {
            DecorationType.EFFECT_COLOR -> profile.copy(selectedEffectColor = id)
            DecorationType.GLOW -> profile.copy(selectedGlow = id)
            DecorationType.WIN_LINE -> profile.copy(selectedWinLine = id)
        }
        store.saveProfile(profile)
    }

    fun setEffectsEnabledFlag(enabled: Boolean) {
        effectsEnabled = enabled
    }

    // ---------- 语言 ----------

    fun setLanguage(language: I18n.Language) {
        I18n.setLanguage(language)
        store.saveLanguage(language.code)
    }

    // ---------- 计时 ----------

    fun startTimer() {
        timerJob?.cancel()
        if (!config.timerEnabled || status.isOver || restoreRequested) return
        timerJob = scope.launch {
            while (status == GameStatus.PLAYING) {
                delay(1000)
                if (status != GameStatus.PLAYING) break
                totalSeconds++
                turnSecondsLeft--
                if (turnSecondsLeft <= 0) {
                    withContext(Dispatchers.Main) { onTimeout() }
                    break
                }
            }
        }
    }

    private fun onTimeout() {
        if (status.isOver) return
        val winner = currentStone.opponent
        sound.play(SoundType.TIMEOUT)
        endGame(if (winner == Stone.BLACK) GameStatus.BLACK_WIN else GameStatus.WHITE_WIN)
    }

    // ---------- 存档 ----------

    private fun persistGame() {
        val game = SavedGame(
            boardSize = board.size,
            cells = board.cells.toList(),
            history = moveHistory.map { com.gomoku.nusv.data.MoveDto(it) },
            currentStoneId = currentStone.id,
            modeName = config.mode.name,
            difficultyName = config.difficulty.name,
            timerEnabled = config.timerEnabled,
            secondsPerMove = config.secondsPerMove
        )
        store.saveSavedGame(game)
    }

    // ---------- AI ----------

    private fun scheduleAiMove() {
        aiJob?.cancel()
        aiThinking = true
        aiTimedOut = false
        aiJob = scope.launch {
            val result = withContext(Dispatchers.Default) {
                val workBoard = Board(board.size)
                workBoard.copyFrom(board)
                GomokuAI.bestMove(workBoard, currentStone, config.difficulty)
            }
            aiThinking = false
            if (result.timedOut) aiTimedOut = true
            if (status == GameStatus.PLAYING && board.isEmpty(result.position.row, result.position.col)) {
                place(result.position.row, result.position.col)
            }
        }
    }
}
