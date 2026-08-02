package com.gomoku.nusv.logic

import com.gomoku.nusv.model.Board
import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.model.Position
import com.gomoku.nusv.model.Stone
import com.gomoku.nusv.platformAiTimeLimitMs
import com.gomoku.nusv.platformMaxAiDepth
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * 五子棋 AI：威胁空间搜索（threat-space search）。
 * 借鉴 NUSV-Lite mini app 的成熟思路：
 * - 强制着法（urgent move）优先：立即胜 / 堵立即胜 / 形成活四 / 智能堵活四
 * - 有强制着法时只展开该着法（极大提升搜索深度有效性）
 * - 增量评估（deltaEval）降低搜索成本
 * - 评分表区分活/眠威胁梯度
 * - 时间预算截止（deadline），超时截断并返回 timedOut 标记
 */
object GomokuAI {

    private const val FIVE = 10_000_000
    private const val OPEN_FOUR = 1_000_000
    private const val FOUR = 100_000
    private const val OPEN_THREE = 50_000
    private const val THREE = 5_000
    private const val OPEN_TWO = 1_000
    private const val TWO = 100
    private const val OPEN_ONE = 50
    private const val ONE = 10

    private inline fun setCell(board: Board, row: Int, col: Int, id: Int) {
        board.cells[board.index(row, col)] = id
    }

    private val dirs = arrayOf(
        intArrayOf(1, 0),
        intArrayOf(0, 1),
        intArrayOf(1, 1),
        intArrayOf(1, -1)
    )

    data class AiMove(val position: Position, val timedOut: Boolean)

    // ---------- 评分 ----------

    private fun lineScore(count: Int, openEnds: Int): Int = when {
        count >= 5 -> FIVE
        count == 4 -> when (openEnds) { 2 -> OPEN_FOUR; 1 -> FOUR; else -> 0 }
        count == 3 -> when (openEnds) { 2 -> OPEN_THREE; 1 -> THREE; else -> 0 }
        count == 2 -> when (openEnds) { 2 -> OPEN_TWO; 1 -> TWO; else -> 0 }
        count == 1 -> when (openEnds) { 2 -> OPEN_ONE; 1 -> ONE; else -> 0 }
        else -> 0
    }

    /** 在 (row,col) 落 player 子后的局部得分（4 方向线段分） */
    private fun scorePoint(board: Board, row: Int, col: Int, player: Int): Int {
        var score = 0
        for ((dr, dc) in dirs) {
            var count = 1
            var openEnds = 0
            var r = row + dr
            var c = col + dc
            while (board.get(r, c) == player) {
                count++
                r += dr
                c += dc
            }
            if (board.get(r, c) == 0) openEnds++
            r = row - dr
            c = col - dc
            while (board.get(r, c) == player) {
                count++
                r -= dr
                c -= dc
            }
            if (board.get(r, c) == 0) openEnds++
            score += lineScore(count, openEnds)
        }
        return score
    }

    fun pointScore(board: Board, row: Int, col: Int, stone: Stone): Int =
        scorePoint(board, row, col, stone.id)

    /** 全盘静态评估（rootStone 视角），仅用于公开 API 与测试 */
    fun evaluateBoard(board: Board, rootStone: Stone): Int {
        var score = 0L
        for (r in 0 until board.size) {
            for (c in 0 until board.size) {
                val id = board.get(r, c)
                if (id == 0) continue
                for ((dr, dc) in dirs) {
                    val br = r - dr
                    val bc = c - dc
                    if (board.get(br, bc) == id) continue
                    var count = 1
                    var openEnds = 0
                    var rr = r + dr
                    var cc = c + dc
                    while (board.get(rr, cc) == id) {
                        count++
                        rr += dr
                        cc += dc
                    }
                    if (board.get(rr, cc) == 0) openEnds++
                    if (board.get(br, bc) == 0) openEnds++
                    val s = lineScore(count, openEnds)
                    if (id == rootStone.id) score += s else score -= s
                }
            }
        }
        return score.toInt().coerceIn(Int.MIN_VALUE / 2, Int.MAX_VALUE / 2)
    }

