package io.github.onreg.data.movies.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.data.movies.api.GameMoviesRepository
import io.github.onreg.data.movies.impl.GameMoviesRepositoryImpl
import io.github.onreg.data.movies.impl.mapper.MovieDtoMapper
import io.github.onreg.data.movies.impl.mapper.MovieDtoMapperImpl
import io.github.onreg.data.movies.impl.mapper.MovieEntityMapper
import io.github.onreg.data.movies.impl.mapper.MovieEntityMapperImpl
import io.github.onreg.data.movies.impl.paging.MoviesRemoteMediatorFactory
import io.github.onreg.data.movies.impl.paging.MoviesRemoteMediatorFactoryImpl
import io.github.onreg.data.movies.impl.quality.MovieQualitySelector
import io.github.onreg.data.movies.impl.quality.MovieQualitySelectorImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public abstract class MoviesModule {
    @Binds
    @Singleton
    public abstract fun bindMoviesRepository(impl: GameMoviesRepositoryImpl): GameMoviesRepository

    @Binds
    public abstract fun bindMovieDtoMapper(impl: MovieDtoMapperImpl): MovieDtoMapper

    @Binds
    public abstract fun bindMovieEntityMapper(impl: MovieEntityMapperImpl): MovieEntityMapper

    @Binds
    public abstract fun bindMovieQualitySelector(
        impl: MovieQualitySelectorImpl,
    ): MovieQualitySelector

    @Binds
    public abstract fun bindMoviesRemoteMediatorFactory(
        impl: MoviesRemoteMediatorFactoryImpl,
    ): MoviesRemoteMediatorFactory
}
