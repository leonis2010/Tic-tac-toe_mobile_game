package com.example.tictactoeapp.di

import android.content.Context
import androidx.room.Room
import com.example.tictactoeapp.data.api.AuthInterceptor
import com.example.tictactoeapp.data.api.TicTacToeApi
import com.example.tictactoeapp.data.database.AppDatabase
import com.example.tictactoeapp.data.database.CurrentUserDao
import com.example.tictactoeapp.data.database.GameDao
import com.example.tictactoeapp.data.database.UserDao
import com.example.tictactoeapp.data.repository.NetworkGameService
import com.example.tictactoeapp.data.repository.DatabaseGameService
import com.example.tictactoeapp.domain.repository.impl.GameRepositoryImpl
import com.example.tictactoeapp.domain.repository.impl.UserRepositoryImpl
import com.example.tictactoeapp.domain.repository.impl.AuthRepositoryImpl
import com.example.tictactoeapp.domain.repository.GameRepository
import com.example.tictactoeapp.domain.repository.UserRepository
import com.example.tictactoeapp.domain.repository.AuthRepository
import com.example.tictactoeapp.mapper.*
import com.example.tictactoeapp.utils.JwtUtil
import com.example.tictactoeapp.utils.SessionManager
import com.example.tictactoeapp.utils.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Database
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tictactoe_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideGameDao(database: AppDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideCurrentUserDao(database: AppDatabase): CurrentUserDao {
        return database.currentUserDao()
    }

    // Network
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8088/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideTicTacToeApi(retrofit: Retrofit): TicTacToeApi {
        return retrofit.create(TicTacToeApi::class.java)
    }

    // Services
    @Provides
    @Singleton
    fun provideNetworkGameService(api: TicTacToeApi): NetworkGameService {
        return NetworkGameService(api)
    }

    @Provides
    @Singleton
    fun provideDatabaseGameService(
        gameDao: GameDao,
        userDao: UserDao,
        currentUserDao: CurrentUserDao
    ): DatabaseGameService {
        return DatabaseGameService(gameDao, userDao, currentUserDao)
    }

    // Mappers
    @Provides
    @Singleton
    fun provideDtoToDomainMapper(): DtoToDomainMapper {
        return DtoToDomainMapper()
    }

    @Provides
    @Singleton
    fun provideDomainToDtoMapper(): DomainToDtoMapper {
        return DomainToDtoMapper()
    }

    @Provides
    @Singleton
    fun provideEntityToDomainMapper(): EntityToDomainMapper {
        return EntityToDomainMapper()
    }

    @Provides
    @Singleton
    fun provideDomainToEntityMapper(): DomainToEntityMapper {
        return DomainToEntityMapper()
    }

    @Provides
    @Singleton
    fun provideViewDataToDomainMapper(): ViewDataToDomainMapper {
        return ViewDataToDomainMapper()
    }

    @Provides
    @Singleton
    fun provideDomainToViewDataMapper(): DomainToViewDataMapper {
        return DomainToViewDataMapper()
    }

    // Repositories
    @Provides
    @Singleton
    fun provideGameRepository(
        networkService: NetworkGameService,
        databaseService: DatabaseGameService,
        dtoToDomainMapper: DtoToDomainMapper,
        domainToDtoMapper: DomainToDtoMapper,
        domainToEntityMapper: DomainToEntityMapper,
        entityToDomainMapper: EntityToDomainMapper
    ): GameRepository {
        return GameRepositoryImpl(
            networkService,
            databaseService,
            dtoToDomainMapper,
            domainToDtoMapper,
            domainToEntityMapper,
            entityToDomainMapper
        )
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        databaseService: DatabaseGameService,
        entityToDomainMapper: EntityToDomainMapper
    ): UserRepository {
        return UserRepositoryImpl(databaseService, entityToDomainMapper)
    }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideJwtUtil(): JwtUtil {
        return JwtUtil()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenManager: TokenManager,
        jwtUtil: JwtUtil,
        apiProvider: Provider<TicTacToeApi>,
        sessionManager: SessionManager,
        @ApplicationContext context: Context
    ): AuthInterceptor {
        return AuthInterceptor(tokenManager, jwtUtil, apiProvider, sessionManager, context)
    }

    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context,
        jwtUtil: JwtUtil
    ): TokenManager {
        return TokenManager(context, jwtUtil)
    }

    // AppModule.kt
    @Provides
    @Singleton
    fun provideAuthRepository(
        api: TicTacToeApi,
        networkGameService: NetworkGameService,
        dtoToDomainMapper: DtoToDomainMapper,
        tokenManager: TokenManager
    ): AuthRepository {
        return AuthRepositoryImpl(
            api = api,
            networkGameService = networkGameService,
            tokenManager = tokenManager
        )
    }
}