    /** 增量评估：落子 (row,col) 后 p 视角在该点四条线的得分变化 */
    private fun deltaEval(board: Board, row: Int, col: Int, p: Int, me: Int): Long {
        var total = 0L
        val ownStone = board.get(row, col) == p
        for ((dr, dc) in dirs) {
            var s = 0L
            if (ownStone) {
                var count = 1
                var opens = 0
                var r = row + dr
                var c = col + dc
                while (board.get(r, c) == p) {
                    count++
                    r += dr
                    c += dc
                }
                if (board.get(r, c) == 0) opens++
                r = row - dr
                c = col - dc
                while (board.get(r, c) == p) {
                    count++
                    r -= dr
                    c -= dc
                }
                if (board.get(r, c) == 0) opens++
                s += lineScore(count, opens).toLong()
            }
            // 该点在方向 dr,dc 上的"挂靠段"（不含自身）
            var f = 1
            var countF = 0
            while (board.get(row + dr * f, col + dc * f) == p) {
                countF++
                f++
            }
            if (countF >= 1) {
                var opensF = 0
                if (board.get(row + dr * f, col + dc * f) == 0) opensF++
                val oldScore = lineScore(countF, opensF + 1).toLong()
                val newScore = if (ownStone) 0L else lineScore(countF, opensF).toLong()
                s += newScore - oldScore
            }
            var b = 1
            var countB = 0
            while (board.get(row - dr * b, col - dc * b) == p) {
                countB++
                b++
            }
            if (countB >= 1) {
                var opensB = 0
                if (board.get(row - dr * b, col - dc * b) == 0) opensB++
                val oldScore = lineScore(countB, opensB + 1).toLong()
                val newScore = if (ownStone) 0L else lineScore(countB, opensB).toLong()
                s += newScore - oldScore
            }
            total += s
        }
        return if (p == me) total else -total
    }

    // ---------- 威胁检测 ----------

    /** 在 idx 落 player 子是否立即五连 */
    private fun canWinAt(board: Board, row: Int, col: Int, player: Int): Boolean =
        WinChecker.hasFive(board, row, col, Stone.fromId(player)!!)

    private fun winMoves(board: Board, player: Int, empties: List<Position>): List<Position> {
        val res = mutableListOf<Position>()
        for (m in empties) {
            setCell(board, m.row, m.col, player)
            if (WinChecker.hasFive(board, m.row, m.col, Stone.fromId(player)!!)) res.add(m)
            board.cells[board.index(m.row, m.col)] = 0
        }
        return res
    }

    /** 在 idx 落 player 子后是否形成活四（两端皆空） */
    private fun isOpenFourAt(board: Board, row: Int, col: Int, player: Int): Boolean {
        for ((dr, dc) in dirs) {
            var count = 1
            var f = 1
            while (board.get(row + dr * f, col + dc * f) == player) {
                count++
                f++
            }
            var b = 1
            while (board.get(row - dr * b, col - dc * b) == player) {
                count++
                b++
            }
            if (count == 4) {
                if (board.get(row + dr * f, col + dc * f) == 0 && board.get(row - dr * b, col - dc * b) == 0) {
                    return true
                }
            }
        }
        return false
    }

    private fun fourMoves(board: Board, player: Int, empties: List<Position>): List<Position> {
        val res = mutableListOf<Position>()
        for (m in empties) {
            if (isOpenFourAt(board, m.row, m.col, player)) res.add(m)
        }
        return res
    }

    /** 选"堵完后对手剩余活四威胁最少"的堵点（防双威胁） */
    private fun bestBlock(board: Board, other: Int, threats: List<Position>): Position {
        var best = threats.first()
        var bestRemaining = Int.MAX_VALUE
        for (idx in threats.distinct()) {
            setCell(board, idx.row, idx.col, 3 - other)
            var remaining = 0
            for (t in threats) {
                if (t == idx || board.isOccupied(t.row, t.col)) continue
                if (isOpenFourAt(board, t.row, t.col, other)) remaining++
            }
            board.cells[board.index(idx.row, idx.col)] = 0
            if (remaining < bestRemaining) {
                bestRemaining = remaining
                best = idx
            }
        }
        return best
    }

