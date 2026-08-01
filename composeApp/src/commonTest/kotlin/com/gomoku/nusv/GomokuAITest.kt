package com.gomoku.nusv

import com.gomoku.nusv.logic.GomokuAI
import com.gomoku.nusv.model.Board
import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.model.Stone
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GomokuAITest {

    private fun play(board: Board, vararg cells: Pair<Int, Int>, stone: Stone) {
        cells.forEach { (r, c) -> board.set(r, c, stone) }
    }

    @Test
    fun emptyBoardPlaysCenter() {
        val board = Board(15)
        val result = GomokuAI.bestMove(board, Stone.BLACK, Difficulty.MEDIUM); val move = result.position
        assertEquals(7, move.row)
        assertEquals(7, move.col)
    }

    @Test
    fun completesOwnFiveWhenAvailable() {
        val board = Board(15)
        play(board, 7 to 3, 7 to 4, 7 to 5, 7 to 6, stone = Stone.BLACK)
        val result = GomokuAI.bestMove(board, Stone.BLACK, Difficulty.MEDIUM); val move = result.position
        assertEquals(7, move.row)
        assertTrue(move.col in listOf(2, 7))
    }

    @Test
    fun blocksOpponentFourInRow() {
        val board = Board(15)
        play(board, 5 to 5, 5 to 6, 5 to 7, 5 to 8, stone = Stone.WHITE)
        val result = GomokuAI.bestMove(board, Stone.BLACK, Difficulty.HARD); val move = result.position
        assertTrue(move.row == 5 && move.col in listOf(4, 9))
    }

    @Test
    fun neverPlaysOnOccupiedCell() {
        val rng = Random(42)
        val board = Board(13)
        play(board, 6 to 6, 7 to 7, 6 to 8, stone = Stone.BLACK)
        repeat(20) {
            val result = GomokuAI.bestMove(board, Stone.WHITE, Difficulty.EASY, rng); val move = result.position
            assertEquals(0, board.get(move.row, move.col))
            board.set(move.row, move.col, Stone.WHITE)
            if (moveHistoryFull(board)) return
        }
    }

    @Test
    fun hardDifficultyWinsAgainstNoDefense() {
        val board = Board(15)
        play(board, 7 to 7, 7 to 8, 7 to 9, stone = Stone.BLACK)
        play(board, 8 to 8, 8 to 9, stone = Stone.WHITE)
        val result = GomokuAI.bestMove(board, Stone.BLACK, Difficulty.HARD); val move = result.position
        assertEquals(7, move.row)
        assertTrue(move.col in listOf(6, 10))
    }

    private fun moveHistoryFull(board: Board): Boolean = board.cells.all { it != 0 }

    @Test
    fun candidateMovesOnlyAroundStones() {
        val board = Board(15)
        board.set(7, 7, Stone.BLACK)
        val moves = GomokuAI.candidateMoves(board, radius = 1)
        assertTrue(moves.isNotEmpty())
        moves.forEach { assertTrue(it.row in 6..8 && it.col in 6..8) }
        assertNotEquals(moves.size, 225)
    }
}
