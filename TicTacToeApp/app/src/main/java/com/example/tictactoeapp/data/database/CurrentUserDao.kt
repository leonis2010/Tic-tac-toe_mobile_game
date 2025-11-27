package com.example.tictactoeapp.data.database
import androidx.room.*
import com.example.tictactoeapp.data.database.entity.CurrentUserEntity

import io.reactivex.Completable
import io.reactivex.Maybe

@Dao
interface CurrentUserDao {

    @Query("SELECT * FROM current_user LIMIT 1")
    fun getCurrentUserRx(): Maybe<CurrentUserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCurrentUserRx(user: CurrentUserEntity): Completable

    @Query("DELETE FROM current_user")
    fun clearCurrentUserRx(): Completable
}