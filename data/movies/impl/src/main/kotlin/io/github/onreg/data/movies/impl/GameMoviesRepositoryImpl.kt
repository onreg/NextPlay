package io.github.onreg.data.movies.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.onreg.core.db.movies.dao.MovieDao
import io.github.onreg.data.movies.api.GameMoviesRepository
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.movies.impl.mapper.MovieEntityMapper
import io.github.onreg.data.movies.impl.paging.MoviesRemoteMediatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

public class GameMoviesRepositoryImpl
    @Inject
    constructor(
        private val movieDao: MovieDao,
        private val pagingConfig: PagingConfig,
        private val remoteMediatorFactory: MoviesRemoteMediatorFactory,
        private val entityMapper: MovieEntityMapper,
    ) : GameMoviesRepository {
        override fun getMovies(gameId: Int): Flow<PagingData<Movie>> = Pager(
            config = pagingConfig,
            remoteMediator = remoteMediatorFactory.create(gameId),
        ) {
            movieDao.pagingSource(gameId)
        }.flow.map { pagingData ->
            pagingData.map(entityMapper::map)
        }
    }