    /** 强制着法：立即胜 → 堵立即胜 → 形成活四 → 智能堵活四 */
    private fun urgentMove(board: Board, mover: Int, other: Int, empties: List<Position>): Position? {
        winMoves(board, mover, empties).firstOrNull()?.let { return it }
        winMoves(board, other, empties).firstOrNull()?.let { return it }
        fourMoves(board, mover, empties).firstOrNull()?.let { return it }
        val otherFours = fourMoves(board, other, empties)
        if (otherFours.isNotEmpty()) return bestBlock(board, other, otherFours)
        return null
    }

    // ---------- 候选 ----------

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

    private fun nearCandidates(board: Board): List<Position> = candidateMoves(board, radius = 2)

    private fun topCandidates(board: Board, player: Int, other: Int, n: Int): List<Position> =
        nearCandidates(board)
            .sortedByDescending { idx ->
                scorePoint(board, idx.row, idx.col, player).toLong() +
                    scorePoint(board, idx.row, idx.col, other)
            }
            .take(n)

    // ---------- 搜索 ----------

    private class Deadline(val limitNanos: Long) {
        private val start = System.nanoTime()
        fun over(): Boolean = System.nanoTime() > start + limitNanos
    }

    private fun searchMove(
        board: Board,
        me: Int,
        opponent: Int,
        mover: Int,
        depth: Int,
        branch: Int,
        totalEval: Long,
        alpha: Long,
        beta: Long,
        deadline: Deadline
    ): Long {
        val other = if (mover == me) opponent else me
        if (depth == 0) {
            // 深度耗尽后继续展开强制着法链（VCF 风格），避免 horizon 效应
            val forced = urgentMove(board, other, mover, nearCandidates(board))
            if (forced != null) {
                setCell(board, forced.row, forced.col, other)
                val total = totalEval +
                    deltaEval(board, forced.row, forced.col, other, me) +
                    deltaEval(board, forced.row, forced.col, mover, me)
                val s = searchMove(board, me, opponent, mover, 3, 1, total, alpha, beta, deadline)
                board.cells[board.index(forced.row, forced.col)] = 0
                return s
            }
            return totalEval
        }
        val empties = nearCandidates(board)
        if (empties.isEmpty()) return totalEval
        val forced = urgentMove(board, mover, other, empties)
        val candidates = if (forced != null) listOf(forced) else topCandidates(board, mover, other, branch)
        if (candidates.isEmpty()) return totalEval
        if (deadline.over()) return totalEval

        if (mover == me) {
            var best = Long.MIN_VALUE
            var a = alpha
            for (idx in candidates) {
                setCell(board, idx.row, idx.col, mover)
                val total = totalEval +
                    deltaEval(board, idx.row, idx.col, mover, me) +
                    deltaEval(board, idx.row, idx.col, other, me)
                val s = searchMove(board, me, opponent, other, depth - 1, branch, total, a, beta, deadline)
                board.cells[board.index(idx.row, idx.col)] = 0
                if (s > best) best = s
                if (best > a) a = best
                if (a >= beta) break
            }
            return best
        } else {
            var worst = Long.MAX_VALUE
            var b = beta
            for (idx in candidates) {
                setCell(board, idx.row, idx.col, mover)
                val total = totalEval +
                    deltaEval(board, idx.row, idx.col, mover, me) +
                    deltaEval(board, idx.row, idx.col, other, me)
                val s = searchMove(board, me, opponent, other, depth - 1, branch, total, alpha, b, deadline)
                board.cells[board.index(idx.row, idx.col)] = 0
                if (s < worst) worst = s
                if (worst < b) b = worst
                if (b <= alpha) break
            }
            return worst
        }
    }

    // ---------- 入口 ----------

    fun bestMove(board: Board, stone: Stone, difficulty: Difficulty, rng: Random = Random.Default): AiMove {
        val work = Board(board.size)
        work.copyFrom(board)
        val me = stone.id
        val opponent = 3 - me

        val empty = nearCandidates(work)
        if (empty.isEmpty()) return AiMove(Position(work.size / 2, work.size / 2), timedOut = false)
        if (work.cells.all { it == 0 }) {
            return AiMove(Position(work.size / 2, work.size / 2), timedOut = false)
        }

        // 根层强制着法（立即胜/堵胜/活四/堵活四）
        urgentMove(work, me, opponent, empty)?.let {
            return AiMove(it, timedOut = false)
        }

        return when (difficulty) {
            Difficulty.EASY -> AiMove(easyMove(work, me, opponent, rng), timedOut = false)
            Difficulty.MEDIUM, Difficulty.HARD -> {
                val depthCap = min(difficulty.searchDepth, platformMaxAiDepth())
                val search = findBestMove(work, me, opponent, depthCap, difficulty)
                AiMove(search.first, search.second)
            }
        }
    }

