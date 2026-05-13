package io.github.onreg.data.movies.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.onreg.core.db.movies.dao.GameMoviesDao
import io.github.onreg.data.movies.api.GameMoviesRepository
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.movies.impl.mapper.impl.MovieEntityMapper
import io.github.onreg.data.movies.impl.paging.GameMoviesRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

public class GameMoviesRepositoryImpl
    @Inject
    constructor(
        private val moviesDao: GameMoviesDao,
        private val entityMapper: MovieEntityMapper,
        private val remoteMediatorFactory: GameMoviesRemoteMediatorFactory,
    ) : GameMoviesRepository {
        private val pagingConfig: PagingConfig = PagingConfig(
            pageSize = 10,
            prefetchDistance = 3,
            initialLoadSize = 20,
            maxSize = 200,
            enablePlaceholders = false,
        )

        override fun getMovies(gameId: Int): Flow<PagingData<Movie>> = Pager(
            config = pagingConfig,
            remoteMediator = remoteMediatorFactory.create(gameId),
        ) {
            moviesDao.pagingSource(gameId)
        }.flow.map { pagingData ->
            pagingData.map(entityMapper::map)
        }
    }

public fun interface GameMoviesRemoteMediatorFactory {
    public fun create(gameId: Int): GameMoviesRemoteMediator
}
