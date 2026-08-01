package com.gomoku.nusv.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.model.GameConfig
import com.gomoku.nusv.model.GameMode
import com.gomoku.nusv.model.GameStatus
import com.gomoku.nusv.model.Stone
import com.gomoku.nusv.ui.theme.BoardTheme
import com.gomoku.nusv.ui.theme.ThemeRegistry

@Composable
fun GameScreen(
    controller: GameController,
    theme: BoardTheme,
    onThemeChange: (BoardTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(color = theme.uiBackground, modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Header(controller, theme)
            BoxWithConstraints(Modifier.fillMaxSize().weight(1f)) {
                if (maxWidth > 720.dp) {
                    Row(Modifier.fillMaxSize().padding(16.dp)) {
                        BoardArea(controller, theme, Modifier.weight(1f))
                        Spacer(Modifier.width(16.dp))
                        ControlPanel(controller, theme, onThemeChange, Modifier.width(300.dp))
                    }
                } else {
                    Column(Modifier.fillMaxSize().padding(12.dp)) {
                        BoardArea(controller, theme, Modifier.weight(1f))
                        Spacer(Modifier.height(12.dp))
                        ControlPanel(controller, theme, onThemeChange, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }

    ResultDialog(controller, theme)
    ResignDialog(controller, theme)
    RestoreDialog(controller, theme)
}

@Composable
private fun Header(controller: GameController, theme: BoardTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.uiSurface)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Gomoku-NUSV",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
            Text(
                "五子棋对弈 · ${if (controller.isVsAi) "人机对战" else "双人对战"}",
                fontSize = 12.sp,
                color = theme.textSecondary
            )
        }
        ScoreBadge(controller, theme)
    }
}

@Composable
private fun ScoreBadge(controller: GameController, theme: BoardTheme) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.uiSurfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("积分", fontSize = 11.sp, color = theme.textSecondary)
                Text(
                    "${controller.profile.score}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.accent
                )
            }
            VerticalStat(controller.profile.wins, "胜", theme)
            VerticalStat(controller.profile.losses, "负", theme)
            VerticalStat(controller.profile.draws, "平", theme)
            VerticalStat(controller.profile.winStreak, "连胜", theme)
        }
    }
}

@Composable
private fun VerticalStat(value: Int, label: String, theme: BoardTheme) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
        Text(label, fontSize = 11.sp, color = theme.textSecondary)
    }
}

@Composable
private fun BoardArea(controller: GameController, theme: BoardTheme, modifier: Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .clickable(
                role = Role.Button,
                onClickLabel = "棋盘，点击棋盘中心落子"
            ) {
                val mid = controller.board.size / 2
                controller.handleTap(mid, mid)
            },
        contentAlignment = Alignment.Center
    ) {
        BoardView(
            board = controller.board,
            theme = theme,
            lastMove = controller.lastMove,
            isPlayerTurn = controller.isPlayerTurn,
            hintStone = if (controller.isPlayerTurn) controller.currentStone else null,
            boardVersion = controller.boardVersion,
            onCellTap = controller::handleTap,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
        if (controller.aiThinking) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.uiSurface.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "AI 思考中...",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (controller.aiTimedOut) {
            PerformanceWarning(controller, theme)
        }
    }
}

@Composable
private fun PerformanceWarning(controller: GameController, theme: BoardTheme) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(4_000)
        controller.aiTimedOut = false
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF57C00))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("⚠", fontSize = 18.sp)
            Column {
                Text(
                    "设备算力有限，本步 AI 已降低思考深度",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7A4A00)
                )
                Text(
                    "若想获得最强 AI，请使用性能更强的设备",
                    fontSize = 11.sp,
                    color = Color(0xFF9C6C2E)
                )
            }
        }
    }
}

