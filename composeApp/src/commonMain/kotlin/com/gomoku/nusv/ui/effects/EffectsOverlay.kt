package com.gomoku.nusv.ui.effects

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import com.gomoku.nusv.model.Position
import com.gomoku.nusv.ui.theme.BoardTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private class ParticleEngine {
    class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        val maxLife: Float,
        val color: Color,
        val size: Float
    )

    class Ring(
        val x: Float,
        val y: Float,
        var radius: Float,
        val maxRadius: Float,
        var life: Float,
        val maxLife: Float,
        val color: Color
    )

    val particles = mutableListOf<Particle>()
    val rings = mutableListOf<Ring>()
    private val rng = Random(System.nanoTime())

    fun burst(x: Float, y: Float, colors: List<Color>, count: Int, speed: Float) {
        repeat(count) {
            val angle = rng.nextFloat() * 2f * PI.toFloat()
            val v = speed * (0.3f + rng.nextFloat() * 0.9f)
            val maxLife = 0.7f + rng.nextFloat() * 0.6f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * v,
                    vy = sin(angle) * v,
                    life = maxLife,
                    maxLife = maxLife,
                    color = colors[rng.nextInt(colors.size)],
                    size = 2.5f + rng.nextFloat() * 3.5f
                )
            )
        }
    }

    fun burstAlongLine(start: Offset, end: Offset, colors: List<Color>, count: Int, speed: Float) {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val len = sqrt(dx * dx + dy * dy)
        repeat(count) {
            val t = rng.nextFloat()
            val maxLife = 0.7f + rng.nextFloat() * 0.5f
            particles.add(
                Particle(
                    x = start.x + dx * t,
                    y = start.y + dy * t,
                    vx = dx / len * speed * (0.4f + rng.nextFloat() * 0.8f) + (rng.nextFloat() - 0.5f) * 60f,
                    vy = dy / len * speed * (0.4f + rng.nextFloat() * 0.8f) + (rng.nextFloat() - 0.5f) * 60f,
                    life = maxLife,
                    maxLife = maxLife,
                    color = colors[rng.nextInt(colors.size)],
                    size = 2.5f + rng.nextFloat() * 3.5f
                )
            )
        }
    }

    fun ripple(x: Float, y: Float, color: Color, maxRadius: Float) {
        rings.add(Ring(x, y, radius = 3f, maxRadius = maxRadius, life = 1.0f, maxLife = 1.0f, color = color))
        rings.add(Ring(x, y, radius = 3f, maxRadius = maxRadius * 0.65f, life = 0.7f, maxLife = 0.7f, color = color))
        rings.add(Ring(x, y, radius = 3f, maxRadius = maxRadius * 0.35f, life = 0.45f, maxLife = 0.45f, color = Color.White))
    }

    fun update(dt: Float) {
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.life -= dt
            if (p.life <= 0f) {
                iter.remove()
                continue
            }
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vx *= 1f - 1.8f * dt
            p.vy *= 1f - 1.8f * dt
            p.vy += 26f * dt
        }
        val ringIter = rings.iterator()
        while (ringIter.hasNext()) {
            val r = ringIter.next()
            r.life -= dt
            if (r.life <= 0f) {
                ringIter.remove()
                continue
            }
            val progress = 1f - r.life / r.maxLife
            r.radius = 3f + (r.maxRadius - 3f) * FastOutSlowInEasing.transform(progress)
        }
    }

    fun draw(drawScope: DrawScope) {
        drawScope.apply {
            for (r in rings) {
                val alpha = r.life / r.maxLife
                drawCircle(
                    color = r.color.copy(alpha = alpha * 0.8f),
                    radius = r.radius,
                    center = Offset(r.x, r.y),
                    style = Stroke(width = 5f * alpha + 2f)
                )
            }
            for (p in particles) {
                val alpha = p.life / p.maxLife
                drawCircle(
                    color = p.color.copy(alpha = alpha * 0.35f),
                    radius = p.size * 3.5f,
                    center = Offset(p.x, p.y)
                )
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size,
                    center = Offset(p.x, p.y)
                )
            }
        }
    }

    fun clear() {
        particles.clear()
        rings.clear()
    }
}

private data class BoardLayout(val cell: Float, val origin: Offset)

private fun boardLayout(size: Size, boardSize: Int): BoardLayout {
    val side = min(size.width, size.height)
    val cell = side / (boardSize + 1)
    val origin = Offset((size.width - side) / 2 + cell, (size.height - side) / 2 + cell)
    return BoardLayout(cell, origin)
}

