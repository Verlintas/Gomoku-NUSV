package com.gomoku.nusv.data

/**
 * 存档混淆与校验。
 * 目的：防止用户直接修改本地存档文件（防篡改）。
 * 说明：使用固定密钥的轮转异或 + 混淆，属于"防手改"级别，
 * 并非密码学意义上的强加密（密钥在客户端）。
 */
object SaveCrypto {

    private val key = byteArrayOf(
        0x47, 0x6F, 0x6D, 0x6F, 0x6B, 0x75, 0x4E, 0x55,
        0x53, 0x56, 0x2D, 0x73, 0x61, 0x6C, 0x74, 0x31
    )

    fun encrypt(plain: String): String {
        val data = plain.encodeToByteArray()
        val out = ByteArray(data.size)
        for (i in data.indices) {
            val v = (data[i].toInt() and 0xFF) xor (key[i % key.size].toInt() and 0xFF) xor (i * 31 and 0xFF)
            out[i] = v.toByte()
        }
        val withCheck = out + checksumBytes(out)
        return encodeBase64(withCheck)
    }

    fun decrypt(encoded: String): String? {
        return try {
            val bytes = decodeBase64(encoded)
            if (bytes.size < 4) return null
            val check = bytes.copyOfRange(bytes.size - 4, bytes.size)
            val body = bytes.copyOfRange(0, bytes.size - 4)
            val expected = checksumBytes(body)
            if (!check.contentEquals(expected)) return null
            val out = ByteArray(body.size)
            for (i in body.indices) {
                val v = (body[i].toInt() and 0xFF) xor (key[i % key.size].toInt() and 0xFF) xor (i * 31 and 0xFF)
                out[i] = v.toByte()
            }
            out.decodeToString()
        } catch (_: Exception) {
            null
        }
    }

    private val b64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    private fun encodeBase64(data: ByteArray): String {
        val sb = StringBuilder()
        var i = 0
        while (i < data.size) {
            val b0 = data[i].toInt() and 0xFF
            val b1 = if (i + 1 < data.size) data[i + 1].toInt() and 0xFF else -1
            val b2 = if (i + 2 < data.size) data[i + 2].toInt() and 0xFF else -1
            sb.append(b64[b0 shr 2])
            sb.append(b64[((b0 shl 4) or (b1 shr 4)) and 0x3F])
            sb.append(if (b1 >= 0) b64[((b1 shl 2) or (b2 shr 6)) and 0x3F] else '=')
            sb.append(if (b2 >= 0) b64[b2 and 0x3F] else '=')
            i += 3
        }
        return sb.toString()
    }

    private fun decodeBase64(s: String): ByteArray {
        val clean = s.filter { it != '=' }
        val out = ArrayList<Byte>(clean.length * 3 / 4)
        var buffer = 0
        var bits = 0
        for (c in clean) {
            val v = b64.indexOf(c)
            if (v < 0) continue
            buffer = (buffer shl 6) or v
            bits += 6
            if (bits >= 8) {
                bits -= 8
                out.add(((buffer shr bits) and 0xFF).toByte())
            }
        }
        return out.toByteArray()
    }

    private fun checksumBytes(data: ByteArray): ByteArray {
        var a = 0x13579BDF.toInt()
        var b = 0x2468ACE0.toInt()
        for (i in data.indices) {
            a = a * 31 + data[i].toInt() and 0x7FFFFFFF
            b = (b * 17 xor (data[i].toInt() shl (i % 8))) and 0x7FFFFFFF
        }
        return byteArrayOf(
            (a and 0xFF).toByte(),
            ((a shr 8) and 0xFF).toByte(),
            (b and 0xFF).toByte(),
            ((b shr 8) and 0xFF).toByte()
        )
    }

    /** 导出存档的公开校验（明文 JSON 导出时附带，导入时校验）。 */
    fun publicChecksum(json: String): String {
        var h = 0
        for (i in json.indices) {
            h = (h * 33 + json[i].code) and 0x7FFFFFFF
        }
        return (h.toUInt().toString(16) + json.length.toUInt().toString(16)).uppercase()
    }
}
