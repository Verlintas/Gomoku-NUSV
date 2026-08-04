package com.gomoku.nusv

import com.gomoku.nusv.net.LanMessage
import com.gomoku.nusv.net.LanProtocol
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LanProtocolTests {

    @Test
    fun moveRoundTrip() {
        val msg = LanMessage.Move(7, 9)
        val encoded = LanProtocol.encode(msg)
        val decoded = LanProtocol.decode(encoded)
        assertEquals(msg, decoded)
    }

    @Test
    fun helloRoundTrip() {
        val msg = LanMessage.Hello("test")
        assertEquals(msg, LanProtocol.decode(LanProtocol.encode(msg)))
    }

    @Test
    fun objectMessagesRoundTrip() {
        assertTrue(LanProtocol.decode(LanProtocol.encode(LanMessage.Undo)) is LanMessage.Undo)
        assertTrue(LanProtocol.decode(LanProtocol.encode(LanMessage.Restart)) is LanMessage.Restart)
        assertTrue(LanProtocol.decode(LanProtocol.encode(LanMessage.Resign)) is LanMessage.Resign)
        assertTrue(LanProtocol.decode(LanProtocol.encode(LanMessage.Close)) is LanMessage.Close)
        val start = LanProtocol.decode(LanProtocol.encode(LanMessage.Start(13)))
        assertTrue(start is LanMessage.Start)
        if (start is LanMessage.Start) assertEquals(13, start.boardSize)
    }

    @Test
    fun garbageLineReturnsNull() {
        assertNull(LanProtocol.decode("not-json!!!"))
        assertNull(LanProtocol.decode(""))
    }

    @Test
    fun jsonHasTypeDiscriminator() {
        val encoded = LanProtocol.encode(LanMessage.Move(3, 3))
        assertTrue(encoded.contains("\"move\""))
    }
}

class LanSocketRoundTripTests {

    @Test
    fun hostAndClientExchangeMessages() {
        // JVM 单测：本机起 host + client 互发
        val port = 45789
        var hostReceived: String? = null
        var clientReceived: String? = null
        var hostConnected = false
        var clientConnected = false

        // lanHost 阻塞等待客户端连接（60s 超时），放后台线程
        var host: com.gomoku.nusv.net.LanSocket? = null
        val hostThread = Thread {
            host = com.gomoku.nusv.net.lanHost(port)
        }
        hostThread.isDaemon = true
        hostThread.start()
        Thread.sleep(400) // 等待绑定

        val client = com.gomoku.nusv.net.lanClient("127.0.0.1", port)
        assertNotNull(client, "client should connect")

        val deadline = System.currentTimeMillis() + 5000
        while (host == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
        }
        assertNotNull(host, "host should accept")

        host!!.start(onLine = { hostReceived = it }, onDisconnect = {})
        client.start(onLine = { clientReceived = it }, onDisconnect = {})

        // host -> client
        host!!.send("hello-from-host\n")
        // client -> host
        client.send("hello-from-client\n")

        // 等待消息（最多 3 秒）
        val waitDeadline = System.currentTimeMillis() + 3000
        while ((clientReceived == null || hostReceived == null) && System.currentTimeMillis() < waitDeadline) {
            Thread.sleep(50)
        }
        assertEquals("hello-from-host", clientReceived?.trim())
        assertEquals("hello-from-client", hostReceived?.trim())

        client.close()
        host!!.close()
    }
}

class LanDiscoveryLoopbackTests {

