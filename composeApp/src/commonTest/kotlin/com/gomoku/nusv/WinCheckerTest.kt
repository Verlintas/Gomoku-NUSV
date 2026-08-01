package com.gomoku.nusv

import com.gomoku.nusv.logic.WinChecker
import com.gomoku.nusv.model.Board
import com.gomoku.nusv.model.Position
import com.gomoku.nusv.model.Stone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WinCheckerTest {

    private fun boardWith(vararg stones: Triple<Int, Int, Stone>): Board {
        val size = 15
        val board = Board(size)
        stones.forEach { (r, c, s) -> board.set(r, c, s) }
        return board
    }

    @Test
    fun horizontalFiveWins() {
        val board = boardWith(
            Triple(7, 3, Stone.BLACK), Triple(7, 4, Stone.BLACK), Triple(7, 5, Stone.BLACK),
            Triple(7, 6, Stone.BLACK), Triple(7, 7, Stone.BLACK)
        )
        assertTrue(WinChecker.hasFive(board, 7, 5, Stone.BLACK))
        assertFalse(WinChecker.hasFive(board, 7, 5, Stone.WHITE))
    }

    @Test
    fun verticalFourDoesNotWin() {
        val board = boardWith(
            Triple(3, 9, Stone.WHITE), Triple(4, 9, Stone.WHITE),
            Triple(5, 9, Stone.WHITE), Triple(6, 9, Stone.WHITE)
        )
        assertFalse(WinChecker.hasFive(board, 5, 9, Stone.WHITE))
    }

    @Test
    fun diagonalFiveWins() {
        val board = boardWith(
            Triple(2, 2, Stone.BLACK), Triple(3, 3, Stone.BLACK), Triple(4, 4, Stone.BLACK),
            Triple(5, 5, Stone.BLACK), Triple(6, 6, Stone.BLACK)
        )
        assertTrue(WinChecker.hasFive(board, 4, 4, Stone.BLACK))
    }

    @Test
    fun antiDiagonalFiveWins() {
        val board = boardWith(
            Triple(2, 10, Stone.BLACK), Triple(3, 9, Stone.BLACK), Triple(4, 8, Stone.BLACK),
            Triple(5, 7, Stone.BLACK), Triple(6, 6, Stone.BLACK)
        )
        assertTrue(WinChecker.hasFive(board, 4, 8, Stone.BLACK))
    }

    @Test
    fun brokenLineDoesNotWin() {
        val board = boardWith(
            Triple(7, 3, Stone.BLACK), Triple(7, 4, Stone.BLACK), Triple(7, 6, Stone.BLACK),
            Triple(7, 7, Stone.BLACK), Triple(7, 8, Stone.BLACK)
        )
        assertFalse(WinChecker.hasFive(board, 7, 4, Stone.BLACK))
    }

    @Test
    fun winningLineReturnsFiveCells() {
        val board = boardWith(
            Triple(7, 3, Stone.BLACK), Triple(7, 4, Stone.BLACK), Triple(7, 5, Stone.BLACK),
            Triple(7, 6, Stone.BLACK), Triple(7, 7, Stone.BLACK)
        )
        val line = WinChecker.winningLine(board, 7, 5, Stone.BLACK)
        assertEquals(5, line?.size)
        assertEquals(Position(7, 3), line?.first()?.let { Position(it.first, it.second) })
    }
}
