package com.gomoku.nusv.data

import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.model.GameConfig
import com.gomoku.nusv.model.GameMode
import com.gomoku.nusv.model.Move
import com.gomoku.nusv.model.Position
import com.gomoku.nusv.model.Stone
import com.gomoku.nusv.APP_VERSION
import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
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
    val enabledEffects: List<String> = emptyList(),
    val totalTimeSec: Long = 0,
    val fastestWinSec: Int = 0,
    val longestGameMoves: Int = 0,
    val themeUses: Map<String, Int> = emptyMap(),
    val minigameWins: Int = 0,
    val signInDate: String = "",
    val signInStreak: Int = 0,
    val totalSignIns: Int = 0,
    val taskDate: String = "",
    val taskIds: List<String> = emptyList(),
    val taskProgress: List<Int> = emptyList(),
    val taskDone: List<Boolean> = emptyList(),
    val dailyTaskCompletions: Int = 0,
    val purchasedDecorations: List<String> = emptyList(),
    val selectedEffectColor: String = "default",
    val selectedGlow: String = "none",
    val selectedWinLine: String = "default",
    val powerups: Map<String, Int> = emptyMap(),
    val appVersion: String = ""
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

    /**
     * 加载存档。
     * 设计：每个版本号一次全新开始 —— 检测到存档来自其他版本（appVersion 不匹配）
     * 时自动重置为新玩家状态，不保留旧存档。想延续进度请先导出，更新后再导入。
     */
    fun loadProfile(): PlayerProfile {
        val loaded = rawProfile()
        val fresh = PlayerProfile(appVersion = APP_VERSION).withInitialPowerups()
        if (loaded == null || loaded.appVersion != APP_VERSION) {
            saveProfile(fresh)
            settings.remove(KEY_SAVED_GAME)
            cleanupLegacyKeys()
            return fresh
        }
        val migrated = loaded.withInitialPowerups()
        cleanupLegacyKeys()
        return migrated
    }

    /**
     * 清理旧版本残留数据，避免版本残留：
     * - 1.3.x 及更早的明文存档 key：已被加密 key 取代，读取迁移后删除。
     */
    private fun cleanupLegacyKeys() {
        if (settings.getStringOrNull(KEY_PROFILE) != null) {
            settings.remove(KEY_PROFILE)
        }
    }

    private fun rawProfile(): PlayerProfile? {
        val rawEnc = settings.getStringOrNull(KEY_PROFILE_ENC)
        if (rawEnc != null) {
            SaveCrypto.decrypt(rawEnc)?.let { raw ->
                return try {
                    json.decodeFromString<PlayerProfile>(raw)
                } catch (_: Exception) {
                    null
                }
            }
        }
        val raw = settings.getStringOrNull(KEY_PROFILE) ?: return null
        return try {
            json.decodeFromString<PlayerProfile>(raw)
        } catch (_: Exception) {
            null
        }
    }

    /** 迁移：无道具库存时赠送初始道具（每个用户一次） */
    private fun PlayerProfile.withInitialPowerups(): PlayerProfile =
        if (powerups.isEmpty()) copy(powerups = PowerupSystem.initialPowerups()) else this

    fun saveProfile(profile: PlayerProfile) {
        settings.putString(KEY_PROFILE_ENC, SaveCrypto.encrypt(json.encodeToString(profile)))
    }

    fun exportProfileJson(): String {
        val payload = json.encodeToString(loadProfile())
        return """{"v":1,"profile":$payload,"check":"${SaveCrypto.publicChecksum(payload)}"}"""
    }

    fun importProfileJson(text: String): Boolean {
        val trimmed = text.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return false
        val data = try {
            json.decodeFromString<ExportData>(trimmed)
        } catch (_: Exception) {
            return false
        }
        if (data.v != 1) return false
        val payload = json.encodeToString(data.profile)
        if (!SaveCrypto.publicChecksum(payload).equals(data.check.trim().uppercase(), ignoreCase = true)) return false
        val profile = try {
            json.decodeFromString<PlayerProfile>(payload)
        } catch (_: Exception) {
            return false
        }
        saveProfile(profile.copy(appVersion = APP_VERSION))
        return true
    }

    @Serializable
    private data class ExportData(val v: Int, val profile: JsonObject, val check: String)

    fun loadThemeId(): String = settings.getString(KEY_THEME, "wood")

    fun saveThemeId(id: String) {
        settings.putString(KEY_THEME, id)
    }

    fun loadLanguage(): String = settings.getString(KEY_LANGUAGE, "zh")

    fun saveLanguage(code: String) {
        settings.putString(KEY_LANGUAGE, code)
    }

    fun loadEffectsEnabled(): Boolean = settings.getBoolean(KEY_EFFECTS, true)

    fun saveEffectsEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_EFFECTS, enabled)
    }

    fun loadSoundEnabled(): Boolean = settings.getBoolean(KEY_SOUND, true)

    fun saveSoundEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_SOUND, enabled)
    }

    fun loadShowAiThinking(): Boolean = settings.getBoolean(KEY_AI_THINKING_HINT, true)

    fun saveShowAiThinking(enabled: Boolean) {
        settings.putBoolean(KEY_AI_THINKING_HINT, enabled)
    }

    fun loadShowPerfWarning(): Boolean = settings.getBoolean(KEY_PERF_WARNING, true)

    fun saveShowPerfWarning(enabled: Boolean) {
        settings.putBoolean(KEY_PERF_WARNING, enabled)
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
        private const val KEY_PROFILE_ENC = "profile_enc"
        private const val KEY_THEME = "theme_id"
        private const val KEY_BOARD_SIZE = "board_size"
        private const val KEY_MODE = "mode"
        private const val KEY_DIFFICULTY = "difficulty"
        private const val KEY_TIMER = "timer_enabled"
        private const val KEY_SECONDS = "seconds_per_move"
        private const val KEY_SAVED_GAME = "saved_game"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_EFFECTS = "effects_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_AI_THINKING_HINT = "ai_thinking_hint"
        private const val KEY_PERF_WARNING = "perf_warning"
    }
}
