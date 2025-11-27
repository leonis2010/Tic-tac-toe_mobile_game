package com.example.tictactoeapp.domain.repository.impl

import com.example.tictactoeapp.data.repository.DatabaseGameService
import com.example.tictactoeapp.domain.repository.UserRepository
import com.example.tictactoeapp.domain.model.User
import com.example.tictactoeapp.mapper.EntityToDomainMapper
import com.example.tictactoeapp.data.database.entity.CurrentUserEntity
import io.reactivex.Completable
import io.reactivex.Maybe

class UserRepositoryImpl(
    private val databaseService: DatabaseGameService,
    private val entityToDomainMapper: EntityToDomainMapper
) : UserRepository {

    override fun getCurrentUser(): Maybe<User> {
        return databaseService.getCurrentUserRx()
            .flatMap { currentUserEntity: CurrentUserEntity ->
                val user = entityToDomainMapper.mapCurrentUserEntityToDomain(currentUserEntity)
                if (user != null) {
                    Maybe.just(user)
                } else {
                    Maybe.empty<User>()
                }
            }
    }

    override fun saveCurrentUser(user: User, authToken: String): Completable {
        val currentUserEntity = CurrentUserEntity(
            userId = user.id ?: 0L,
            username = user.username,
            authToken = user.accessToken ?: authToken
        )
        return databaseService.saveCurrentUserRx(currentUserEntity)
    }

    override fun clearCurrentUser(): Completable {
        return databaseService.clearCurrentUserRx()
    }

    override fun clearAllData(): Completable {
        return databaseService.clearAllDataRx()
    }
}