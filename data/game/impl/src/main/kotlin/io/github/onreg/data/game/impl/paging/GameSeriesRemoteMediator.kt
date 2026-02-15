package io.github.onreg.data.game.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.core.db.series.dao.SeriesRemoteKeysDao
import io.github.onreg.core.db.series.entity.SeriesRemoteKeysEntity
import io.github.onreg.core.network.rawg.api.GameSeriesApi
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.impl.mapper.GameDtoMapper
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import java.net.URI

private const val INITIAL_PAGE = 1

public class GameSeriesRemoteMediator(
    private val gameId: Int,
    private val gameSeriesApi: GameSeriesApi,
    private val gameDao: GameDao,
    private val seriesDao: SeriesDao,
    private val seriesRemoteKeysDao: SeriesRemoteKeysDao,
    private val dtoMapper: GameDtoMapper,
    private val entityMapper: GameEntityMapper,
) : RemoteMediator<Int, GameWithPlatforms>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, GameWithPlatforms>,
    ): MediatorResult {
        val pageResolution = resolvePage(loadType, state)
        if (pageResolution is PageResolution.Finished) {
            return MediatorResult.Success(
                endOfPaginationReached = pageResolution.endOfPaginationReached,
            )
        }
        val page = (pageResolution as PageResolution.LoadPage).page

        return when (
            val response = gameSeriesApi.getGameSeries(
                gameId,
                page,
                state.config.pageSize,
            )
        ) {
            is NetworkResponse.Success -> {
                val games = response.body.results.map(dtoMapper::map)
                val nextPage = response.body.next?.let(::parseNextPage)
                persist(
                    games = games,
                    loadType = loadType,
                    page = page,
                    pageSize = state.config.pageSize,
                    nextPage = nextPage,
                )

                MediatorResult.Success(endOfPaginationReached = nextPage == null)
            }

            is NetworkResponse.Failure -> {
                MediatorResult.Error(
                    response.exception ?: IllegalStateException("Unknown series error"),
                )
            }
        }
    }

    private suspend fun resolvePage(
        loadType: LoadType,
        state: PagingState<Int, GameWithPlatforms>,
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
                val nextKey = seriesRemoteKeysDao.getByGameId(last.game.id)?.nextKey
                if (nextKey == null) {
                    PageResolution.Finished(endOfPaginationReached = true)
                } else {
                    PageResolution.LoadPage(nextKey)
                }
            }
        }
    }

    private suspend fun persist(
        games: List<Game>,
        loadType: LoadType,
        page: Int,
        pageSize: Int,
        nextPage: Int?,
    ) {
        val insertionStart = (page - INITIAL_PAGE).toLong() * pageSize
        val bundle = entityMapper.map(games)
        val seriesEntries = entityMapper.mapSeriesEntries(games, insertionStart)

        if (loadType == LoadType.REFRESH) {
            seriesDao.deleteAll()
            seriesRemoteKeysDao.deleteAll()
        }

        gameDao.insertGamesWithPlatforms(bundle)
        seriesDao.insertSeriesEntries(seriesEntries)
        seriesRemoteKeysDao.insertRemoteKeys(
            games.map {
                SeriesRemoteKeysEntity(
                    gameId = it.id,
                    prevKey = if (page == INITIAL_PAGE) null else page - 1,
                    nextKey = nextPage,
                )
            },
        )
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
