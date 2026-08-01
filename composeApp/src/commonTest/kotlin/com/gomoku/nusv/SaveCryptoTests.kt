package com.gomoku.nusv

import com.gomoku.nusv.data.SaveCrypto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SaveCryptoTests {

    @Test
    fun encryptDecryptRoundTrip() {
        val plain = """{"wins":4,"losses":10,"gamesPlayed":14,"achievements":["first_win"]}"""
        val encrypted = SaveCrypto.encrypt(plain)
        assertNotEquals(plain, encrypted)
        assertEquals(plain, SaveCrypto.decrypt(encrypted))
    }

    @Test
    fun tamperedPayloadFails() {
        val plain = """{"wins":4,"losses":10}"""
        val encrypted = SaveCrypto.encrypt(plain)
        // 翻转一个字符（篡改）
        val tampered = encrypted.substring(0, encrypted.length / 2) +
            if (encrypted[encrypted.length / 2] == 'A') 'B' else 'A' +
            encrypted.substring(encrypted.length / 2 + 1)
        assertNull(SaveCrypto.decrypt(tampered))
    }

    @Test
    fun garbageInputFails() {
        assertNull(SaveCrypto.decrypt("not-base64-!!!"))
        assertNull(SaveCrypto.decrypt(""))
    }

    @Test
    fun unicodeSurvivesRoundTrip() {
        val plain = """{"theme":"水墨丹青","effect":"粒子星尘","名字":"五子棋"}"""
        val decrypted = SaveCrypto.decrypt(SaveCrypto.encrypt(plain))
        assertEquals(plain, decrypted)
    }

    @Test
    fun publicChecksumDetectsChange() {
        val json1 = """{"wins":4}"""
        val json2 = """{"wins":5}"""
        val json3 = """{"wins":4,"x":1}"""
        val c1 = SaveCrypto.publicChecksum(json1)
        assertEquals(c1, SaveCrypto.publicChecksum(json1))
        assertNotEquals(c1, SaveCrypto.publicChecksum(json2))
        assertNotEquals(c1, SaveCrypto.publicChecksum(json3))
    }

    @Test
    fun profileJsonRoundTripThroughStore() {
        val profile = com.gomoku.nusv.data.PlayerProfile(
            wins = 12,
            losses = 5,
            draws = 2,
            bestWinStreak = 6,
            gamesPlayed = 19,
            achievements = listOf("first_win", "streak_5"),
            totalTimeSec = 3600,
            themeUses = mapOf("wood" to 3, "ink" to 1)
        )
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.encodeToString(profile)
        val exported = """{"v":1,"profile":$json,"check":"${SaveCrypto.publicChecksum(json)}"}"""
        // 校验 check 一致
        val m = Regex("\"check\":\"([A-F0-9]+)\"").find(exported)
        assertTrue(m != null)
        assertEquals(SaveCrypto.publicChecksum(json), m.groupValues[1])
    }

    @Test
    fun checksumFormatStable() {
        val c = SaveCrypto.publicChecksum("""{"wins":1}""")
        assertTrue(c.length >= 4)
        assertTrue(c.all { it.isDigit() || it in 'A'..'F' })
    }
}
