package com.example.tictactoeapp.data.database
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GameConverters {
    private val gson = Gson()

    @TypeConverter
    fun boardToString(board: Array<IntArray>?): String {
        return gson.toJson(board)
    }

    @TypeConverter
    fun stringToBoard(data: String): Array<IntArray>? {
        val type = object : TypeToken<Array<IntArray>>() {}.type
        return gson.fromJson(data, type)
    }
}