package com.gomoku.nusv.net

/** 局域网房间（发现结果）。 */
data class LanRoom(val name: String, val host: String)

/**
 * UDP 广播房间发现。
 * - 房主：监听 DISCOVER_PORT，收到 Discover 后单播回复 Offer(房间名)
 * - 加入者：向广播地址发 Discover，收集 Offer 形成房间列表
 */
expect class LanDiscovery() {
    /** 开始广播服务（UDP 监听），返回是否成功（端口被占用等失败）。 */
    fun startHost(name: String): Boolean

    fun stop()

    /**
     * 扫描局域网（阻塞在调用线程，建议放 IO 线程）。
     * @param broadcastAddress 广播地址（局域网 255.255.255.255；测试可用 127.0.0.1）
     */
    fun scan(broadcastAddress: String, onFound: (LanRoom) -> Unit, onDone: () -> Unit)
}
