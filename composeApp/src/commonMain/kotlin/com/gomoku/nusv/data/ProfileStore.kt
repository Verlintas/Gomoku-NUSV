package com.gomoku.nusv.data

import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.model.GameConfig
import com.gomoku.nusv.model.GameMode
import com.gomoku.nusv.model.Move
import com.gomoku.nusv.model.Position
import com.gomoku.nusv.model.Stone
import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PlayerProfile(
    val score: Int = 0,
    val coins: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val winStreak: Int = 0,
    val bestWinStreak: Int = 0,
    val gamesPlayed: Int = 0,
    val purchasedThemes: List<String> = emptyList(),
    val achievements: List<String> = emptyList(),
    val winsByDifficulty: Map<String, Int> = emptyMap(),
    val purchasedEffects: List<String> = emptyList(),
    val enabledEffects: List<String> = emptyList()
)

@Serializable
data class SavedGame(
    val boardSize: Int,
    val cells: List<Int>,
    val history: List<MoveDto>,
    val currentStoneId: Int,
    val modeName: String,
    val difficultyName: String,
    val timerEnabled: Boolean,
    val secondsPerMove: Int,
    val elapsedMs: Long = 0
)

@Serializable
data class MoveDto(val row: Int, val col: Int, val stoneId: Int) {
    constructor(move: Move) : this(move.pos.row, move.pos.col, move.stone.id)
    fun toMove(): Move = Move(Position(row, col), requireNotNull(Stone.fromId(stoneId)))
}

class ProfileStore(private val settings: Settings) {

    private val json = Json { ignoreUnknownKeys = true }

    fun loadProfile(): PlayerProfile {
        val raw = settings.getStringOrNull(KEY_PROFILE) ?: return PlayerProfile()
        return try {
            json.decodeFromString<PlayerProfile>(raw)
        } catch (_: Exception) {
            PlayerProfile()
        }
    }

    fun saveProfile(profile: PlayerProfile) {
        settings.putString(KEY_PROFILE, json.encodeToString(profile))
    }

    fun loadThemeId(): String = settings.getString(KEY_THEME, "wood")

    fun saveThemeId(id: String) {
        settings.putString(KEY_THEME, id)
    }

    fun loadLanguage(): String = settings.getString(KEY_LANGUAGE, "zh")

    fun saveLanguage(code: String) {
        settings.putString(KEY_LANGUAGE, code)
    }

    fun loadConfig(): GameConfig {
        val mode = settings.getStringOrNull(KEY_MODE)?.let { name ->
            GameMode.entries.find { it.name == name }
        } ?: GameMode.VS_AI
        val difficulty = settings.getStringOrNull(KEY_DIFFICULTY)?.let { name ->
            Difficulty.entries.find { it.name == name }
        } ?: Difficulty.MEDIUM
        return GameConfig(
            boardSize = settings.getInt(KEY_BOARD_SIZE, 15),
            mode = mode,
            difficulty = difficulty,
            timerEnabled = settings.getBoolean(KEY_TIMER, true),
            secondsPerMove = settings.getInt(KEY_SECONDS, 60)
        )
    }

    fun saveConfig(config: GameConfig) {
        settings.putInt(KEY_BOARD_SIZE, config.validBoardSize)
        settings.putString(KEY_MODE, config.mode.name)
        settings.putString(KEY_DIFFICULTY, config.difficulty.name)
        settings.putBoolean(KEY_TIMER, config.timerEnabled)
        settings.putInt(KEY_SECONDS, config.secondsPerMove)
    }

    fun loadSavedGame(): SavedGame? {
        val raw = settings.getStringOrNull(KEY_SAVED_GAME) ?: return null
        return try {
            json.decodeFromString<SavedGame>(raw)
        } catch (_: Exception) {
            null
        }
    }

    fun saveSavedGame(game: SavedGame?) {
        if (game == null) {
            settings.remove(KEY_SAVED_GAME)
        } else {
            settings.putString(KEY_SAVED_GAME, json.encodeToString(game))
        }
    }

    companion object {
        private const val KEY_PROFILE = "profile"
        private const val KEY_THEME = "theme_id"
        private const val KEY_BOARD_SIZE = "board_size"
        private const val KEY_MODE = "mode"
        private const val KEY_DIFFICULTY = "difficulty"
        private const val KEY_TIMER = "timer_enabled"
        private const val KEY_SECONDS = "seconds_per_move"
        private const val KEY_SAVED_GAME = "saved_game"
        private const val KEY_LANGUAGE = "language"
    }
}
