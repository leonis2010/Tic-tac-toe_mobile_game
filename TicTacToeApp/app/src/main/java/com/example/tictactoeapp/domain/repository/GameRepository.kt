package com.example.tictactoeapp.domain.repository

import com.example.tictactoeapp.domain.model.Game
import com.example.tictactoeapp.domain.model.GameHistory
import com.example.tictactoeapp.domain.model.Leaderboard
import io.reactivex.Single
import io.reactivex.Completable
import io.reactivex.Maybe
import java.util.*

interface GameRepository {
    fun startNewGame(): Single<Game>
    fun makeMove(gameId: UUID, game: Game): Single<Game>
    fun getGame(gameId: UUID): Single<Game>
    fun saveGameLocally(game: Game): Completable
    fun getLocalGame(gameId: UUID): Maybe<Game>
    fun getAvailableGames(): Single<List<Game>>
    fun joinGame(gameId: UUID): Single<Game>
    fun startNewGameWithComputer(): Single<Game>
    fun startNewGameWithPlayer(): Single<Game>
    fun playerLeftGame(gameId: UUID): Single<Game>
    fun getGameHistory(): Single<List<GameHistory>>
    fun getLeaderboard(): Single<List<Leaderboard>>
}