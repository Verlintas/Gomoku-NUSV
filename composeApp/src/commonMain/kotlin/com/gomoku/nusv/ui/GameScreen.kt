package com.gomoku.nusv.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gomoku.nusv.data.Achievements
import com.gomoku.nusv.i18n.I18n
import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.ui.effects.EffectRegistry
import com.gomoku.nusv.ui.effects.EffectsOverlay
import com.gomoku.nusv.model.GameConfig
import com.gomoku.nusv.model.GameMode
import com.gomoku.nusv.model.GameStatus
import com.gomoku.nusv.model.Stone
import com.gomoku.nusv.ui.theme.BoardTheme
import com.gomoku.nusv.ui.theme.ThemeRegistry

@Composable
fun GamePage(
    controller: GameController,
    theme: BoardTheme,
    nav: com.gomoku.nusv.ui.nav.NavController,
    onThemeChange: (BoardTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(color = theme.uiBackground, modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Header(controller, theme, nav)
            BoxWithConstraints(Modifier.fillMaxSize().weight(1f)) {
                val panelMaxHeight = maxHeight * 0.45f
                val boardMaxHeight = maxHeight * 0.55f
                if (maxWidth > 720.dp) {
                    Row(Modifier.fillMaxSize().padding(16.dp)) {
                        BoardArea(controller, theme, Modifier.weight(1f))
                        Spacer(Modifier.width(16.dp))
                        ControlPanel(controller, theme, onThemeChange, nav = nav, modifier = Modifier.width(300.dp))
                    }
                } else {
                    Column(Modifier.fillMaxSize().padding(12.dp)) {
                        BoardArea(
                            controller,
                            theme,
                            Modifier
                                .weight(1f)
                                .heightIn(max = boardMaxHeight)
                        )
                        Spacer(Modifier.height(12.dp))
                        ControlPanel(
                            controller,
                            theme,
                            onThemeChange,
                            nav = nav,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = panelMaxHeight)
                        )
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
private fun Header(controller: GameController, theme: BoardTheme, nav: com.gomoku.nusv.ui.nav.NavController? = null) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.uiSurface)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        val compact = maxWidth < 560.dp
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (nav != null) {
                OutlinedButton(
                    onClick = { nav.back() },
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text("←", fontSize = 14.sp)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "Gomoku-NUSV",
                    fontSize = if (compact) 19.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                if (!compact) {
                    Text(
                        I18n.t(if (controller.isVsAi) "subtitle_ai" else "subtitle_pvp"),
                        fontSize = 12.sp,
                        color = theme.textSecondary
                    )
                }
            }
            ScoreBadge(controller, theme, compact)
        }
    }
}

@Composable
private fun ScoreBadge(controller: GameController, theme: BoardTheme, compact: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.uiSurfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 14.dp)
        ) {
            VerticalStat(controller.profile.wins, "wins", theme)
            VerticalStat(controller.profile.losses, "losses", theme)
            if (!compact) {
                VerticalStat(controller.profile.draws, "draws", theme)
                VerticalStat(controller.profile.winStreak, "streak", theme)
            }
        }
    }
}

@Composable
private fun VerticalStat(value: Int, label: String, theme: BoardTheme) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
        Text(I18n.t(label), fontSize = 11.sp, color = theme.textSecondary)
    }
}

@Composable
private fun BoardArea(controller: GameController, theme: BoardTheme, modifier: Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .clickable(
                role = Role.Button,
                onClickLabel = I18n.t("board_click_hint")
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
            winningLine = controller.winningLine,
            hint = controller.aiHint,
            glowColor = com.gomoku.nusv.data.DecorationRegistry.glowColor(controller.profile)
                ?.let { androidx.compose.ui.graphics.Color(it) },
            winLineColors = com.gomoku.nusv.data.DecorationRegistry.winLineColors(controller.profile)
                ?.let { androidx.compose.ui.graphics.Color(it.first) to androidx.compose.ui.graphics.Color(it.second) },
            onCellTap = controller::handleTap,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
        EffectsOverlay(
            boardSize = controller.board.size,
            theme = theme,
            lastMove = controller.lastMove,
            boardVersion = controller.boardVersion,
            winningLine = controller.winningLine,
            enabled = controller.effectsEnabled,
            effectColors = com.gomoku.nusv.data.DecorationRegistry.effectColors(controller.profile)
                .map { androidx.compose.ui.graphics.Color(it) },
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
                    I18n.t("ai_thinking_short"),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (controller.aiTimedOut) {
            PerformanceWarning(controller, theme)
        }
        AchievementToast(controller, theme)
    }
}

@Composable
private fun AchievementToast(controller: GameController, theme: BoardTheme) {
    if (!controller.showAchievementToast || controller.newlyUnlocked.isEmpty()) return
    LaunchedEffect(controller.newlyUnlocked) {
        kotlinx.coroutines.delay(5_000)
        controller.showAchievementToast = false
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF0C94C))
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .graphicsLayer {
                    scaleX = 0.8f + progress.value * 0.2f
                    scaleY = 0.8f + progress.value * 0.2f
                    alpha = progress.value
                },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                I18n.t("achievement_unlocked"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8D6E00)
            )
            controller.newlyUnlocked.forEach { a ->
                Text(
                    I18n.t(a.nameKey),
                    fontSize = 13.sp,
                    color = Color(0xFF5D4A00)
                )
            }
        }
    }
}