    @Test
    fun udpDiscoveryLoopback() {
        val discovery = com.gomoku.nusv.net.LanDiscovery()
        try {
            val ok = discovery.startHost("loopback-room")
            assertTrue(ok, "udp host should bind")
            Thread.sleep(300)

            var found: com.gomoku.nusv.net.LanRoom? = null
            val done = java.util.concurrent.atomic.AtomicBoolean(false)
            discovery.scan(
                broadcastAddress = "127.0.0.1",
                onFound = { found = it },
                onDone = { done.set(true) }
            )
            val deadline = System.currentTimeMillis() + 6000
            while ((found == null || !done.get()) && System.currentTimeMillis() < deadline) {
                Thread.sleep(50)
            }
            assertNotNull(found, "loopback scan should discover the host room")
            assertEquals("loopback-room", found?.name)
            assertTrue(done.get(), "scan should finish")
        } finally {
            discovery.stop()
        }
    }
}

/** 完整 LAN 对局流程集成测试：发现 → 连接 → Start → 落子 → 悔棋 → 认输。 */
class LanFullFlowTests {

    @Test
    fun fullFlowHostAndClient() {
        val discovery = com.gomoku.nusv.net.LanDiscovery()
        val hostPort = 45812
        val roomFound = java.util.concurrent.atomic.AtomicReference<com.gomoku.nusv.net.LanRoom?>(null)

        try {
            // 1) 主机创建房间（UDP 广播）
            assertTrue(discovery.startHost("integration-room"))
            Thread.sleep(400) // 等待 UDP 监听线程就绪

            // 2) 扫描发现房间
            val scanDone = java.util.concurrent.atomic.AtomicBoolean(false)
            discovery.scan("127.0.0.1", onFound = { roomFound.set(it) }, onDone = { scanDone.set(true) })
            val scanDeadline = System.currentTimeMillis() + 8000
            while ((roomFound.get() == null || !scanDone.get()) && System.currentTimeMillis() < scanDeadline) {
                Thread.sleep(50)
            }
            assertEquals("integration-room", roomFound.get()?.name)

            // 3) TCP 连接（模拟加入）
            var host: com.gomoku.nusv.net.LanSocket? = null
            val hostThread = Thread { host = com.gomoku.nusv.net.lanHost(hostPort) }
            hostThread.isDaemon = true
            hostThread.start()
            Thread.sleep(300)
            val client = com.gomoku.nusv.net.lanClient(roomFound.get()!!.host, hostPort)
            assertNotNull(client, "client should connect")
            val connDeadline = System.currentTimeMillis() + 5000
            while (host == null && System.currentTimeMillis() < connDeadline) Thread.sleep(50)
            assertNotNull(host, "host should accept")

            // 4) 消息双向（模拟 Start/Move/Undo/Resign 序列）
            val hostMsgs = mutableListOf<com.gomoku.nusv.net.LanMessage>()
            val clientMsgs = mutableListOf<com.gomoku.nusv.net.LanMessage>()
            host!!.start(onLine = { hostMsgs.add(LanProtocol.decode(it)!!) }, onDisconnect = {})
            client.start(onLine = { clientMsgs.add(LanProtocol.decode(it)!!) }, onDisconnect = {})

            host!!.send(LanProtocol.encode(LanMessage.Start(13)))    // 主机开局（13 路）
            Thread.sleep(200)
            val startMsg = clientMsgs.filterIsInstance<LanMessage.Start>().firstOrNull()
            assertTrue(startMsg != null, "client should receive Start")
            assertEquals(13, startMsg?.boardSize)

            client.send(LanProtocol.encode(LanMessage.Move(7, 7)))   // 白方落子（加入者视角）
            Thread.sleep(200)
            assertTrue(hostMsgs.any { it is LanMessage.Move }, "host should receive Move")
            client.send(LanProtocol.encode(LanMessage.Undo))
            Thread.sleep(200)
            assertTrue(hostMsgs.any { it is LanMessage.Undo }, "host should receive Undo")
            host!!.send(LanProtocol.encode(LanMessage.Resign))
            Thread.sleep(200)
            assertTrue(clientMsgs.any { it is LanMessage.Resign }, "client should receive Resign")

            client.close()
            host!!.close()
        } finally {
            discovery.stop()
        }
    }
}
