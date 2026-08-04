package com.gomoku.nusv.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gomoku.nusv.i18n.I18n
import com.gomoku.nusv.ui.nav.NavController
import com.gomoku.nusv.ui.nav.Page
import com.gomoku.nusv.ui.theme.BoardTheme

/**
 * 局域网联机页：
 * 1) 未连接 —— 模式选择（创建房间 / 加入房间-扫描列表）
 * 2) 已连接未开局 —— 等待大厅（主机可开局，加入者等待）
 * 3) 开局 —— 自动进入对局页
 */
@Composable
fun LanPage(controller: GameController, theme: BoardTheme, nav: NavController) {
    var roomName by remember { mutableStateOf(controller.lanRoomName) }

    // 开局后自动进入对局页
    LaunchedEffect(controller.lanGameStarted) {
        if (controller.lanGameStarted) {
            nav.navigate(Page.GAME)
        }
    }

    Surface(color = theme.uiBackground, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { nav.back() }) {
                    Text("←", fontSize = 14.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        I18n.t("nav_lan"),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    Text(
                        I18n.t("lan_subtitle"),
                        fontSize = 12.sp,
                        color = theme.textSecondary
                    )
                }
            }

            if (controller.lanMode && !controller.lanGameStarted) {
                Lobby(controller, theme, roomName)
            } else if (!controller.lanMode) {
                ModeSelect(controller, theme, roomName, onRoomNameChange = { roomName = it })
            }

            if (controller.lanStatus == "lan_disconnected") {
                Text(I18n.t("lan_rejoin_hint"), fontSize = 12.sp, color = Color(0xFFC62828))
            }
        }
    }
}

@Composable
private fun ModeSelect(
    controller: GameController,
    theme: BoardTheme,
    roomName: String,
    onRoomNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!controller.lanAvailable()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFF57C00))
            ) {
                Text(
                    I18n.t("lan_unsupported"),
                    modifier = Modifier.padding(16.dp),
                    fontSize = 13.sp,
                    color = Color(0xFF7A4A00)
                )
            }
        }

        Text(
            when (controller.lanStatus) {
                "lan_connecting" -> I18n.t("lan_connecting")
                "lan_host_failed" -> I18n.t("lan_host_failed")
                "lan_join_failed" -> I18n.t("lan_join_failed")
                "lan_host_udp_failed" -> I18n.t("lan_host_udp_failed")
                else -> I18n.t("lan_setup")
            },
            fontSize = 13.sp,
            color = theme.textPrimary,
            textAlign = TextAlign.Center
        )

        // 创建房间
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.uiSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, theme.uiSurfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(I18n.t("lan_create_section"), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                TextField(
                    value = roomName,
                    onValueChange = onRoomNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(I18n.t("lan_room_name_hint"), fontSize = 13.sp, color = theme.textSecondary)
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )
                Button(
                    onClick = {
                        controller.lanRoomName = roomName
                        controller.startLanHost(roomName)
                    },
                    enabled = controller.lanStatus != "lan_waiting" && controller.lanStatus != "lan_connecting",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(I18n.t("lan_create"), fontSize = 14.sp)
                }
                if (controller.lanStatus == "lan_waiting" || controller.lanStatus == "lan_host_udp_failed") {
                    Text(
                        I18n.t("lan_waiting") + "  " + I18n.t("lan_port", "n" to "${com.gomoku.nusv.ui.GameController.LAN_PORT}"),
                        fontSize = 12.sp,
                        color = theme.textSecondary
                    )
                    Text(
                        I18n.t("lan_ip_label") + ": " + controller.lanHostIp().ifBlank { "-" },
                        fontSize = 12.sp,
                        color = theme.textSecondary
                    )
                }
            }
        }

        // 加入房间（扫描）
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.uiSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, theme.uiSurfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(I18n.t("lan_join_section"), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                Button(
                    onClick = { controller.scanLanRooms() },
                    enabled = !controller.scanning &&
                        controller.lanStatus != "lan_waiting" && controller.lanStatus != "lan_connecting",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (controller.scanning) I18n.t("lan_scanning") else I18n.t("lan_scan"), fontSize = 14.sp)
                }
                if (controller.scanning) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(modifier = Modifier.width(16.dp).height(16.dp), strokeWidth = 2.dp)
                        Text(I18n.t("lan_scanning_hint"), fontSize = 12.sp, color = theme.textSecondary)
                    }
                } else if (controller.discoveredRooms.isEmpty() && controller.lanStatus != "") {
                    Text(I18n.t("lan_no_rooms"), fontSize = 12.sp, color = theme.textSecondary)
                }
                controller.discoveredRooms.forEach { room ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = theme.uiSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Column(Modifier.weight(1f)) {
                                Text(room.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
                                Text(room.host, fontSize = 11.sp, color = theme.textSecondary)
                            }
                            Button(
                                onClick = { controller.joinLanRoom(room) },
                                enabled = controller.lanStatus != "lan_waiting" && controller.lanStatus != "lan_connecting"
                            ) {
                                Text(I18n.t("lan_join"), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        TextButton(onClick = { controller.stopLan() }) {
            Text(I18n.t("lan_cancel"), fontSize = 13.sp)
        }
    }
}

@Composable
private fun Lobby(controller: GameController, theme: BoardTheme, roomName: String) {
    val isHost = controller.lanRole == com.gomoku.nusv.ui.LanRole.HOST
    val statusText = when {
        !controller.lanConnected && isHost -> I18n.t("lan_wait_opponent")
        !controller.lanConnected -> I18n.t("lan_connecting")
        isHost -> I18n.t("lan_opponent_joined")
        else -> I18n.t("lan_wait_start")
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.uiSurface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, theme.accent.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                I18n.t("lan_lobby"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
            Text(
                if (isHost) {
                    I18n.t("lan_lobby_host") + " " + roomName.ifBlank { "Gomoku-NUSV" }
                } else {
                    I18n.t("lan_lobby_guest")
                },
                fontSize = 14.sp,
                color = theme.textPrimary
            )
            if (isHost) {
                Text(
                    I18n.t("lan_ip_label") + ": " + controller.lanHostIp().ifBlank { "-" },
                    fontSize = 13.sp,
                    color = theme.textSecondary
                )
            }
            if (controller.lanConnected && isHost) {
                Button(
                    onClick = { controller.hostStartGame() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(I18n.t("lan_start"), fontSize = 15.sp)
                }
                Text(I18n.t("lan_start_hint"), fontSize = 12.sp, color = theme.textSecondary)
            } else {
                CircularProgressIndicator(modifier = Modifier.width(28.dp).height(28.dp), strokeWidth = 3.dp)
                Text(statusText, fontSize = 13.sp, color = theme.textSecondary)
            }
            OutlinedButton(onClick = { controller.stopLan() }, modifier = Modifier.fillMaxWidth()) {
                Text(I18n.t("lan_leave"), fontSize = 13.sp)
            }
        }
    }
}
