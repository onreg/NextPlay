package io.github.onreg.data.series.impl.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.db.game.list.dao.GameListDao
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.core.db.series.dao.SeriesRemoteKeysDao
import io.github.onreg.core.network.rawg.api.GameSeriesApi
import io.github.onreg.data.game.list.impl.mapper.GameDtoMapper
import io.github.onreg.data.game.list.impl.mapper.GameEntityMapper
import io.github.onreg.data.series.api.GameSeriesRepository
import io.github.onreg.data.series.impl.GameSeriesRemoteMediatorFactory
import io.github.onreg.data.series.impl.GameSeriesRepositoryImpl
import io.github.onreg.data.series.impl.paging.GameSeriesRemoteMediator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public abstract class GameSeriesModule {
    @Binds
    @Singleton
    public abstract fun bindGameSeriesRepository(
        impl: GameSeriesRepositoryImpl,
    ): GameSeriesRepository

    public companion object {
        @Provides
        @Singleton
        public fun provideRemoteMediatorFactory(
            api: GameSeriesApi,
            gameListDao: GameListDao,
            seriesDao: SeriesDao,
            seriesRemoteKeysDao: SeriesRemoteKeysDao,
            gameDtoMapper: GameDtoMapper,
            gameEntityMapper: GameEntityMapper,
        ): GameSeriesRemoteMediatorFactory = GameSeriesRemoteMediatorFactory { parentGameId ->
            GameSeriesRemoteMediator(
                parentGameId = parentGameId,
                gameSeriesApi = api,
                gameListDao = gameListDao,
                seriesDao = seriesDao,
                seriesRemoteKeysDao = seriesRemoteKeysDao,
                dtoMapper = gameDtoMapper,
                entityMapper = gameEntityMapper,
            )
        }
    }
}
