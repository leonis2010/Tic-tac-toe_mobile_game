package com.example.tictactoeapp.data.database.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_user")
data class CurrentUserEntity(
    @PrimaryKey
    val id: Int = 1,

    val userId: Long,

    val username: String,

    val authToken: String? = null
)