package com.example.tictactoeapp.domain.repository.impl

import com.example.tictactoeapp.data.model.GameDto
import com.example.tictactoeapp.data.repository.NetworkGameService
import com.example.tictactoeapp.data.repository.DatabaseGameService
import com.example.tictactoeapp.domain.repository.GameRepository
import com.example.tictactoeapp.domain.model.Game
import com.example.tictactoeapp.domain.model.GameHistory
import com.example.tictactoeapp.domain.model.Leaderboard
import com.example.tictactoeapp.mapper.DtoToDomainMapper
import com.example.tictactoeapp.mapper.DomainToDtoMapper
import com.example.tictactoeapp.mapper.DomainToEntityMapper
import com.example.tictactoeapp.mapper.EntityToDomainMapper
import io.reactivex.Single
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.util.*

class GameRepositoryImpl(
    private val networkService: NetworkGameService,
    private val databaseService: DatabaseGameService,
    private val dtoToDomainMapper: DtoToDomainMapper,
    private val domainToDtoMapper: DomainToDtoMapper,
    private val domainToEntityMapper: DomainToEntityMapper,
    private val entityToDomainMapper: EntityToDomainMapper
) : GameRepository {

    override fun startNewGame(): Single<Game> {
        println("DEBUG: GameRepositoryImpl - startNewGame() called")

        return networkService.startNewGame()
            .subscribeOn(Schedulers.io())
            .doOnSuccess { gameDto ->
                println("DEBUG: GameRepositoryImpl - gameDto received: ${gameDto.id}")
            }
            .flatMap { gameDto ->
                println("DEBUG: GameRepositoryImpl - mapping gameDto to domain")
                val game = dtoToDomainMapper.mapGameDtoToDomain(gameDto)
                    ?: return@flatMap Single.error<Game>(Exception("Failed to map game"))

                println("DEBUG: GameRepositoryImpl - game mapped: ${game.id}")

                // Сохраняем локально в фоновом потоке
                val gameEntity = domainToEntityMapper.mapGameToEntity(game)
                return@flatMap if (gameEntity != null) {
                    println("DEBUG: GameRepositoryImpl - saving game to local DB")
                    databaseService.saveGameRx(gameEntity)
                        .subscribeOn(Schedulers.io())
                        .doOnComplete {
                            println("DEBUG: GameRepositoryImpl - game saved to local DB successfully")
                        }
                        .doOnError { error ->
                            println("DEBUG: GameRepositoryImpl - error saving to local DB: ${error.message}")
                        }
                        .andThen(Single.just(game))
                } else {
                    println("DEBUG: GameRepositoryImpl - gameEntity is null, returning game without saving")
                    Single.just(game)
                }
            }
            .doOnError { error ->
                println("DEBUG: GameRepositoryImpl - error in startNewGame: ${error.message}")
            }
    }

    override fun makeMove(gameId: UUID, game: Game): Single<Game> {
        println("DEBUG: GameRepositoryImpl - makeMove called for game: $gameId")

        val gameDto = domainToDtoMapper.mapGameToDto(game)
        println("DEBUG: GameRepositoryImpl - mapped to DTO: $gameDto")

        if (gameDto != null) {
            println("DEBUG: GameRepositoryImpl - Calling networkService.makeMove")
            return networkService.makeMove(gameId, gameDto)
                .flatMap { updatedDto ->
                    println("DEBUG: GameRepositoryImpl - Network response received: $updatedDto")
                    val updatedGame = dtoToDomainMapper.mapGameDtoToDomain(updatedDto)
                        ?: throw Exception("Failed to map updated game from DTO")

                    println("DEBUG: GameRepositoryImpl - Successfully mapped to domain: $updatedGame")

                    // Сохраняем локально и возвращаем игру
                    val gameEntity = domainToEntityMapper.mapGameToEntity(updatedGame)
                    if (gameEntity != null) {
                        println("DEBUG: GameRepositoryImpl - Saving to local DB")
                        databaseService.saveGameRx(gameEntity)
                            .subscribeOn(Schedulers.io())
                            .doOnComplete { println("DEBUG: Game saved locally") }
                            .doOnError { error ->
                                println("🔴 DEBUG: Failed to save locally: ${error.message}")
                            }
                            .onErrorComplete()
                            .andThen(Single.just(updatedGame))  // ← Возвращаем игру после сохранения
                    } else {
                        Single.just(updatedGame)  // ← Возвращаем игру даже если не сохранили
                    }
                }
                .doOnError { error ->
                    println("DEBUG: GameRepositoryImpl - Error in makeMove: ${error.message}")
                }
        } else {
            println("DEBUG: GameRepositoryImpl - Failed to map game to DTO")
            return Single.error(Exception("Failed to map game to DTO"))
        }
    }

    override fun getGame(gameId: UUID): Single<Game> {
        return networkService.getGame(gameId)
            .flatMap { gameDto ->
                val game = dtoToDomainMapper.mapGameDtoToDomain(gameDto)
                    ?: return@flatMap Single.error<Game>(Exception("Failed to map game from network"))
                // Сохраняем локально, но не прерываем цепочку при ошибке сохранения
                val gameEntity = domainToEntityMapper.mapGameToEntity(game)
                if (gameEntity != null) {
                    databaseService.saveGameRx(gameEntity)
                        .onErrorComplete() // Игнорируем ошибки сохранения
                        .andThen(Single.just(game)) // Всегда возвращаем game
                } else {
                    Single.just(game) // Возвращаем game даже если не удалось сохранить
                }
            }
            .onErrorResumeNext { error ->
                // Пробуем локальную базу только при сетевой ошибке
                getLocalGame(gameId)
                    .toSingle() //Maybe в Single
                    .onErrorReturn { throwable ->
                        throw Exception("Game not found locally: ${error.message}")
                    }
            }
    }

    override fun saveGameLocally(game: Game): Completable {
        val gameEntity = domainToEntityMapper.mapGameToEntity(game)
        if (gameEntity != null) {
            return databaseService.saveGameRx(gameEntity) // <-- Возвращает Completable
        } else {
            return Completable.error(Exception("Failed to map game to entity"))
        }
    }

    override fun getLocalGame(gameId: UUID): Maybe<Game> {
        return databaseService.getGameByIdRx(gameId)
            .flatMapMaybe { gameEntity ->
                val game = entityToDomainMapper.mapGameEntityToDomain(gameEntity)
                if (game != null) {
                    Maybe.just(game)
                } else {
                    Maybe.empty()
                }
            }
            .onErrorResumeNext(Maybe.empty<Game>()) // ← Явно указываем тип для устранения неоднозначности
    }

    override fun getAvailableGames(): Single<List<Game>> {
        println("🟡 DEBUG: GameRepositoryImpl - getAvailableGames() called")

        return networkService.getGames()
            .subscribeOn(Schedulers.io())
            .doOnSuccess { gameDtos ->
                println("🟢 DEBUG: GameRepositoryImpl - getAvailableGames network success: ${gameDtos.size} games")
                gameDtos.forEach { dto ->
                    println("🔵 DEBUG: Game DTO: id=${dto.id}, creator=${dto.creatorUsername}, status=${dto.status}")
                }
            }
            .doOnError { error ->
                println("🔴 DEBUG: GameRepositoryImpl - getAvailableGames network error: ${error.message}")
            }
            .flatMap { gameDtos ->
                val games = gameDtos.mapNotNull { dtoToDomainMapper.mapGameDtoToDomain(it) }
                println("🟢 DEBUG: GameRepositoryImpl - mapped ${games.size} games to domain")

                Completable.concat(
                    games.map { game ->
                        val gameEntity = domainToEntityMapper.mapGameToEntity(game)
                        if (gameEntity != null) {
                            databaseService.saveGameRx(gameEntity)
                                .subscribeOn(Schedulers.io())
                        } else {
                            Completable.complete()
                        }
                    }
                )
                    .subscribeOn(Schedulers.io())
                    .toSingleDefault(games)
            }
    }

    // ...
    override fun joinGame(gameId: UUID): Single<Game> {
        return networkService.joinGameRx(gameId)
            .subscribeOn(Schedulers.io())
            .doOnSuccess { gameDto -> println("DEBUG: GameRepositoryImpl - joinGame DTO received: ${gameDto.id}, status: ${gameDto.status}") }
            .flatMap { gameDto: GameDto -> // <-- Укажите тип явно
                println("DEBUG: GameRepositoryImpl - mapping joined game DTO to domain")
                val game = dtoToDomainMapper.mapGameDtoToDomain(gameDto)
                    ?: return@flatMap Single.error<Game>(Exception("Failed to map joined game from DTO"))
                println("DEBUG: GameRepositoryImpl - joined game mapped: ${game.id}")

                val gameEntity = domainToEntityMapper.mapGameToEntity(game)
                return@flatMap if (gameEntity != null) {
                    println("DEBUG: GameRepositoryImpl - saving joined game to local DB")
                    databaseService.saveGameRx(gameEntity)
                        .subscribeOn(Schedulers.io())
                        .doOnComplete { println("DEBUG: GameRepositoryImpl - joined game saved to local DB successfully") }
                        .doOnError { error -> println("DEBUG: GameRepositoryImpl - error saving joined game to local DB: ${error.message}") }
                        .andThen(Single.just(game))
                } else {
                    println("DEBUG: GameRepositoryImpl - gameEntity is null for joined game, returning without saving")
                    Single.just(game) // Возвращаем game без сохранения
                }
            }
            .doOnError { error -> println("DEBUG: GameRepositoryImpl - error in joinGame: ${error.message}") }
    }

    override fun startNewGameWithComputer(): Single<Game> {
        return networkService.startNewGameWithComputer()
            .map { gameDto ->
                dtoToDomainMapper.mapGameDtoToDomain(gameDto)
                    ?: throw Exception("Failed to map game")
            }
    }

    override fun startNewGameWithPlayer(): Single<Game> {
        return networkService.startNewGameWithPlayer()
            .map { gameDto ->
                dtoToDomainMapper.mapGameDtoToDomain(gameDto)
                    ?: throw Exception("Failed to map game")
            }
    }

    override fun playerLeftGame(gameId: UUID): Single<Game> {
        println("DEBUG: GameRepositoryImpl - playerLeftGame called for: $gameId")
        return networkService.playerLeftGame(gameId)
            .map { gameDto ->
                println("DEBUG: GameRepositoryImpl - playerLeftGame response received")
                dtoToDomainMapper.mapGameDtoToDomain(gameDto)
                    ?: throw Exception("Failed to map game from DTO")
            }
            .doOnSuccess { game ->
                println("DEBUG: GameRepositoryImpl - playerLeftGame SUCCESS: ${game.id}, status: ${game.status}")
            }
            .doOnError { error ->
                println("DEBUG: GameRepositoryImpl - playerLeftGame ERROR: ${error.message}")
            }
    }

    override fun getGameHistory(): Single<List<GameHistory>> {
        println("🟡 DEBUG: GameRepositoryImpl - getGameHistory() called")

        return networkService.getGameHistory()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { historyDtos ->
                val history = historyDtos.mapNotNull { dto ->
                    dtoToDomainMapper.mapGameHistoryDtoToDomain(dto)
                }
                println("🟢 DEBUG: GameRepositoryImpl - mapped ${history.size} history items")
                history
            }
            .doOnError { error ->
                println("🔴 DEBUG: GameRepositoryImpl - getGameHistory ERROR: ${error.message}")
            }
    }

    override fun getLeaderboard(): Single<List<Leaderboard>> {
        println("🟡 DEBUG: GameRepositoryImpl - getLeaderboard() called")

        return networkService.getLeaderboard()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { leaderboardDtos ->
                val leaderboard = leaderboardDtos.mapNotNull { dtoToDomainMapper.mapLeaderboardDtoToDomain(it) }
                println("🟢 DEBUG: GameRepositoryImpl - mapped ${leaderboard.size} leaderboard items")
                leaderboard
            }
            .doOnError { error ->
                println("🔴 DEBUG: GameRepositoryImpl - getLeaderboard ERROR: ${error.message}")
            }
    }

}