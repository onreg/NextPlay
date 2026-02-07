package io.github.onreg.data.movies.impl.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.db.movies.dao.GameMovieRemoteKeysDao
import io.github.onreg.core.db.movies.dao.GameMoviesDao
import io.github.onreg.core.network.rawg.api.GameMoviesApi
import io.github.onreg.data.movies.api.GameMoviesRepository
import io.github.onreg.data.movies.impl.GameMoviesRemoteMediatorFactory
import io.github.onreg.data.movies.impl.GameMoviesRepositoryImpl
import io.github.onreg.data.movies.impl.mapper.MovieDtoMapper
import io.github.onreg.data.movies.impl.mapper.MovieEntityMapper
import io.github.onreg.data.movies.impl.mapper.impl.MovieDtoMapperImpl
import io.github.onreg.data.movies.impl.mapper.impl.MovieEntityMapperImpl
import io.github.onreg.data.movies.impl.paging.GameMoviesRemoteMediator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public abstract class GameMoviesModule {
    @Binds
    @Singleton
    public abstract fun bindGameMoviesRepository(
        impl: GameMoviesRepositoryImpl,
    ): GameMoviesRepository

    @Binds
    public abstract fun bindMovieDtoMapper(impl: MovieDtoMapperImpl): MovieDtoMapper

    @Binds
    public abstract fun bindMovieEntityMapper(impl: MovieEntityMapperImpl): MovieEntityMapper

    public companion object {
        @Provides
        @Singleton
        public fun provideRemoteMediatorFactory(
            api: GameMoviesApi,
            dao: GameMoviesDao,
            keysDao: GameMovieRemoteKeysDao,
            mapper: MovieDtoMapper,
        ): GameMoviesRemoteMediatorFactory = GameMoviesRemoteMediatorFactory { gameId ->
            GameMoviesRemoteMediator(
                gameId = gameId,
                moviesApi = api,
                moviesDao = dao,
                remoteKeysDao = keysDao,
                dtoMapper = mapper,
            )
        }
    }
}