@Composable
private fun PerformanceWarning(controller: GameController, theme: BoardTheme) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2_500)
        controller.aiTimedOut = false
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.uiSurface.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, theme.accent.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                I18n.t("perf_warn_title"),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = theme.textPrimary
            )
            Text(
                I18n.t("perf_warn_body"),
                fontSize = 10.sp,
                color = theme.textSecondary
            )
        }
    }
}

@Composable
private fun ControlPanel(
    controller: GameController,
    theme: BoardTheme,
    onThemeChange: (BoardTheme) -> Unit,
    nav: com.gomoku.nusv.ui.nav.NavController? = null,
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
                ActionButton(I18n.t("undo"), enabled = controller.moveHistory.isNotEmpty() && !controller.aiThinking) {
                    controller.undo()
                }
                ActionButton(I18n.t("resign"), enabled = !controller.status.isOver && controller.moveHistory.isNotEmpty()) {
                    controller.resign()
                }
                ActionButton(I18n.t("restart"), enabled = true) {
                    controller.restart()
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton(
                    I18n.t("powerup_hint") + if (controller.hintUsed) "✓" else "",
                    enabled = !controller.hintUsed && !controller.aiThinking && controller.isPlayerTurn && !controller.status.isOver
                ) {
                    controller.useHint()
                }
                ActionButton(
                    I18n.t("powerup_time") + " ${2 - controller.timeBoostUsed}",
                    enabled = controller.timeBoostUsed < 2 && !controller.status.isOver && controller.isPlayerTurn
                ) {
                    controller.useTimeBoost()
                }
            }

            HorizontalDivider(color = theme.uiSurfaceVariant)

            Text(I18n.t("settings"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
            ChipGroup(I18n.t("language"), theme) {
                I18n.Language.entries.forEach { lang ->
                    ChoiceChip(lang.displayName, I18n.currentLanguage == lang, theme) {
                        controller.setLanguage(lang)
                    }
                }
            }
            ChipGroup(I18n.t("mode"), theme) {
                ChoiceChip(
                    I18n.t("mode_pvp"),
                    config.mode == GameMode.PVP,
                    theme
                ) { controller.setMode(GameMode.PVP) }
                ChoiceChip(
                    I18n.t("mode_ai"),
                    config.mode == GameMode.VS_AI,
                    theme
                ) { controller.setMode(GameMode.VS_AI) }
            }
            if (controller.isVsAi) {
                ChipGroup(I18n.t("difficulty"), theme) {
                    Difficulty.entries.forEach { d ->
                        ChoiceChip(difficultyName(d), config.difficulty == d, theme) {
                            controller.setDifficulty(d)
                        }
                    }
                }
                ChipGroup(I18n.t("side"), theme) {
                    ChoiceChip(I18n.t("side_black"), controller.playerColor == Stone.BLACK, theme) {
                        controller.setPlayerStone(Stone.BLACK)
                    }
                    ChoiceChip(I18n.t("side_white"), controller.playerColor == Stone.WHITE, theme) {
                        controller.setPlayerStone(Stone.WHITE)
                    }
                }
            }
            ChipGroup(I18n.t("board"), theme) {
                GameConfig.BOARD_SIZES.forEach { size ->
                    ChoiceChip(I18n.t("board_size", "n" to "$size"), config.boardSize == size, theme) {
                        controller.setBoardSize(size)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(I18n.t("per_move_timer"), modifier = Modifier.weight(1f), fontSize = 13.sp, color = theme.textPrimary)
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

            ChipGroup(I18n.t("theme"), theme) {
                ThemeRegistry.themes.forEach { t ->
                    ChoiceChip(I18n.t(t.nameKey), t.id == theme.id, theme) { onThemeChange(t) }
                }
            }

            HorizontalDivider(color = theme.uiSurfaceVariant)

            Text(I18n.t("effects"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
            Text(
                I18n.t("effects_always_on"),
                fontSize = 11.sp,
                color = theme.textSecondary
            )

            HorizontalDivider(color = theme.uiSurfaceVariant)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { nav?.navigate(com.gomoku.nusv.ui.nav.Page.ACHIEVEMENTS) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(I18n.t("achievements"), fontSize = 13.sp)
                }
                Button(
                    onClick = { nav?.navigate(com.gomoku.nusv.ui.nav.Page.STATS) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(I18n.t("stats"), fontSize = 13.sp)
                }
            }

            HorizontalDivider(color = theme.uiSurfaceVariant)

            Text(I18n.t("game_info"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
            InfoRow(I18n.t("total_time"), formatClock(controller.totalSeconds), theme)
            InfoRow(I18n.t("move_count"), "${controller.moveHistory.size}", theme)
            InfoRow(I18n.t("current_turn"), stoneName(controller.currentStone), theme)
        }
    }
}

@Composable
private fun TurnIndicator(controller: GameController, theme: BoardTheme) {
    val playerLabel = if (controller.isVsAi) {
        I18n.t("you_vs_ai", "player" to stoneName(controller.playerColor))
    } else {
        I18n.t("pvp_label")
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(playerLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            StoneDot(controller.currentStone, theme)
            Spacer(Modifier.width(8.dp))
            Text(
                if (controller.isVsAi) {
                    if (controller.currentStone == controller.playerColor) I18n.t("your_turn") else I18n.t("ai_thinking")
                } else {
                    if (controller.currentStone == Stone.BLACK) I18n.t("black_turn") else I18n.t("white_turn")
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipGroup(title: String, theme: BoardTheme, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontSize = 12.sp, color = theme.textSecondary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
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
    if (!controller.showResultDialog) return
    val status = controller.status
    val (title, titleColor) = when (status) {
        GameStatus.BLACK_WIN -> I18n.t("black_wins") to Color(0xFF2E7D32)
        GameStatus.WHITE_WIN -> I18n.t("white_wins") to Color(0xFFC62828)
        GameStatus.DRAW -> I18n.t("draw") to theme.textSecondary
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
                            isDraw -> I18n.t("draw_with_ai")
                            isPlayerWin -> I18n.t("you_beat_ai")
                            else -> I18n.t("ai_won")
                        },
                        color = theme.textPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    Text(
                        if (isDraw) {
                            I18n.t("draw_both")
                        } else {
                            val winnerStone = if (status == GameStatus.BLACK_WIN) Stone.BLACK else Stone.WHITE
                            I18n.t("congrats", "winner" to stoneName(winnerStone))
                        },
                        color = theme.textPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { controller.showResultDialog = false; controller.restart() }) {
                Text(I18n.t("play_again"))
            }
        },
        dismissButton = {
            TextButton(onClick = { controller.showResultDialog = false }) {
                Text(I18n.t("close"))
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
        title = { Text(I18n.t("resign_title"), color = theme.textPrimary) },
        text = { Text(I18n.t("resign_body"), color = theme.textSecondary) },
        confirmButton = {
            Button(onClick = controller::confirmResign) {
                Text(I18n.t("confirm_resign"))
            }
        },
        dismissButton = {
            TextButton(onClick = controller::cancelResign) { Text(I18n.t("cancel")) }
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
        title = { Text(I18n.t("resume_title"), color = theme.textPrimary) },
        text = {
            Text(
                I18n.t(
                    "resume_body",
                    "size" to "${saved.boardSize}",
                    "moves" to "${saved.history.size}",
                    "mode" to I18n.t(if (saved.modeName == "PVP") "mode_pvp" else "mode_ai")
                ),
                color = theme.textSecondary
            )
        },
        confirmButton = {
            Button(onClick = { controller.continueSavedGame(saved) }) {
                Text(I18n.t("resume_continue"))
            }
        },
        dismissButton = {
            TextButton(onClick = controller::discardSavedGame) { Text(I18n.t("resume_discard")) }
        }
    )
}

private fun stoneName(stone: Stone): String =
    I18n.t(if (stone == Stone.BLACK) "stone_black" else "stone_white")

private fun difficultyName(d: Difficulty): String = I18n.t(
    when (d) {
        Difficulty.EASY -> "diff_easy"
        Difficulty.MEDIUM -> "diff_medium"
        Difficulty.HARD -> "diff_hard"
    }
)
