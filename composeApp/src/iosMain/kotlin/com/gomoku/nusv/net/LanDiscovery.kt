package com.gomoku.nusv.net

/** iOS 局域网发现暂不支持（同 TCP 联机）。 */
actual class LanDiscovery actual constructor() {
    actual fun startHost(name: String): Boolean = false
    actual fun stop() {}
    actual fun scan(broadcastAddress: String, onFound: (LanRoom) -> Unit, onDone: () -> Unit) {
        onDone()
    }
}