@Composable
private fun ControlPanel(
    controller: GameController,
    theme: BoardTheme,
    onThemeChange: (BoardTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val config = controller.config
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = theme.uiSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, theme.uiSurfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TurnIndicator(controller, theme)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("悔棋", enabled = controller.moveHistory.isNotEmpty() && !controller.aiThinking) {
                    controller.undo()
                }
                ActionButton("认输", enabled = !controller.status.isOver && controller.moveHistory.isNotEmpty()) {
                    controller.resign()
                }
                ActionButton("重开", enabled = true) {
                    controller.restart()
                }
            }

            HorizontalDivider(color = theme.uiSurfaceVariant)

            Text("对局设置", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
            ChipGroup("模式", theme) {
                ChoiceChip(
                    "双人",
                    config.mode == GameMode.PVP,
                    theme
                ) { controller.setMode(GameMode.PVP) }
                ChoiceChip(
                    "人机",
                    config.mode == GameMode.VS_AI,
                    theme
                ) { controller.setMode(GameMode.VS_AI) }
            }
            if (controller.isVsAi) {
                ChipGroup("难度", theme) {
                    Difficulty.entries.forEach { d ->
                        ChoiceChip(d.displayName, config.difficulty == d, theme) {
                            controller.setDifficulty(d)
                        }
                    }
                }
                ChipGroup("执子", theme) {
                    ChoiceChip("黑棋先手", controller.playerColor == Stone.BLACK, theme) {
                        controller.setPlayerStone(Stone.BLACK)
                    }
                    ChoiceChip("白棋后手", controller.playerColor == Stone.WHITE, theme) {
                        controller.setPlayerStone(Stone.WHITE)
                    }
                }
            }
            ChipGroup("棋盘", theme) {
                GameConfig.BOARD_SIZES.forEach { size ->
                    ChoiceChip("${size}路", config.boardSize == size, theme) {
                        controller.setBoardSize(size)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("每步限时", modifier = Modifier.weight(1f), fontSize = 13.sp, color = theme.textPrimary)
                Switch(
                    checked = config.timerEnabled,
                    onCheckedChange = { controller.setTimerEnabled(it) }
                )
            }
            if (config.timerEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${config.secondsPerMove}s",
                        modifier = Modifier.width(56.dp),
                        fontSize = 13.sp,
                        color = theme.accent,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = config.secondsPerMove.toFloat(),
                        onValueChange = { controller.setSecondsPerMove(it.toInt()) },
                        valueRange = 10f..300f,
                        steps = 28
                    )
                }
            }

            ChipGroup("主题", theme) {
                ThemeRegistry.themes.forEach { t ->
                    ChoiceChip(t.name, t.id == theme.id, theme) { onThemeChange(t) }
                }
            }

            HorizontalDivider(color = theme.uiSurfaceVariant)

            Text("对局信息", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
            InfoRow("总用时", formatClock(controller.totalSeconds), theme)
            InfoRow("落子数", "${controller.moveHistory.size}", theme)
            InfoRow("当前回合", controller.currentStone.displayName, theme)
        }
    }
}

@Composable
private fun TurnIndicator(controller: GameController, theme: BoardTheme) {
    val playerLabel = if (controller.isVsAi) {
        "${controller.playerColor.displayName}（你） vs AI"
    } else {
        "双人同屏"
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(playerLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            StoneDot(controller.currentStone, theme)
            Spacer(Modifier.width(8.dp))
            Text(
                if (controller.isVsAi) {
                    if (controller.currentStone == controller.playerColor) "轮到你了" else "AI 思考中"
                } else {
                    "${controller.currentStone.displayName}回合"
                },
                fontSize = 14.sp,
                color = theme.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                formatClock(controller.turnSecondsLeft),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (controller.turnSecondsLeft <= 10 && controller.config.timerEnabled) {
                    Color(0xFFD32F2F)
                } else {
                    theme.textPrimary
                }
            )
        }
    }
}

@Composable
private fun StoneDot(stone: Stone, theme: BoardTheme) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(if (stone == Stone.BLACK) theme.blackStone else theme.whiteStone)
            .border(1.dp, theme.textSecondary.copy(alpha = 0.4f), CircleShape)
    )
}

