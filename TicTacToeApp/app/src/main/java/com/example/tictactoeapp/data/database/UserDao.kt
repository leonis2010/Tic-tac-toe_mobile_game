package com.example.tictactoeapp.data.database
import androidx.room.*
import com.example.tictactoeapp.data.database.entity.UserEntity
import io.reactivex.Single
import io.reactivex.Completable

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    fun getAllUsersRx(): Single<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserByIdRx(id: Long): Single<UserEntity>

    @Query("SELECT * FROM users WHERE username = :username")
    fun getUserByUsernameRx(username: String): Single<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserRx(user: UserEntity): Completable

    @Update
    fun updateUserRx(user: UserEntity): Completable

    @Delete
    fun deleteUserRx(user: UserEntity): Completable
}