    private fun easyMove(board: Board, me: Int, opponent: Int, rng: Random): Position {
        val empty = nearCandidates(board)
        var best = empty.first()
        var bestScore = Long.MIN_VALUE
        for (idx in empty) {
            val attack = scorePoint(board, idx.row, idx.col, me).toLong()
            val defense = scorePoint(board, idx.row, idx.col, opponent).toLong() / 2
            val total = attack + defense + rng.nextLong(-500, 500)
            if (total > bestScore) {
                bestScore = total
                best = idx
            }
        }
        return best
    }

    private fun findBestMove(
        board: Board,
        me: Int,
        opponent: Int,
        depthCap: Int,
        difficulty: Difficulty
    ): Pair<Position, Boolean> {
        val ordered = nearCandidates(board)
            .sortedByDescending { idx ->
                scorePoint(board, idx.row, idx.col, me).toLong() +
                    scorePoint(board, idx.row, idx.col, opponent)
            }
        val initEval = evaluateBoardLong(board, me)
        val deadline = Deadline(platformAiTimeLimitMs() * 1_000_000L)
        var bestIdx = ordered.first()
        var bestScore = Long.MIN_VALUE
        var timedOut = false
        val branch = if (difficulty == Difficulty.HARD) 6 else 5

        for (idx in ordered) {
            if (deadline.over()) {
                timedOut = true
                break
            }
            setCell(board, idx.row, idx.col, me)
            val total = initEval +
                deltaEval(board, idx.row, idx.col, me, me) +
                deltaEval(board, idx.row, idx.col, opponent, me)
            val s = searchMove(
                board, me, opponent, opponent, depthCap, branch, total,
                Long.MIN_VALUE, Long.MAX_VALUE, deadline
            )
            board.cells[board.index(idx.row, idx.col)] = 0
            if (s > bestScore) {
                bestScore = s
                bestIdx = idx
            }
        }
        return bestIdx to timedOut
    }

    private fun evaluateBoardLong(board: Board, me: Int): Long {
        var score = 0L
        for (r in 0 until board.size) {
            for (c in 0 until board.size) {
                val id = board.get(r, c)
                if (id == 0) continue
                for ((dr, dc) in dirs) {
                    val br = r - dr
                    val bc = c - dc
                    if (board.get(br, bc) == id) continue
                    var count = 1
                    var openEnds = 0
                    var rr = r + dr
                    var cc = c + dc
                    while (board.get(rr, cc) == id) {
                        count++
                        rr += dr
                        cc += dc
                    }
                    if (board.get(rr, cc) == 0) openEnds++
                    if (board.get(br, bc) == 0) openEnds++
                    val s = lineScore(count, openEnds).toLong()
                    if (id == me) score += s else score -= s
                }
            }
        }
        return score
    }

    // ---------- 兼容旧测试 API ----------

    internal fun searchMove(board: Board, stone: Stone, moves: List<Position>, depth: Int): AiMove {
        val me = stone.id
        val opponent = 3 - me
        val deadline = Deadline(platformAiTimeLimitMs() * 1_000_000L)
        var best = moves.first()
        var bestScore = Long.MIN_VALUE
        var timedOut = false
        val initEval = evaluateBoardLong(board, me)
        for (idx in moves) {
            if (deadline.over()) {
                timedOut = true
                break
            }
            setCell(board, idx.row, idx.col, me)
            val total = initEval +
                deltaEval(board, idx.row, idx.col, me, me) +
                deltaEval(board, idx.row, idx.col, opponent, me)
            val s = searchMove(
                board, me, opponent, opponent, depth, 5, total,
                Long.MIN_VALUE, Long.MAX_VALUE, deadline
            )
            board.cells[board.index(idx.row, idx.col)] = 0
            if (s > bestScore) {
                bestScore = s
                best = idx
            }
        }
        return AiMove(best, timedOut)
    }

    internal fun evaluateMove(board: Board, m: Position, stone: Stone): Int =
        evaluateBoard(board, stone)
}
