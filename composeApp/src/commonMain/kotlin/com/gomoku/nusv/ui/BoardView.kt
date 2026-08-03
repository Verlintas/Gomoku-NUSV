package com.gomoku.nusv.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.gomoku.nusv.model.Board
import com.gomoku.nusv.model.Position
import com.gomoku.nusv.model.Stone
import com.gomoku.nusv.ui.theme.BoardTheme

@Composable
fun BoardView(
    board: Board,
    theme: BoardTheme,
    lastMove: Position?,
    isPlayerTurn: Boolean,
    hintStone: Stone?,
    boardVersion: Int,
    winningLine: List<Pair<Int, Int>>? = null,
    hint: Position? = null,
    glowColor: Color? = null,
    winLineColors: Pair<Color, Color>? = null,
    onCellTap: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hintPulse = remember { Animatable(0f) }
    LaunchedEffect(hint) {
        if (hint != null) {
            while (true) {
                hintPulse.snapTo(0f)
                hintPulse.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
            }
        }
    }
    val version = boardVersion
    var hover by remember { mutableStateOf<Offset?>(null) }
    val dropScale = remember { Animatable(1f) }
    LaunchedEffect(version, lastMove) {
        if (lastMove != null) {
            dropScale.snapTo(0.2f)
            dropScale.animateTo(1f, tween(160, easing = FastOutSlowInEasing))
        }
    }

    fun boardLayout(size: Size): Triple<Float, Offset, Int> {
        val side = minOf(size.width, size.height)
        val cell = side / (board.size + 1)
        val origin = Offset((size.width - side) / 2 + cell, (size.height - side) / 2 + cell)
        return Triple(cell, origin, board.size)
    }

    fun hitTest(pos: Offset, cell: Float, origin: Offset): Pair<Int, Int>? {
        val c = ((pos.x - origin.x) / cell).toInt()
        val r = ((pos.y - origin.y) / cell).toInt()
        if (r in 0 until board.size && c in 0 until board.size) return r to c
        return null
    }

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(board.size) {
                detectTapGestures { offset ->
                    val layout = boardLayout(Size(this.size.width.toFloat(), this.size.height.toFloat()))
                    hitTest(offset, layout.first, layout.second)?.let { (r, c) ->
                        onCellTap(r, c)
                    }
                }
            }
            .pointerInput(board.size) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        hover = event.changes.firstOrNull()?.position
                    }
                }
            }
    ) {
        val (cell, origin, _) = boardLayout(size)
        val side = (board.size - 1) * cell

        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(theme.boardColor, theme.boardColor.copy(alpha = 0.9f))
            ),
            size = Size(side + cell * 2, side + cell * 2),
            topLeft = Offset(origin.x - cell, origin.y - cell),
            cornerRadius = CornerRadius(12f, 12f)
        )

        val lineWidth = cell * 0.045f
        for (i in 0 until board.size) {
            val y = origin.y + i * cell
            drawLine(theme.gridColor, Offset(origin.x, y), Offset(origin.x + side, y), lineWidth)
            val x = origin.x + i * cell
            drawLine(theme.gridColor, Offset(x, origin.y), Offset(x, origin.y + side), lineWidth)
        }

        val starRows = listOf(3, board.size / 2, board.size - 4).distinct()
        for (r in starRows) {
            for (c in starRows) {
                drawCircle(
                    color = theme.starColor,
                    radius = cell * 0.075f,
                    center = Offset(origin.x + c * cell, origin.y + r * cell)
                )
            }
        }

        val lastIndex = lastMove?.let { board.index(it.row, it.col) }
        for (i in board.cells.indices) {
            val id = board.cells[i]
            if (id == 0) continue
            val r = i / board.size
            val c = i % board.size
            val center = Offset(origin.x + c * cell, origin.y + r * cell)
            val scale = if (i == lastIndex) dropScale.value else 1f
            drawStone(center, cell, Stone.fromId(id) ?: continue, theme, scale, glowColor)
        }

        lastMove?.let {
            drawCircle(
                color = theme.lastMoveMark,
                radius = cell * 0.095f,
                center = Offset(origin.x + it.col * cell, origin.y + it.row * cell)
            )
        }

        hint?.let { h ->
            if (board.isEmpty(h.row, h.col)) {
                val center = Offset(origin.x + h.col * cell, origin.y + h.row * cell)
                val r = cell * 0.42f * (1f + hintPulse.value * 0.25f)
                drawCircle(
                    color = theme.accent.copy(alpha = 0.5f + 0.5f * hintPulse.value),
                    radius = r,
                    center = center,
                    style = Stroke(width = cell * 0.08f)
                )
                drawCircle(
                    color = theme.accent.copy(alpha = 0.18f),
                    radius = r * 1.15f,
                    center = center
                )
            }
        }

        winningLine?.let { line ->
            if (line.size >= 2) {
                val start = Offset(origin.x + line.first().second * cell, origin.y + line.first().first * cell)
                val end = Offset(origin.x + line.last().second * cell, origin.y + line.last().first * cell)
                val linePair = winLineColors ?: (theme.accent to theme.lastMoveMark)
                drawLine(
                    brush = Brush.linearGradient(
                        listOf(linePair.first.copy(alpha = 0.9f), linePair.second.copy(alpha = 0.9f))
                    ),
                    start = start,
                    end = end,
                    strokeWidth = cell * 0.28f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = start,
                    end = end,
                    strokeWidth = cell * 0.08f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        hover?.let { pos ->
            if (isPlayerTurn) {
                hitTest(pos, cell, origin)?.let { (r, c) ->
                    if (board.isEmpty(r, c)) {
                        val stone = hintStone ?: Stone.BLACK
                        val color = if (stone == Stone.BLACK) theme.blackStone else theme.whiteStone
                        drawCircle(
                            color = color.copy(alpha = 0.4f),
                            radius = cell * 0.4f,
                            center = Offset(origin.x + c * cell, origin.y + r * cell)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStone(
    center: Offset,
    cell: Float,
    stone: Stone,
    theme: BoardTheme,
    scale: Float,
    glowColor: Color?
) {
    val radius = cell * 0.42f * scale
    if (radius <= 0f) return
    glowColor?.let { glow ->
        drawCircle(
            color = glow.copy(alpha = 0.35f),
            radius = radius * 1.45f,
            center = center
        )
        drawCircle(
            color = glow.copy(alpha = 0.18f),
            radius = radius * 1.9f,
            center = center
        )
    }
    drawCircle(
        color = Color.Black.copy(alpha = 0.25f),
        radius = radius,
        center = center + Offset(cell * 0.03f, cell * 0.04f)
    )
    if (stone == Stone.BLACK) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF5A5A5A), theme.blackStone),
                center = center - Offset(radius * 0.35f, radius * 0.35f),
                radius = radius * 1.4f
            ),
            radius = radius,
            center = center
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.18f),
            radius = radius * 0.42f,
            center = center - Offset(radius * 0.32f, radius * 0.32f)
        )
    } else {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color(0xFFCFCFCF)),
                center = center - Offset(radius * 0.35f, radius * 0.35f),
                radius = radius * 1.4f
            ),
            radius = radius,
            center = center
        )
        drawCircle(
            color = Color(0xFF9A9A9A).copy(alpha = 0.5f),
            radius = radius,
            center = center,
            style = Stroke(width = cell * 0.035f)
        )
    }
}
