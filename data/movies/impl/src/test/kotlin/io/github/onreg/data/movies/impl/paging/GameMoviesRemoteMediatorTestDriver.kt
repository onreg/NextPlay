package io.github.onreg.data.movies.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.movies.dao.GameMovieRemoteKeysDao
import io.github.onreg.core.db.movies.dao.GameMoviesDao
import io.github.onreg.core.db.movies.entity.GameMovieRemoteKeysEntity
import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.core.network.rawg.api.GameMoviesApi
import io.github.onreg.core.network.rawg.dto.MovieDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.movies.impl.mapper.impl.MovieDtoMapper
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

internal class GameMoviesRemoteMediatorTestDriver private constructor(
    val gameId: Int,
    val moviesApi: GameMoviesApi,
    val moviesDao: GameMoviesDao,
    val remoteKeysDao: GameMovieRemoteKeysDao,
    val dtoMapper: MovieDtoMapper,
    val pagingConfig: PagingConfig,
) : RemoteMediator<Int, MovieEntity>() {
    private val mediator by lazy {
        GameMoviesRemoteMediator(
            gameId = gameId,
            moviesApi = moviesApi,
            moviesDao = moviesDao,
            remoteKeysDao = remoteKeysDao,
            dtoMapper = dtoMapper,
        )
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>,
    ) = mediator.load(loadType, state)

    fun emptyPagingState(): PagingState<Int, MovieEntity> = PagingState(
        pages = emptyList(),
        anchorPosition = null,
        config = pagingConfig,
        leadingPlaceholderCount = 0,
    )

    fun pagingStateWithLastItem(lastItem: MovieEntity): PagingState<Int, MovieEntity> = PagingState(
        pages = listOf(
            PagingSource.LoadResult.Page(
                data = listOf(lastItem),
                prevKey = null,
                nextKey = null,
            ),
        ),
        anchorPosition = 0,
        config = pagingConfig,
        leadingPlaceholderCount = 0,
    )

    class Builder {
        private val gameId = 7
        private val moviesApi: GameMoviesApi = mock()
        private val moviesDao: GameMoviesDao = mock()
        private val remoteKeysDao: GameMovieRemoteKeysDao = mock()
        private val dtoMapper: MovieDtoMapper = mock()
        private val pagingConfig = PagingConfig(
            pageSize = 2,
            prefetchDistance = 1,
            initialLoadSize = 2,
            maxSize = 10,
        )

        fun moviesApiGetMovies(
            page: Int = 1,
            response: NetworkResponse<PaginatedResponseDto<MovieDto>>,
        ): Builder = apply {
            moviesApi.stub {
                onBlocking { getMovies(gameId, page, pagingConfig.pageSize) } doReturn response
            }
        }

        fun remoteKeysDaoGetByGameId(entity: GameMovieRemoteKeysEntity?): Builder = apply {
            remoteKeysDao.stub { onBlocking { getByGameId(gameId) } doReturn entity }
        }

        fun movieDtoMapperMap(
            dto: MovieDto,
            gameId: Int,
            position: Long,
            entity: MovieEntity?,
        ): Builder = apply {
            dtoMapper.stub { on { map(dto, gameId, position) } doReturn entity }
        }

        fun build() = GameMoviesRemoteMediatorTestDriver(
            gameId = gameId,
            moviesApi = moviesApi,
            moviesDao = moviesDao,
            remoteKeysDao = remoteKeysDao,
            dtoMapper = dtoMapper,
            pagingConfig = pagingConfig,
        )
    }
}
