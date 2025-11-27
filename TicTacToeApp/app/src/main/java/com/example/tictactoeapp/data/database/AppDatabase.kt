package com.example.tictactoeapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tictactoeapp.data.database.entity.GameEntity
import com.example.tictactoeapp.data.database.entity.UserEntity
import com.example.tictactoeapp.data.database.entity.CurrentUserEntity

@Database(
    entities = [GameEntity::class, UserEntity::class, CurrentUserEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(GameConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao
    abstract fun userDao(): UserDao
    abstract fun currentUserDao(): CurrentUserDao
}