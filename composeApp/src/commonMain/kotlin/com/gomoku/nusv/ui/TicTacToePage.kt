package com.gomoku.nusv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gomoku.nusv.i18n.I18n
import com.gomoku.nusv.model.Stone
import com.gomoku.nusv.ui.nav.NavController
import com.gomoku.nusv.ui.theme.BoardTheme
import com.gomoku.nusv.ui.theme.ThemeRegistry

private enum class TTTSide { X, O }

private class TTTGame {
    val cells = Array(9) { 0 } // 0 empty, 1 X, 2 O
    var current = TTTSide.X
    var over = false
    var winner = 0

    fun play(index: Int): Boolean {
        if (over || cells[index] != 0) return false
        cells[index] = if (current == TTTSide.X) 1 else 2
        checkEnd()
        if (!over) current = if (current == TTTSide.X) TTTSide.O else TTTSide.X
        return true
    }

    fun aiMove(): Int {
        // 优先获胜位，其次堵对方获胜位，否则中心/角/边
        for (i in 0..8) {
            if (cells[i] == 0) {
                cells[i] = if (current == TTTSide.X) 1 else 2
                if (hasWin(cells, cells[i])) {
                    cells[i] = 0
                    return i
                }
                cells[i] = 0
            }
        }
        val opp = if (current == TTTSide.X) 2 else 1
        for (i in 0..8) {
            if (cells[i] == 0) {
                cells[i] = opp
                if (hasWin(cells, opp)) {
                    cells[i] = 0
                    return i
                }
                cells[i] = 0
            }
        }
        if (cells[4] == 0) return 4
        val corners = listOf(0, 2, 6, 8).filter { cells[it] == 0 }
        if (corners.isNotEmpty()) return corners.first()
        return cells.indexOfFirst { it == 0 }
    }

    fun checkEnd() {
        val c = if (current == TTTSide.X) 1 else 2
        if (hasWin(cells, c)) {
            over = true
            winner = c
        } else if (cells.all { it != 0 }) {
            over = true
            winner = 0
        }
    }

    private fun hasWin(c: Array<Int>, v: Int): Boolean {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        return lines.any { l -> l.all { c[it] == v } }
    }
}

@Composable
fun TicTacToePage(
    controller: GameController,
    theme: BoardTheme,
    nav: NavController
) {
    var game by remember { mutableStateOf(TTTGame()) }
    var gameVersion by remember { mutableStateOf(0) }
    var vsAi by remember { mutableStateOf(true) }
    var aiThinking by remember { mutableStateOf(false) }

    fun refresh() {
        gameVersion++
    }

    fun afterMove() {
        refresh()
        if (!vsAi || game.over) return
        if (game.current == TTTSide.O) {
            aiThinking = true
            val move = game.aiMove()
            game.play(move)
            refresh()
            // 只有玩家（X）获胜才计入胜场；AI 获胜不计
            if (game.over && game.winner == 1) {
                controller.onMinigameWin()
            }
            aiThinking = false
        } else if (game.over && game.winner == 1) {
            controller.onMinigameWin()
        }
    }

    Surface(color = theme.uiBackground, modifier = Modifier.fillMaxSize()) {
        val refreshTick = gameVersion
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
                Text(
                    I18n.t("nav_minigame"),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    I18n.t("minigame_wins") + ": ${controller.profile.minigameWins}",
                    fontSize = 13.sp,
                    color = theme.textSecondary
                )
            }

            Text(
                when {
                    game.over && game.winner == 0 -> I18n.t("minigame_draw")
                    game.over && game.winner == 1 -> I18n.t("minigame_x_wins")
                    game.over -> I18n.t("minigame_o_wins")
                    vsAi && game.current == TTTSide.O -> I18n.t("minigame_ai_thinking")
                    else -> I18n.t("minigame_turn") + " ${if (game.current == TTTSide.X) "X" else "O"}"
                },
                fontSize = 15.sp,
                color = theme.textPrimary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { r ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(3) { c ->
                                val idx = r * 3 + c
                                val v = game.cells[idx]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(theme.uiSurface)
                                        .border(1.dp, theme.uiSurfaceVariant, RoundedCornerShape(10.dp))
                                        .clickable(enabled = !game.over && !aiThinking) {
                                            if (game.play(idx)) {
                                                afterMove()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        when (v) {
                                            1 -> "X"
                                            2 -> "O"
                                            else -> ""
                                        },
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (v) {
                                            1 -> theme.textPrimary
                                            2 -> theme.accent
                                            else -> Color.Transparent
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        game = TTTGame()
                        gameVersion++
                        aiThinking = false
                    }
                ) {
                    Text(I18n.t("restart"), fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = {
                        vsAi = !vsAi
                        game = TTTGame()
                        gameVersion++
                        aiThinking = false
                    }
                ) {
                    Text(if (vsAi) I18n.t("minigame_pvp_mode") else I18n.t("minigame_ai_mode"), fontSize = 13.sp)
                }
            }
        }
    }
}
