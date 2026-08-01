package com.gomoku.nusv

import com.gomoku.nusv.logic.GomokuAI
import com.gomoku.nusv.logic.WinChecker
import com.gomoku.nusv.model.Board
import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.model.Stone
import kotlin.test.Test
import kotlin.test.assertTrue

class TraceTest {
    @Test
    fun singleGame() {
        val board = Board(15)
        var stone = Stone.BLACK
        var slowest = 0L
        repeat(60) { i ->
            val t0 = System.currentTimeMillis()
            val result = GomokuAI.bestMove(board, stone, Difficulty.HARD); val move = result.position
            val elapsed = System.currentTimeMillis() - t0
            slowest = maxOf(slowest, elapsed)
            assertTrue(elapsed < 10_000, "move $i took ${elapsed}ms")
            board.set(move.row, move.col, stone)
            print("${if (stone == Stone.BLACK) "B" else "W"}(${move.row},${move.col}) ")
            if (i % 8 == 7) println()
            if (WinChecker.hasFive(board, move.row, move.col, stone)) {
                println("\nWIN by ${if (stone == Stone.BLACK) "BLACK" else "WHITE"} at move ${i + 1}, slowest=$slowest ms")
                return
            }
            if (board.isFull()) {
                println("\nDRAW, slowest=$slowest ms")
                return
            }
            stone = stone.opponent
        }
        println("\nNO WIN in 60 moves, slowest=$slowest ms")
    }
}
