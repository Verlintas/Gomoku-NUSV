package com.gomoku.nusv

import com.gomoku.nusv.logic.GomokuAI
import com.gomoku.nusv.logic.WinChecker
import com.gomoku.nusv.model.Board
import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.model.Stone
import kotlin.test.Test
import kotlin.test.assertTrue

class AISelfPlayTest {

    @Test
    fun hardVsHardEndsQuickly() {
        val board = Board(15)
        var stone = Stone.BLACK
        val start = System.currentTimeMillis()
        var moves = 0
        var winner: Stone? = null
        repeat(100) {
            val t0 = System.currentTimeMillis()
            val result = GomokuAI.bestMove(board, stone, Difficulty.HARD)
            val move = result.position
            val elapsed = System.currentTimeMillis() - t0
            assertTrue(elapsed < 8000, "AI move too slow: ${elapsed}ms")
            board.set(move.row, move.col, stone)
            moves++
            if (WinChecker.hasFive(board, move.row, move.col, stone)) {
                winner = stone
                return@repeat
            }
            if (board.isFull()) return@repeat
            stone = stone.opponent
        }
        val total = System.currentTimeMillis() - start
        println("hard vs hard: moves=$moves winner=$winner totalTime=${total}ms avg=${total / moves}ms")
        assertTrue(moves < 130, "game should end before board fills, got $moves")
    }

    @Test
    fun mediumVsMediumEndsReasonable() {
        val board = Board(15)
        var stone = Stone.BLACK
        var moves = 0
        repeat(100) {
            val result = GomokuAI.bestMove(board, stone, Difficulty.MEDIUM); val move = result.position
            board.set(move.row, move.col, stone)
            moves++
            if (WinChecker.hasFive(board, move.row, move.col, stone)) return
            if (board.isFull()) return
            stone = stone.opponent
        }
        assertTrue(moves < 80)
    }
}
