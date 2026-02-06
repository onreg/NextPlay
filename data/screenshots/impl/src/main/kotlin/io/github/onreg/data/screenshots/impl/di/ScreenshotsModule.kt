package io.github.onreg.data.screenshots.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.data.screenshots.api.GameScreenshotsRepository
import io.github.onreg.data.screenshots.impl.GameScreenshotsRepositoryImpl
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotDtoMapper
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotDtoMapperImpl
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotEntityMapper
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotEntityMapperImpl
import io.github.onreg.data.screenshots.impl.paging.ScreenshotsRemoteMediatorFactory
import io.github.onreg.data.screenshots.impl.paging.ScreenshotsRemoteMediatorFactoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public abstract class ScreenshotsModule {
    @Binds
    @Singleton
    public abstract fun bindScreenshotsRepository(
        impl: GameScreenshotsRepositoryImpl,
    ): GameScreenshotsRepository

    @Binds
    public abstract fun bindScreenshotDtoMapper(impl: ScreenshotDtoMapperImpl): ScreenshotDtoMapper

    @Binds
    public abstract fun bindScreenshotEntityMapper(
        impl: ScreenshotEntityMapperImpl,
    ): ScreenshotEntityMapper

    @Binds
    public abstract fun bindScreenshotsRemoteMediatorFactory(
        impl: ScreenshotsRemoteMediatorFactoryImpl,
    ): ScreenshotsRemoteMediatorFactory
}
