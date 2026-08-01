package com.gomoku.nusv

import com.gomoku.nusv.logic.GomokuAI
import com.gomoku.nusv.model.Board
import com.gomoku.nusv.model.Stone
import kotlin.test.Test

class BenchTest {
    @Test
    fun bench() {
        val board = Board(15)
        val mids = listOf(
            7 to 7, 7 to 8, 8 to 8, 8 to 9, 6 to 6,
            9 to 9, 6 to 8, 5 to 5, 6 to 5, 9 to 7
        )
        mids.forEachIndexed { i, (r, c) ->
            if (i % 2 == 0) board.set(r, c, Stone.BLACK) else board.set(r, c, Stone.WHITE)
        }
        println("--- mid-game board, ${mids.size} stones ---")
        for (depth in listOf(3, 4, 5, 6)) {
            val t0 = System.currentTimeMillis()
            val moves = GomokuAI.candidateMoves(board, radius = 2)
            val result = GomokuAI.searchMove(board, Stone.BLACK, moves, depth)
            val elapsed = System.currentTimeMillis() - t0
            println("depth=$depth -> ${elapsed}ms move=(${result.position.row},${result.position.col}) timedOut=${result.timedOut}")
        }
        println("platformMaxAiDepth=${platformMaxAiDepth()}")
    }
}