@Composable
private fun ChipGroup(title: String, theme: BoardTheme, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontSize = 12.sp, color = theme.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, theme: BoardTheme, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun RowScope.ActionButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.weight(1f)
    ) {
        Text(label, fontSize = 13.sp)
    }
}

@Composable
private fun InfoRow(label: String, value: String, theme: BoardTheme) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, color = theme.textSecondary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, color = theme.textPrimary, fontWeight = FontWeight.Medium)
    }
}

private fun formatClock(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val m = s / 60
    val sec = s % 60
    return "$m:${if (sec < 10) "0" else ""}$sec"
}

// ---------- 弹窗 ----------

@Composable
private fun ResultDialog(controller: GameController, theme: BoardTheme) {
    if (!controller.showResultDialog || controller.scoreResult == null) return
    val result = controller.scoreResult!!
    val status = controller.status
    val (title, titleColor) = when (status) {
        GameStatus.BLACK_WIN -> "黑棋获胜" to Color(0xFF2E7D32)
        GameStatus.WHITE_WIN -> "白棋获胜" to Color(0xFFC62828)
        GameStatus.DRAW -> "平局" to theme.textSecondary
        else -> return
    }
    AlertDialog(
        onDismissRequest = { controller.showResultDialog = false },
        containerColor = theme.uiSurface,
        title = {
            Text(
                title,
                color = titleColor,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val isPlayerWin = status == GameStatus.BLACK_WIN
                val isDraw = status == GameStatus.DRAW
                if (controller.isVsAi) {
                    Text(
                        when {
                            isDraw -> "与 AI 战成平手"
                            isPlayerWin -> "你击败了 AI！"
                            else -> "AI 获胜，再接再厉"
                        },
                        color = theme.textPrimary,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        if (isDraw) "双方战成平手" else "恭喜${controller.currentStone.opponent.displayName}",
                        color = theme.textPrimary,
                        fontSize = 14.sp
                    )
                }
                if (result.breakdown.isNotEmpty()) {
                    HorizontalDivider(color = theme.uiSurfaceVariant)
                    result.breakdown.forEach { line ->
                        Text(
                            line,
                            color = theme.textSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                Text(
                    "积分 +${result.scoreGained}",
                    color = theme.accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(onClick = { controller.showResultDialog = false; controller.restart() }) {
                Text("再来一局")
            }
        },
        dismissButton = {
            TextButton(onClick = { controller.showResultDialog = false }) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun ResignDialog(controller: GameController, theme: BoardTheme) {
    if (!controller.resignRequested) return
    AlertDialog(
        onDismissRequest = controller::cancelResign,
        containerColor = theme.uiSurface,
        title = { Text("确认认输？", color = theme.textPrimary) },
        text = { Text("认输后本局将判定对方获胜。", color = theme.textSecondary) },
        confirmButton = {
            Button(onClick = controller::confirmResign) {
                Text("确认认输")
            }
        },
        dismissButton = {
            TextButton(onClick = controller::cancelResign) { Text("取消") }
        }
    )
}

@Composable
private fun RestoreDialog(controller: GameController, theme: BoardTheme) {
    val saved = controller.pendingSavedGame
    if (!controller.restoreRequested || saved == null) return
    AlertDialog(
        onDismissRequest = {},
        containerColor = theme.uiSurface,
        title = { Text("发现未完成的对局", color = theme.textPrimary) },
        text = {
            Text(
                "棋盘 ${saved.boardSize} 路 · 已落 ${saved.history.size} 子 · " +
                    "${if (saved.modeName == "PVP") "双人" else "人机"}模式\n是否继续上次对局？",
                color = theme.textSecondary
            )
        },
        confirmButton = {
            Button(onClick = { controller.continueSavedGame(saved) }) {
                Text("继续对局")
            }
        },
        dismissButton = {
            TextButton(onClick = controller::discardSavedGame) { Text("放弃并重开") }
        }
    )
}
