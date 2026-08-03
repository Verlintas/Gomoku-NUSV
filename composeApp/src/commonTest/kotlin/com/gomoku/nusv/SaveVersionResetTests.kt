package com.gomoku.nusv

import com.gomoku.nusv.APP_VERSION
import com.gomoku.nusv.data.ProfileStore
import com.gomoku.nusv.data.PlayerProfile
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveVersionResetTests {

    private fun store(): ProfileStore = ProfileStore(MapSettings())

    @Test
    fun freshInstallGetsCurrentVersionAndPowerups() {
        val s = store()
        val p = s.loadProfile()
        assertEquals(APP_VERSION, p.appVersion)
        assertEquals(3, p.powerups["hint"])
        assertEquals(5, p.powerups["timeboost"])
    }

    @Test
    fun olderVersionSaveIsReset() {
        val s = store()
        // 模拟旧版本存档（appVersion 不匹配）
        s.saveProfile(PlayerProfile(wins = 42, gamesPlayed = 99, appVersion = "1.4.3"))
        val loaded = s.loadProfile()
        assertEquals(APP_VERSION, loaded.appVersion)
        assertEquals(0, loaded.wins)
        assertEquals(0, loaded.gamesPlayed)
    }

    @Test
    fun sameVersionSaveIsKept() {
        val s = store()
        s.saveProfile(PlayerProfile(wins = 7, gamesPlayed = 20, appVersion = APP_VERSION))
        val loaded = s.loadProfile()
        assertEquals(7, loaded.wins)
        assertEquals(20, loaded.gamesPlayed)
    }

    @Test
    fun importedSaveGetsCurrentVersion() {
        val s = store()
        // 先创建一份存档再导出
        s.saveProfile(PlayerProfile(wins = 5, appVersion = APP_VERSION))
        val exported = s.exportProfileJson()
        // 模拟"更新后"（不同 store 实例）导入
        val s2 = store()
        assertTrue(s2.importProfileJson(exported))
        val loaded = s2.loadProfile()
        assertEquals(APP_VERSION, loaded.appVersion)
        assertEquals(5, loaded.wins)
    }
}

class ResetProfileTest {
    @Test
    fun resetClearsEverything() {
        val store = ProfileStore(MapSettings())
        store.saveProfile(
            PlayerProfile(
                wins = 12, losses = 3, score = 999, powerups = mapOf("hint" to 9),
                achievements = listOf("first_win"), appVersion = APP_VERSION
            )
        )
        // 模拟 GameController.resetProfile 逻辑
        val fresh = PlayerProfile(appVersion = APP_VERSION, powerups = com.gomoku.nusv.data.PowerupSystem.initialPowerups())
        store.saveProfile(fresh)
        val loaded = store.loadProfile()
        assertEquals(0, loaded.wins)
        assertEquals(0, loaded.score)
        assertEquals(3, loaded.powerups["hint"])
        assertEquals(emptyList<String>(), loaded.achievements)
        assertEquals(APP_VERSION, loaded.appVersion)
    }
}
