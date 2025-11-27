package com.example.tictactoeapp.domain.model

data class GameBoard(
    val board: Array<IntArray>,
    val size: Int = 3
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameBoard

        if (!board.contentDeepEquals(other.board)) return false
        return size == other.size
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + size
        return result
    }
}