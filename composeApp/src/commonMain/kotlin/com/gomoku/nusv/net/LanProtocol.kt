package com.gomoku.nusv.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** 局域网对战消息协议（JSON 行协议，\n 分隔）。 */
@Serializable
sealed class LanMessage {
    @Serializable
    @SerialName("hello")
    data class Hello(val name: String = "Gomoku-NUSV") : LanMessage()

    @Serializable
    @SerialName("move")
    data class Move(val row: Int, val col: Int) : LanMessage()

    @Serializable
    @SerialName("undo")
    data object Undo : LanMessage()

    @Serializable
    @SerialName("restart")
    data object Restart : LanMessage()

    @Serializable
    @SerialName("resign")
    data object Resign : LanMessage()

    @Serializable
    @SerialName("close")
    data object Close : LanMessage()
}

object LanProtocol {
    val json = Json { ignoreUnknownKeys = true }

    fun encode(msg: LanMessage): String = json.encodeToString(msg)

    fun decode(line: String): LanMessage? = try {
        json.decodeFromString<LanMessage>(line)
    } catch (_: Exception) {
        null
    }
}
