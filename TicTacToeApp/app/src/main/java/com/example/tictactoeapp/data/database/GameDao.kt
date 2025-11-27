package com.example.tictactoeapp.data.database

import androidx.room.*
import com.example.tictactoeapp.data.database.entity.GameEntity
import java.util.*

import io.reactivex.Single
import io.reactivex.Completable

@Dao
interface GameDao {

    @Query("SELECT * FROM games")
    fun getAllGamesRx(): Single<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    fun getGameByIdRx(id: UUID): Single<GameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGameRx(game: GameEntity): Completable

    @Update
    fun updateGameRx(game: GameEntity): Completable

    @Delete
    fun deleteGameRx(game: GameEntity): Completable

    @Query("DELETE FROM games WHERE id = :id")
    fun deleteGameByIdRx(id: UUID): Completable
}