package io.github.onreg.data.movies.impl

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.movies.dao.GameMoviesDao
import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.data.movies.api.GameMoviesRepository
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.movies.impl.mapper.impl.MovieEntityMapper
import io.github.onreg.data.movies.impl.paging.GameMoviesRemoteMediator
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

internal class GameMoviesRepositoryTestDriver private constructor(
    val moviesDao: GameMoviesDao,
    val entityMapper: MovieEntityMapper,
    val remoteMediator: GameMoviesRemoteMediator,
    val remoteMediatorFactory: GameMoviesRemoteMediatorFactory,
) : GameMoviesRepository {
    private val repository: GameMoviesRepository by lazy {
        GameMoviesRepositoryImpl(
            moviesDao = moviesDao,
            entityMapper = entityMapper,
            remoteMediatorFactory = remoteMediatorFactory,
        )
    }

    override fun getMovies(gameId: Int) = repository.getMovies(gameId)

    class Builder {
        private val moviesDao: GameMoviesDao = mock()
        private val entityMapper: MovieEntityMapper = mock()
        private val remoteMediator: GameMoviesRemoteMediator = mock {
            onBlocking { load(any(), any()) } doReturn RemoteMediator.MediatorResult.Success(
                endOfPaginationReached = true,
            )
        }
        private val remoteMediatorFactory: GameMoviesRemoteMediatorFactory = mock {
            on { create(any()) } doReturn remoteMediator
        }

        fun movieEntityMapperMap(
            entity: MovieEntity,
            mapped: Movie,
        ): Builder = apply {
            entityMapper.stub { on { map(entity) } doReturn mapped }
        }

        fun moviesDaoPagingSource(
            gameId: Int,
            pagingSource: List<MovieEntity>,
        ): Builder = apply {
            val source: PagingSource<Int, MovieEntity> = mock {
                onBlocking { load(any()) } doReturn LoadResult.Page(
                    data = pagingSource,
                    prevKey = null,
                    nextKey = null,
                )
            }
            moviesDao.stub { on { pagingSource(gameId) } doReturn source }
        }

        fun build(): GameMoviesRepositoryTestDriver = GameMoviesRepositoryTestDriver(
            moviesDao = moviesDao,
            entityMapper = entityMapper,
            remoteMediator = remoteMediator,
            remoteMediatorFactory = remoteMediatorFactory,
        )
    }
}
