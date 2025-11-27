package com.example.tictactoeapp.data.model

import com.google.gson.annotations.SerializedName

data class GameBoardDto(
    @SerializedName("board")
    val board: Array<IntArray>? = null,

    @SerializedName("size")
    val size: Int = 3
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameBoardDto

        if (board != null) {
            if (other.board == null) return false
            if (!board.contentDeepEquals(other.board)) return false
        } else if (other.board != null) {
            return false
        }

        return size == other.size
    }

    override fun hashCode(): Int {
        var result = board?.contentDeepHashCode() ?: 0
        result = 31 * result + size
        return result
    }
}