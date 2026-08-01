package com.gomoku.nusv.model

enum class Stone(val id: Int, val displayName: String) {
    BLACK(1, "黑棋"),
    WHITE(2, "白棋");

    val opponent: Stone get() = if (this == BLACK) WHITE else BLACK

    companion object {
        fun fromId(id: Int): Stone? = entries.find { it.id == id }
    }
}

data class Position(val row: Int, val col: Int)

data class Move(val pos: Position, val stone: Stone)

class Board(val size: Int) {

    val cells = IntArray(size * size)

    fun index(row: Int, col: Int) = row * size + col

    fun get(row: Int, col: Int): Int {
        if (row !in 0 until size || col !in 0 until size) return -1
        return cells[index(row, col)]
    }

    fun set(row: Int, col: Int, stone: Stone) {
        cells[index(row, col)] = stone.id
    }

    fun clear() {
        cells.fill(0)
    }

    fun isOccupied(row: Int, col: Int): Boolean = get(row, col) != 0

    fun isEmpty(row: Int, col: Int): Boolean = get(row, col) == 0

    fun stoneAt(row: Int, col: Int): Stone? = Stone.fromId(get(row, col))

    fun isFull(): Boolean = cells.all { it != 0 }

    fun emptyPositions(): List<Position> {
        val list = ArrayList<Position>(size * size)
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (cells[index(r, c)] == 0) list.add(Position(r, c))
            }
        }
        return list
    }

    fun copyFrom(other: Board) {
        check(size == other.size)
        other.cells.copyInto(cells)
    }

    fun snapshot(): IntArray = cells.clone()
}