private fun BoardLayout.center(row: Int, col: Int): Offset =
    Offset(origin.x + col * cell, origin.y + row * cell)

/**
 * 科幻特效层，叠加在棋盘上方。
 * 仅渲染已购买且启用的特效（enabledEffects）。
 */
@Composable
fun EffectsOverlay(
    boardSize: Int,
    theme: BoardTheme,
    lastMove: Position?,
    boardVersion: Int,
    enabledEffects: List<String>,
    winningLine: List<Pair<Int, Int>>?,
    modifier: Modifier = Modifier
) {
    val engine = remember { ParticleEngine() }
    val rippleOn = "ripple" in enabledEffects
    val starfieldOn = "starfield" in enabledEffects
    val hologramOn = "hologram" in enabledEffects
    val neonOn = "neon" in enabledEffects

    var tick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { nanos ->
                engine.update(nanos / 1_000_000_000f)
                tick++
            }
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val maxW = with(density) { maxWidth.toPx() }
        val maxH = with(density) { maxHeight.toPx() }
        val layout = remember(maxW, maxH, boardSize) {
            boardLayout(Size(maxW, maxH), boardSize)
        }

        LaunchedEffect(boardVersion) {
            if (boardVersion > 0 && lastMove != null) {
                val c = layout.center(lastMove.row, lastMove.col)
                if (starfieldOn) {
                    engine.burst(
                        c.x, c.y,
                        colors = listOf(theme.accent, Color.White, Color(0xFF7FE7FF)),
                        count = 42,
                        speed = 240f
                    )
                }
                if (rippleOn) engine.ripple(c.x, c.y, theme.accent, layout.cell * 7f)
            }
        }
        LaunchedEffect(winningLine) {
            if (winningLine != null && winningLine.size >= 2 && starfieldOn) {
                engine.burstAlongLine(
                    start = layout.center(winningLine.first().first, winningLine.first().second),
                    end = layout.center(winningLine.last().first, winningLine.last().second),
                    colors = listOf(theme.accent, Color.White, Color(0xFFFFD54F)),
                    count = 110,
                    speed = 300f
                )
            }
        }

        val infinite = rememberInfiniteTransition(label = "sci-fi")
        val sweep by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(4600, easing = FastOutSlowInEasing)),
            label = "sweep"
        )
        val neonFlow by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing)),
            label = "neon"
        )

        Canvas(Modifier.fillMaxSize()) {
            val frameTick = tick
            if (hologramOn) {
                val side = layout.cell * (boardSize + 1)
                val y = layout.origin.y - layout.cell + (size.height - 2 * layout.cell) * sweep
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            theme.accent.copy(alpha = 0.16f),
                            theme.accent.copy(alpha = 0.55f),
                            Color.White.copy(alpha = 0.95f),
                            theme.accent.copy(alpha = 0.55f),
                            theme.accent.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        startY = y - layout.cell * 2.2f,
                        endY = y + layout.cell * 2.2f
                    ),
                    topLeft = Offset(layout.origin.x - layout.cell, y - layout.cell * 2.2f),
                    size = Size(side, layout.cell * 4.4f)
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.95f),
                    start = Offset(layout.origin.x - layout.cell, y),
                    end = Offset(layout.origin.x + layout.cell * boardSize, y),
                    strokeWidth = 2.6f
                )
            }

            if (neonOn && winningLine != null && winningLine.size >= 2) {
                val start = layout.center(winningLine.first().first, winningLine.first().second)
                val end = layout.center(winningLine.last().first, winningLine.last().second)
                drawLine(
                    brush = Brush.linearGradient(
                        listOf(
                            Color.Transparent,
                            theme.accent.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.5f),
                            theme.accent.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    start = start,
                    end = end,
                    strokeWidth = layout.cell * 0.5f,
                    cap = StrokeCap.Round
                )
                repeat(4) { i ->
                    val t = (neonFlow * 1.25f + i / 4f) % 1f
                    val px = start.x + (end.x - start.x) * t
                    val py = start.y + (end.y - start.y) * t
                    val glow = (1f - ((neonFlow * 1.25f + i / 4f) % 1f))
                    drawCircle(
                        color = Color.White.copy(alpha = 0.55f + 0.4f * glow),
                        radius = layout.cell * 0.16f,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = theme.accent.copy(alpha = 0.3f),
                        radius = layout.cell * 0.4f,
                        center = Offset(px, py)
                    )
                }
            }

            engine.draw(this)
        }
    }
}
