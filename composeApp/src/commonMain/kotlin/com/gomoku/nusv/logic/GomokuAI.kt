package com.gomoku.nusv.logic

import com.gomoku.nusv.model.Board
import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.model.Position
import com.gomoku.nusv.model.Stone
import com.gomoku.nusv.platformAiTimeLimitMs
import com.gomoku.nusv.platformMaxAiDepth
import kotlin.random.Random

object GomokuAI {

    private const val FIVE = 10_000_000
    private const val BLOCKED_ALL = 10

    private val OPEN_TWO_SCORES = intArrayOf(0, 100, 1_000, 10_000, 200_000)
    private val BLOCKED_ONE_SCORES = intArrayOf(0, 20, 200, 2_000, 20_000)

    private val dirs = arrayOf(
        intArrayOf(0, 1),
        intArrayOf(1, 0),
        intArrayOf(1, 1),
        intArrayOf(1, -1)
    )

    data class AiMove(val position: Position, val timedOut: Boolean)

    fun bestMove(board: Board, stone: Stone, difficulty: Difficulty, rng: Random = Random.Default): AiMove {
        val work = Board(board.size)
        work.copyFrom(board)
        val moves = candidateMoves(work, radius = 2)
        if (moves.isEmpty()) {
            val center = work.size / 2
            return AiMove(Position(center, center), timedOut = false)
        }
        if (work.cells.all { it == 0 }) {
            return AiMove(Position(work.size / 2, work.size / 2), timedOut = false)
        }

        val depth = minOf(difficulty.searchDepth, platformMaxAiDepth())
        return when (difficulty) {
            Difficulty.EASY -> AiMove(greedyMove(work, stone, moves, rng), timedOut = false)
            Difficulty.MEDIUM -> searchMove(work, stone, moves, depth = minOf(3, depth))
            Difficulty.HARD -> searchMove(work, stone, moves, depth = depth)
        }
    }

    private fun greedyMove(
        board: Board,
        stone: Stone,
        moves: List<Position>,
        rng: Random
    ): Position {
        var best = moves[0]
        var bestScore = Int.MIN_VALUE
        val scores = IntArray(moves.size)
        for (i in moves.indices) {
            val m = moves[i]
            board.set(m.row, m.col, stone)
            val s = evaluateMove(board, m, stone)
            board.cells[board.index(m.row, m.col)] = 0
            scores[i] = s
            if (s > bestScore) {
                bestScore = s
                best = m
            }
        }
        val top = moves.filterIndexed { i, _ -> scores[i] >= bestScore - 3000 }
        return top[rng.nextInt(top.size)]
    }

    internal fun searchMove(board: Board, stone: Stone, moves: List<Position>, depth: Int): AiMove {
        val clock = AiClock(limitMs = platformAiTimeLimitMs())
        var bestMove = moves[0]
        var bestScore = Int.MIN_VALUE
        var timedOut = false
        for (d in 1..depth) {
            val completed = try {
                val (move, score) = searchDepth(board, stone, moves, d, clock)
                bestMove = move
                bestScore = score
                true
            } catch (_: AiTimeout) {
                timedOut = true
                false
            }
            if (!completed) break
            if (bestScore >= FIVE + depth) break
        }
        return AiMove(bestMove, timedOut)
    }

    private fun searchDepth(
        board: Board,
        stone: Stone,
        moves: List<Position>,
        depth: Int,
        clock: AiClock
    ): Pair<Position, Int> {
        val sorted = moves.sortedByDescending { m ->
            board.set(m.row, m.col, stone)
            val s = evaluateMove(board, m, stone)
            board.cells[board.index(m.row, m.col)] = 0
            s
        }
        val limited = sorted.take(12)

        var bestMove = limited[0]
        var bestScore = Int.MIN_VALUE
        for (m in limited) {
            clock.check()
            board.set(m.row, m.col, stone)
            val s = if (isImmediateWin(board, m.row, m.col, stone)) {
                FIVE + depth
            } else {
                -alphaBeta(board, stone.opponent, depth - 1, Int.MIN_VALUE + 1, Int.MAX_VALUE - 1, stone, clock)
            }
            board.cells[board.index(m.row, m.col)] = 0
            if (s > bestScore) {
                bestScore = s
                bestMove = m
            }
        }
        return bestMove to bestScore
    }

    internal fun alphaBeta(
        board: Board,
        stone: Stone,
        depth: Int,
        alphaIn: Int,
        betaIn: Int,
        rootStone: Stone,
        clock: AiClock = noTimeoutClock
    ): Int {
        if (depth == 0) return evaluateBoard(board, rootStone)

        val moves = candidateMoves(board, radius = 1)
        if (moves.isEmpty()) return 0

        val sorted = moves.sortedByDescending { m ->
            board.set(m.row, m.col, stone)
            val s = evaluateMove(board, m, stone)
            board.cells[board.index(m.row, m.col)] = 0
            s
        }
        val limited = sorted.take(10)

        var alpha = alphaIn
        var beta = betaIn
        var best = Int.MIN_VALUE + 1

        for (m in limited) {
            clock.check()
            board.set(m.row, m.col, stone)
            val s = if (isImmediateWin(board, m.row, m.col, stone)) {
                FIVE + depth
            } else {
                val child = -alphaBeta(board, stone.opponent, depth - 1, -beta, -alpha, rootStone, clock)
                child
            }
            board.cells[board.index(m.row, m.col)] = 0

            if (s > best) best = s
            if (best > alpha) alpha = best
            if (alpha >= beta) break
        }
        return best
    }

