package com.example.tictactoeapp.data.database.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Long,

    val username: String,

    val password: String
)