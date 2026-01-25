package io.github.onreg.data.game.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.TransactionProvider
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.dao.GameRemoteKeysDao
import io.github.onreg.core.db.game.entity.GameRemoteKeysEntity
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.network.rawg.api.GameApi
import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.game.impl.mapper.GameDtoMapper
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import java.net.URI

private const val INITIAL_PAGE = 1

public class GameRemoteMediator(
    private val gameApi: GameApi,
    private val gameDao: GameDao,
    private val remoteKeysDao: GameRemoteKeysDao,
    private val dtoMapper: GameDtoMapper,
    private val entityMapper: GameEntityMapper,
    private val transactionProvider: TransactionProvider,
) : RemoteMediator<Int, GameWithPlatforms>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, GameWithPlatforms>,
    ): MediatorResult {
        val nextPageResult = resolvePageToLoad(loadType, state)
        val page = (nextPageResult as? NextPage.Value)?.nextPage

        val result = when {
            nextPageResult is NextPage.Error -> {
                MediatorResult.Error(nextPageResult.exception)
            }

            nextPageResult is NextPage.WaitForRefresh -> {
                MediatorResult.Success(endOfPaginationReached = false)
            }

            page == null -> {
                MediatorResult.Success(endOfPaginationReached = true)
            }

            else -> {
                val config = state.config
                when (val response = gameApi.getGames(page = page, pageSize = config.pageSize)) {
                    is NetworkResponse.Success -> {
                        val persistedNextPage = persistResponse(
                            page = page,
                            response = response.body,
                            isRefresh = loadType == LoadType.REFRESH,
                            config = config,
                        )
                        MediatorResult.Success(endOfPaginationReached = persistedNextPage == null)
                    }

                    is NetworkResponse.Failure -> {
                        MediatorResult.Error(
                            response.exception ?: IllegalStateException("Unknown error"),
                        )
                    }
                }
            }
        }

        return result
    }

    private suspend fun persistResponse(
        page: Int,
        response: PaginatedResponseDto<GameDto>,
        isRefresh: Boolean,
        config: PagingConfig,
    ): Int? {
        val games = response.results.map(dtoMapper::map)
        val insertionOrderStart =
            (page - INITIAL_PAGE).toLong() * config.pageSize
        val databaseBundle = entityMapper.map(games, insertionOrderStart)
        val nextPage = response.next?.let(::getNextPageFromResponse)

        val keys = databaseBundle.games.map { entity ->
            GameRemoteKeysEntity(
                entity.id,
                if (page == INITIAL_PAGE) null else page - 1,
                nextPage,
            )
        }
        transactionProvider.run {
            if (isRefresh) {
                gameDao.clearGames()
            }
            gameDao.insertGamesWithPlatforms(databaseBundle)
            remoteKeysDao.insertRemoteKeys(keys)
        }
        return nextPage
    }

    private fun getNextPageFromResponse(next: String): Int? {
        val query = runCatching { URI(next).query }.getOrNull() ?: return null
        val pageParams = query
            .split('&')
            .mapNotNull {
                val parts = it.split('=', limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
        return pageParams
            .firstOrNull { it.first == "page" }
            ?.second
            ?.toIntOrNull()
    }

    private suspend fun resolvePageToLoad(
        loadType: LoadType,
        state: PagingState<Int, GameWithPlatforms>,
    ): NextPage {
        val result = when (loadType) {
            LoadType.REFRESH -> {
                NextPage.Value(nextPage = INITIAL_PAGE)
            }

            LoadType.PREPEND -> {
                NextPage.Value(nextPage = null)
            }

            LoadType.APPEND -> {
                val lastItem = state.pages
                    .lastOrNull()
                    ?.data
                    ?.lastOrNull()
                if (lastItem == null) {
                    NextPage.WaitForRefresh
                } else {
                    val remoteKey = remoteKeysDao.getRemoteKey(lastItem.game.id)
                    if (remoteKey == null) {
                        NextPage.Error(
                            IllegalStateException("Missing remote key for id=${lastItem.game.id}"),
                        )
                    } else {
                        NextPage.Value(nextPage = remoteKey.nextKey)
                    }
                }
            }
        }

        return result
    }
}

private sealed interface NextPage {
    object WaitForRefresh : NextPage

    data class Value(val nextPage: Int?) : NextPage

    data class Error(val exception: Throwable) : NextPage
}
