package io.github.onreg.data.series.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.data.series.api.GameSeriesRepository
import io.github.onreg.data.series.impl.GameSeriesRepositoryImpl
import io.github.onreg.data.series.impl.mapper.SeriesGameDtoMapper
import io.github.onreg.data.series.impl.mapper.SeriesGameDtoMapperImpl
import io.github.onreg.data.series.impl.mapper.SeriesGameEntityMapper
import io.github.onreg.data.series.impl.mapper.SeriesGameEntityMapperImpl
import io.github.onreg.data.series.impl.mapper.SeriesGameMapper
import io.github.onreg.data.series.impl.mapper.SeriesGameMapperImpl
import io.github.onreg.data.series.impl.paging.SeriesRemoteMediatorFactory
import io.github.onreg.data.series.impl.paging.SeriesRemoteMediatorFactoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public abstract class SeriesModule {
    @Binds
    @Singleton
    public abstract fun bindSeriesRepository(impl: GameSeriesRepositoryImpl): GameSeriesRepository

    @Binds
    public abstract fun bindSeriesGameEntityMapper(
        impl: SeriesGameEntityMapperImpl,
    ): SeriesGameEntityMapper

    @Binds
    public abstract fun bindSeriesGameMapper(impl: SeriesGameMapperImpl): SeriesGameMapper

    @Binds
    public abstract fun bindSeriesGameDtoMapper(impl: SeriesGameDtoMapperImpl): SeriesGameDtoMapper

    @Binds
    public abstract fun bindSeriesRemoteMediatorFactory(
        impl: SeriesRemoteMediatorFactoryImpl,
    ): SeriesRemoteMediatorFactory
}
