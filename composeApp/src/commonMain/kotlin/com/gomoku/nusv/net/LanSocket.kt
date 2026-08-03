package com.gomoku.nusv.net

/**
 * 局域网 TCP 连接抽象。
 * - 房主：监听端口等待对方接入（阻塞，应在线程中调用）
 * - 加入者：连接指定 IP:端口
 * 返回 null 表示失败（端口占用 / 连接失败 / 平台不支持）。
 */
interface LanSocket {
    fun send(message: String)
    fun close()

    /**
     * 启动读循环：持续接收行消息并回调 [onLine]（后台线程），
     * 断开时回调 [onDisconnect]。
     */
    fun start(onLine: (String) -> Unit, onDisconnect: () -> Unit)
}

expect fun lanHost(port: Int): LanSocket?

expect fun lanClient(host: String, port: Int): LanSocket?

/** 平台是否支持局域网联机（iOS 暂不支持）。 */
expect fun lanSupported(): Boolean


/** 本机局域网 IPv4（用于房主展示；iOS 暂返回空）。 */
expect fun lanHostIp(): String
