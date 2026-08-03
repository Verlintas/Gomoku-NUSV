package com.gomoku.nusv.net

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

actual class LanDiscovery actual constructor() {

    @Volatile
    private var running = false
    private var hostSocket: DatagramSocket? = null

    actual fun startHost(name: String): Boolean {
        stop()
        return try {
            val socket = DatagramSocket(LanProtocol.DISCOVER_PORT)
            socket.broadcast = true
            hostSocket = socket
            running = true
            thread(isDaemon = true, name = "gomoku-lan-udp-host") {
                val buf = ByteArray(1024)
                while (running) {
                    try {
                        val packet = DatagramPacket(buf, buf.size)
                        socket.receive(packet)
                        val msg = String(packet.data, 0, packet.length, Charsets.UTF_8)
                        if (LanProtocol.decode(msg) is LanMessage.Discover) {
                            val offer = LanProtocol.encode(LanMessage.Offer(name))
                            val bytes = offer.toByteArray()
                            val reply = DatagramPacket(bytes, bytes.size, packet.address, packet.port)
                            socket.send(reply)
                        }
                    } catch (_: Exception) {
                        if (!running) break
                    }
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    actual fun stop() {
        running = false
        try {
            hostSocket?.close()
        } catch (_: Exception) {
        }
        hostSocket = null
    }

    actual fun scan(broadcastAddress: String, onFound: (LanRoom) -> Unit, onDone: () -> Unit) {
        thread(isDaemon = true, name = "gomoku-lan-udp-scan") {
            val socket = DatagramSocket()
            socket.broadcast = true
            socket.soTimeout = 400
            val discover = LanProtocol.encode(LanMessage.Discover).toByteArray()
            val end = System.currentTimeMillis() + 2000
            val seen = HashSet<String>()
            try {
                while (System.currentTimeMillis() < end) {
                    try {
                        val out = DatagramPacket(
                            discover,
                            discover.size,
                            InetAddress.getByName(broadcastAddress),
                            LanProtocol.DISCOVER_PORT
                        )
                        socket.send(out)
                    } catch (_: Exception) {
                    }
                    val winEnd = System.currentTimeMillis() + 400
                    while (System.currentTimeMillis() < winEnd) {
                        try {
                            val buf = ByteArray(1024)
                            val packet = DatagramPacket(buf, buf.size)
                            socket.receive(packet)
                            val msg = String(packet.data, 0, packet.length, Charsets.UTF_8)
                            val offer = LanProtocol.decode(msg)
                            if (offer is LanMessage.Offer) {
                                val host = packet.address.hostAddress ?: continue
                                if (seen.add("$host|${offer.name}")) {
                                    onFound(LanRoom(offer.name, host))
                                }
                            }
                        } catch (_: Exception) {
                            break
                        }
                    }
                }
            } finally {
                try {
                    socket.close()
                } catch (_: Exception) {
                }
                onDone()
            }
        }
    }
}
