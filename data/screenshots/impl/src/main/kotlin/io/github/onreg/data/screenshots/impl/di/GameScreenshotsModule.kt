package io.github.onreg.data.screenshots.impl.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.db.screenshots.dao.GameScreenshotRemoteKeysDao
import io.github.onreg.core.db.screenshots.dao.GameScreenshotsDao
import io.github.onreg.core.network.rawg.api.GameScreenshotsApi
import io.github.onreg.data.screenshots.api.GameScreenshotsRepository
import io.github.onreg.data.screenshots.impl.GameScreenshotsRemoteMediatorFactory
import io.github.onreg.data.screenshots.impl.GameScreenshotsRepositoryImpl
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotDtoMapper
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotDtoMapperImpl
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotEntityMapper
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotEntityMapperImpl
import io.github.onreg.data.screenshots.impl.paging.GameScreenshotsRemoteMediator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public abstract class GameScreenshotsModule {
    @Binds
    @Singleton
    public abstract fun bindGameScreenshotsRepository(
        impl: GameScreenshotsRepositoryImpl,
    ): GameScreenshotsRepository

    @Binds
    public abstract fun bindScreenshotDtoMapper(impl: ScreenshotDtoMapperImpl): ScreenshotDtoMapper

    @Binds
    public abstract fun bindScreenshotEntityMapper(
        impl: ScreenshotEntityMapperImpl,
    ): ScreenshotEntityMapper

    public companion object {
        @Provides
        @Singleton
        public fun provideRemoteMediatorFactory(
            api: GameScreenshotsApi,
            dao: GameScreenshotsDao,
            keysDao: GameScreenshotRemoteKeysDao,
            mapper: ScreenshotDtoMapper,
        ): GameScreenshotsRemoteMediatorFactory = GameScreenshotsRemoteMediatorFactory { gameId ->
            GameScreenshotsRemoteMediator(
                gameId = gameId,
                screenshotsApi = api,
                screenshotsDao = dao,
                remoteKeysDao = keysDao,
                dtoMapper = mapper,
            )
        }
    }
}
