// ./main/java/com/example/tictactoeapp/utils/TokenManager.kt
package com.example.tictactoeapp.utils

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val context: Context,
    private val jwtUtil: JwtUtil
) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("jwt_tokens", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
    }

    fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        expiresIn: Long,
        userId: Long? = null,
        username: String? = null
    ): Completable {
        return Completable.fromCallable {
            val expiryTime = System.currentTimeMillis() + (expiresIn * 1000)
            val editor = prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putLong(KEY_TOKEN_EXPIRY, expiryTime)

            // 👇 СОХРАНЯЕМ userId И username ТОЛЬКО ЕСЛИ ОНИ ПРЕДОСТАВЛЕНЫ
            userId?.let { editor.putLong(KEY_USER_ID, it) }
            username?.let { editor.putString(KEY_USERNAME, it) }

            editor.apply()

            println("🔄 DEBUG: Tokens saved - access: ${accessToken.take(10)}..., refresh: ${refreshToken.take(10)}...")
            println("🔄 DEBUG: Username saved: $username, UserId: $userId")
        }.subscribeOn(Schedulers.io())
    }

    fun getAccessToken(): Single<String?> {
        return Single.fromCallable {
            prefs.getString(KEY_ACCESS_TOKEN, null)
        }.subscribeOn(Schedulers.io())
    }

    fun getRefreshToken(): Single<String?> {
        return Single.fromCallable {
            prefs.getString(KEY_REFRESH_TOKEN, null)
        }.subscribeOn(Schedulers.io())
    }

    fun getUsername(): Single<String?> {
        return Single.fromCallable {
            prefs.getString(KEY_USERNAME, null)
        }.subscribeOn(Schedulers.io())
    }

    fun clearTokens(): Completable {
        return Completable.fromCallable {
            prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_TOKEN_EXPIRY)
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .apply()
            println("🔄 DEBUG: Tokens cleared")
        }.subscribeOn(Schedulers.io())
    }

    fun isTokenExpired(): Single<Boolean> {
        return Single.fromCallable {
            val expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
            jwtUtil.isTokenExpiredBasedOnSavedTime(expiryTime)
        }.subscribeOn(Schedulers.io())
    }

    fun isTokenExpiringSoon(): Single<Boolean> {
        return Single.fromCallable {
            val expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
            jwtUtil.isTokenExpiringSoon(expiryTime)
        }.subscribeOn(Schedulers.io())
    }


}