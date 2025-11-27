package com.example.tictactoeapp.data.database.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.tictactoeapp.data.database.GameConverters
import java.util.*

@Entity(tableName = "games")
@TypeConverters(GameConverters::class)
data class GameEntity(
    @PrimaryKey
    val id: UUID,

    val boardData: String,

    val isPlayerTurn: Boolean,

    val status: String,

    val createdAt: Long = System.currentTimeMillis()
)