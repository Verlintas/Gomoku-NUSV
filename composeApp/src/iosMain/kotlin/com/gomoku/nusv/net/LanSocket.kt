package com.gomoku.nusv.net

/** iOS 局域网联机暂不支持（TCP socket 适配待后续版本）。 */
actual fun lanHost(port: Int): LanSocket? = null

actual fun lanClient(host: String, port: Int): LanSocket? = null

actual fun lanSupported(): Boolean = false
