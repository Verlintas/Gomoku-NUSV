package com.gomoku.nusv.logic

import com.gomoku.nusv.model.Board
import com.gomoku.nusv.model.Stone

object WinChecker {

    private val directions = arrayOf(
        intArrayOf(0, 1),
        intArrayOf(1, 0),
        intArrayOf(1, 1),
        intArrayOf(1, -1)
    )

    const val WIN_COUNT = 5

    fun countInDirection(board: Board, row: Int, col: Int, stone: Stone, dr: Int, dc: Int): Int {
        var count = 1
        var r = row + dr
        var c = col + dc
        while (board.get(r, c) == stone.id) {
            count++
            r += dr
            c += dc
        }
        return count
    }

    fun hasFive(board: Board, row: Int, col: Int, stone: Stone): Boolean {
        for ((dr, dc) in directions) {
            val a = countInDirection(board, row, col, stone, -dr, -dc)
            val b = countInDirection(board, row, col, stone, dr, dc)
            if (a + b - 1 >= WIN_COUNT) return true
        }
        return false
    }

    fun winningLine(board: Board, row: Int, col: Int, stone: Stone): List<Pair<Int, Int>>? {
        for ((dr, dc) in directions) {
            val line = mutableListOf(row to col)
            var r = row + dr
            var c = col + dc
            while (board.get(r, c) == stone.id) {
                line.add(r to c)
                r += dr
                c += dc
            }
            r = row - dr
            c = col - dc
            while (board.get(r, c) == stone.id) {
                line.add(0, r to c)
                r -= dr
                c -= dc
            }
            if (line.size >= WIN_COUNT) return line
        }
        return null
    }
}
