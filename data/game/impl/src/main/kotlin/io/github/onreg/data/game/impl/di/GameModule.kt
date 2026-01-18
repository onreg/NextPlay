package io.github.onreg.data.game.impl.di

import androidx.paging.PagingConfig
import androidx.paging.RemoteMediator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.db.TransactionProvider
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.dao.GameRemoteKeysDao
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.network.rawg.api.GameApi
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.game.impl.GameRepositoryImpl
import io.github.onreg.data.game.impl.mapper.GameDtoMapper
import io.github.onreg.data.game.impl.mapper.GameDtoMapperImpl
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import io.github.onreg.data.game.impl.mapper.GameEntityMapperImpl
import io.github.onreg.data.game.impl.paging.GameRemoteMediator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public abstract class GameModule {
    @Binds
    @Singleton
    public abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    @Binds
    public abstract fun bindGameEntityMapper(impl: GameEntityMapperImpl): GameEntityMapper

    @Binds
    public abstract fun bindGameDtoMapper(impl: GameDtoMapperImpl): GameDtoMapper

    public companion object {
        @Provides
        @Singleton
        public fun providePagingConfig(): PagingConfig = PagingConfig(
            pageSize = 10,
            prefetchDistance = 3,
            initialLoadSize = 20,
            maxSize = 200,
            enablePlaceholders = false,
        )

        @Provides
        public fun provideGameRemoteMediator(
            gameApi: GameApi,
            gameDao: GameDao,
            gameRemoteKeysDao: GameRemoteKeysDao,
            pagingConfig: PagingConfig,
            gameDtoMapper: GameDtoMapper,
            gameEntityMapper: GameEntityMapper,
            transactionProvider: TransactionProvider,
        ): RemoteMediator<Int, GameWithPlatforms> = GameRemoteMediator(
            gameApi = gameApi,
            gameDao = gameDao,
            remoteKeysDao = gameRemoteKeysDao,
            dtoMapper = gameDtoMapper,
            entityMapper = gameEntityMapper,
            transactionProvider = transactionProvider,
        )
    }
}
