package com.gomoku.nusv.net

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import kotlin.concurrent.thread

private class JvmLanSocket(private val socket: Socket) : LanSocket {

    @Volatile
    private var running = false

    private val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

    @Synchronized
    override fun send(message: String) {
        try {
            writer.write(message + "\n")
            writer.flush()
        } catch (_: Exception) {
        }
    }

    override fun close() {
        running = false
        try {
            socket.close()
        } catch (_: Exception) {
        }
    }

    override fun start(onLine: (String) -> Unit, onDisconnect: () -> Unit) {
        running = true
        thread(isDaemon = true, name = "gomoku-lan-read") {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (running) {
                    val line = reader.readLine() ?: break
                    if (line.isNotBlank()) onLine(line)
                }
            } catch (_: Exception) {
            } finally {
                running = false
                onDisconnect()
            }
        }
    }
}

actual fun lanHost(port: Int): LanSocket? {
    return try {
        val server = ServerSocket()
        server.reuseAddress = true
        server.bind(InetSocketAddress(port))
        // 等待首个连接（最多 20 秒超时，取消/重试响应更快）
        server.soTimeout = 20_000
        val client = server.accept()
        server.close()
        JvmLanSocket(client)
    } catch (_: SocketTimeoutException) {
        null
    } catch (_: Exception) {
        null
    }
}

actual fun lanClient(host: String, port: Int): LanSocket? {
    return try {
        val socket = Socket()
        socket.connect(InetSocketAddress(host, port), 10_000)
        JvmLanSocket(socket)
    } catch (_: Exception) {
        null
    }
}

actual fun lanSupported(): Boolean = true

actual fun lanHostIp(): String {
    // 优先取对外路由地址（局域网 IP）
    val socket = java.net.Socket()
    try {
        socket.connect(java.net.InetSocketAddress("8.8.8.8", 80), 1500)
        socket.localAddress.hostAddress?.let { if (it.isNotBlank()) return it }
    } catch (_: Exception) {
    } finally {
        try { socket.close() } catch (_: Exception) {}
    }
    // 兜底：枚举本机非回环 IPv4 网卡
    return try {
        java.net.NetworkInterface.getNetworkInterfaces()
            .toList()
            .filter { it.isUp && !it.isLoopback && !it.isVirtual }
            .flatMap { it.inetAddresses.toList() }
            .mapNotNull { it.hostAddress }
            .firstOrNull { it.contains(".") && !it.startsWith("127.") } ?: ""
    } catch (_: Exception) {
        ""
    }
}
