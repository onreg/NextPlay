package io.github.onreg.data.movies.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.movies.dao.GameMovieRemoteKeysDao
import io.github.onreg.core.db.movies.dao.GameMoviesDao
import io.github.onreg.core.db.movies.entity.GameMovieRemoteKeysEntity
import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.core.network.rawg.api.GameMoviesApi
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.movies.impl.mapper.impl.MovieDtoMapper
import java.net.URI

private const val INITIAL_PAGE = 1

public class GameMoviesRemoteMediator(
    private val gameId: Int,
    private val moviesApi: GameMoviesApi,
    private val moviesDao: GameMoviesDao,
    private val remoteKeysDao: GameMovieRemoteKeysDao,
    private val dtoMapper: MovieDtoMapper,
) : RemoteMediator<Int, MovieEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>,
    ): MediatorResult {
        val pageResolution = resolvePage(loadType, state)
        if (pageResolution is PageResolution.Finished) {
            return MediatorResult.Success(
                endOfPaginationReached = pageResolution.endOfPaginationReached,
            )
        }
        val page = (pageResolution as PageResolution.LoadPage).page

        return when (val response = moviesApi.getMovies(gameId, page, state.config.pageSize)) {
            is NetworkResponse.Success -> {
                val positionStart = (page - INITIAL_PAGE).toLong() * state.config.pageSize
                val items = response.body.results
                    .mapIndexedNotNull { index, dto ->
                        dtoMapper.map(dto, gameId, positionStart + index)
                    }
                val nextPage = response.body.next?.let(::parseNextPage)

                if (loadType == LoadType.REFRESH) {
                    moviesDao.deleteByGameId(gameId)
                }

                moviesDao.insertMovies(items)
                remoteKeysDao.insertRemoteKeys(
                    items.map {
                        GameMovieRemoteKeysEntity(
                            gameId = gameId,
                            prevKey = if (page == INITIAL_PAGE) null else page - 1,
                            nextKey = nextPage,
                        )
                    },
                )

                MediatorResult.Success(endOfPaginationReached = nextPage == null)
            }

            is NetworkResponse.Failure -> {
                MediatorResult.Error(
                    response.exception ?: IllegalStateException("Unknown movie error"),
                )
            }
        }
    }

    private suspend fun resolvePage(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>,
    ): PageResolution = when (loadType) {
        LoadType.REFRESH -> {
            PageResolution.LoadPage(INITIAL_PAGE)
        }

        LoadType.PREPEND -> {
            PageResolution.Finished(endOfPaginationReached = true)
        }

        LoadType.APPEND -> {
            val last = state.pages
                .lastOrNull()
                ?.data
                ?.lastOrNull()

            if (last == null) {
                PageResolution.Finished(endOfPaginationReached = false)
            } else {
                val nextKey = remoteKeysDao.getByGameId(gameId)?.nextKey
                if (nextKey == null) {
                    PageResolution.Finished(endOfPaginationReached = true)
                } else {
                    PageResolution.LoadPage(nextKey)
                }
            }
        }
    }

    private fun parseNextPage(next: String): Int? {
        val query = runCatching { URI(next).query }.getOrNull() ?: return null
        return query
            .split('&')
            .mapNotNull {
                val parts = it.split('=', limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }.firstOrNull { it.first == "page" }
            ?.second
            ?.toIntOrNull()
    }

    private sealed interface PageResolution {
        data class LoadPage(val page: Int) : PageResolution

        data class Finished(val endOfPaginationReached: Boolean) : PageResolution
    }
}
