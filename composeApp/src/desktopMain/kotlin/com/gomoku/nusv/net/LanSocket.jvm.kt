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
        // 等待首个连接（最多 60 秒超时）
        server.soTimeout = 60_000
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
