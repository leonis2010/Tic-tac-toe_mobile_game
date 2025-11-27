// ./src/main/java/com/example/tictactoeapp/data/repository/DatabaseGameService.kt
package com.example.tictactoeapp.data.repository

import com.example.tictactoeapp.data.database.CurrentUserDao
import com.example.tictactoeapp.data.database.GameDao
import com.example.tictactoeapp.data.database.UserDao
import com.example.tictactoeapp.data.database.entity.CurrentUserEntity
import com.example.tictactoeapp.data.database.entity.GameEntity

import java.util.*

import io.reactivex.Single
import io.reactivex.Completable
import io.reactivex.Maybe

class DatabaseGameService(
    private val gameDao: GameDao,
    private val userDao: UserDao,
    private val currentUserDao: CurrentUserDao
) {
    fun saveGameRx(gameEntity: GameEntity): Completable {
        return gameDao.insertGameRx(gameEntity)
    }

    fun getGameByIdRx(id: UUID): Single<GameEntity> {
        return gameDao.getGameByIdRx(id)
    }

    fun getAllGamesRx(): Single<List<GameEntity>> {
        return gameDao.getAllGamesRx()
    }

    fun saveCurrentUserRx(currentUserEntity: CurrentUserEntity): Completable {
        return currentUserDao.insertCurrentUserRx(currentUserEntity)
    }

    fun getCurrentUserRx(): Maybe<CurrentUserEntity> {
        return currentUserDao.getCurrentUserRx()
    }

    fun clearCurrentUserRx(): Completable {
        return currentUserDao.clearCurrentUserRx()
    }

    fun clearAllDataRx(): Completable {
        return gameDao.getAllGamesRx()
            .flatMapCompletable { games ->
                Completable.concat(
                    games.map { game -> gameDao.deleteGameRx(game) }
                )
            }
            .andThen(userDao.getAllUsersRx())
            .flatMapCompletable { users ->
                Completable.concat(
                    users.map { user -> userDao.deleteUserRx(user) }
                )
            }
            .andThen(currentUserDao.clearCurrentUserRx())
    }
}