    private class AiTimeout : Exception()

    internal class AiClock(val limitMs: Long) {
        private val start = kotlin.time.TimeSource.Monotonic.markNow()
        private var nodes = 0L
        fun check() {
            if (limitMs >= Long.MAX_VALUE) return
            if (++nodes and 0x3F == 0L) {
                if (start.elapsedNow().inWholeMilliseconds > limitMs) throw AiTimeout()
            }
        }
    }

    private val noTimeoutClock = AiClock(Long.MAX_VALUE)

    private fun isImmediateWin(board: Board, row: Int, col: Int, stone: Stone): Boolean =
        WinChecker.hasFive(board, row, col, stone)

    internal fun evaluateMove(board: Board, m: Position, stone: Stone): Int {
        var score = evaluateBoard(board, stone)
        val opp = stone.opponent
        var fourThreats = 0
        var threeThreats = 0
        for ((dr, dc) in dirs) {
            val (cnt, open) = countLine(board, m, stone, dr, dc)
            when {
                cnt >= WinChecker.WIN_COUNT -> score += FIVE
                cnt == 4 && open >= 1 -> {
                    score += FIVE / 2
                    fourThreats++
                }
                cnt == 3 && open == 2 -> {
                    score += FIVE / 3
                    threeThreats++
                }
            }
            val (oppCnt, _) = countLine(board, m, opp, dr, dc)
            if (oppCnt >= WinChecker.WIN_COUNT - 1) score += FIVE / 3
        }
        if (fourThreats + threeThreats >= 2) score += FIVE / 2
        return score
    }

    private fun countLine(
        board: Board,
        m: Position,
        stone: Stone,
        dr: Int,
        dc: Int
    ): Pair<Int, Int> {
        var count = 1
        var open = 0
        var r = m.row + dr
        var c = m.col + dc
        while (board.get(r, c) == stone.id) {
            count++
            r += dr
            c += dc
        }
        if (board.get(r, c) == 0) open++
        r = m.row - dr
        c = m.col - dc
        while (board.get(r, c) == stone.id) {
            count++
            r -= dr
            c -= dc
        }
        if (board.get(r, c) == 0) open++
        return count to open
    }

    fun evaluateBoard(board: Board, rootStone: Stone): Int {
        var score = 0
        for (r in 0 until board.size) {
            for (c in 0 until board.size) {
                val id = board.get(r, c)
                if (id == 0) continue
                val stone = Stone.fromId(id) ?: continue
                val multiplier = if (stone == rootStone) 1 else -1
                score += multiplier * pointScore(board, r, c, stone)
            }
        }
        val myThreats = countThreats(board, rootStone)
        val oppThreats = countThreats(board, rootStone.opponent)
        if (myThreats >= 2) score += FIVE / 4
        if (oppThreats >= 2) score -= FIVE / 4
        return score
    }

    private fun countThreats(board: Board, stone: Stone): Int {
        var threats = 0
        for (r in 0 until board.size) {
            for (c in 0 until board.size) {
                if (board.get(r, c) != stone.id) continue
                for ((dr, dc) in dirs) {
                    val prevR = r - dr
                    val prevC = c - dc
                    if (board.get(prevR, prevC) == stone.id) continue

                    var count = 0
                    var rr = r
                    var cc = c
                    while (board.get(rr, cc) == stone.id) {
                        count++
                        rr += dr
                        cc += dc
                    }
                    if (count < 3) continue
                    var open = 0
                    if (board.get(rr, cc) == 0) open++
                    val endR = r - dr
                    val endC = c - dc
                    if (board.get(endR, endC) == 0) open++
                    if (count >= 5) return Int.MAX_VALUE / 4
                    if (count == 4 && open >= 1) threats++
                    if (count == 3 && open == 2) threats++
                }
            }
        }
        return threats
    }

    fun pointScore(board: Board, row: Int, col: Int, stone: Stone): Int {
        var score = 0
        for ((dr, dc) in dirs) {
            val prevR = row - dr
            val prevC = col - dc
            if (board.get(prevR, prevC) == stone.id) continue

            var count = 0
            var r = row
            var c = col
            while (board.get(r, c) == stone.id) {
                count++
                r += dr
                c += dc
            }
            if (count >= WinChecker.WIN_COUNT) {
                score += FIVE
                continue
            }
            var open = 0
            if (board.get(r, c) == 0) open++
            val endR = row - dr
            val endC = col - dc
            if (board.get(endR, endC) == 0) open++

            score += when (open) {
                2 -> OPEN_TWO_SCORES[count]
                1 -> BLOCKED_ONE_SCORES[count]
                else -> BLOCKED_ALL * count
            }
        }
        return score
    }

    fun candidateMoves(board: Board, radius: Int): List<Position> {
        val result = ArrayList<Position>()
        val seen = HashSet<Int>()
        for (r in 0 until board.size) {
            for (c in 0 until board.size) {
                if (board.get(r, c) == 0) continue
                for (dr in -radius..radius) {
                    for (dc in -radius..radius) {
                        if (dr == 0 && dc == 0) continue
                        val nr = r + dr
                        val nc = c + dc
                        if (nr in 0 until board.size && nc in 0 until board.size && board.isEmpty(nr, nc)) {
                            if (seen.add(board.index(nr, nc))) {
                                result.add(Position(nr, nc))
                            }
                        }
                    }
                }
            }
        }
        val center = board.size / 2
        return result.sortedBy { p ->
            val dr = p.row - center
            val dc = p.col - center
            dr * dr + dc * dc
        }
    }
}
