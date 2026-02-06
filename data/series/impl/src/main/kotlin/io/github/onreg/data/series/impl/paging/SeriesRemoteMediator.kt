package io.github.onreg.data.series.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.TransactionProvider
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.core.db.series.dao.SeriesRemoteKeysDao
import io.github.onreg.core.db.series.entity.SeriesRemoteKeysEntity
import io.github.onreg.core.network.rawg.api.GameSeriesApi
import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.series.impl.mapper.SeriesGameDtoMapper
import io.github.onreg.data.series.impl.mapper.SeriesGameEntityMapper
import java.net.URI
import javax.inject.Inject

private const val INITIAL_PAGE = 1

public class SeriesRemoteMediator(
    private val parentGameId: Int,
    private val seriesApi: GameSeriesApi,
    private val gameDao: GameDao,
    private val seriesDao: SeriesDao,
    private val remoteKeysDao: SeriesRemoteKeysDao,
    private val dtoMapper: SeriesGameDtoMapper,
    private val entityMapper: SeriesGameEntityMapper,
    private val transactionProvider: TransactionProvider,
) : RemoteMediator<Int, GameWithPlatforms>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, GameWithPlatforms>,
    ): MediatorResult {
        val nextPageResult = resolvePageToLoad(loadType, state)
        val page = (nextPageResult as? NextPage.Value)?.nextPage

        return when {
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
                when (val response = seriesApi.getSeries(parentGameId, page, config.pageSize)) {
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
    }

    private suspend fun persistResponse(
        page: Int,
        response: PaginatedResponseDto<GameDto>,
        isRefresh: Boolean,
        config: PagingConfig,
    ): Int? {
        val models = response.results.map(dtoMapper::map)
        val insertionOrderStart = (page - INITIAL_PAGE).toLong() * config.pageSize
        val bundle = entityMapper.map(models, parentGameId, insertionOrderStart)
        val nextPage = response.next?.let(::getNextPageFromResponse)
        val keys = bundle.seriesEntities.map { entity ->
            SeriesRemoteKeysEntity(
                parentGameId = entity.parentGameId,
                gameId = entity.gameId,
                prevKey = if (page == INITIAL_PAGE) null else page - 1,
                nextKey = nextPage,
            )
        }

        transactionProvider.run {
            if (isRefresh) {
                seriesDao.clearForParent(parentGameId)
                remoteKeysDao.clearForParent(parentGameId)
            }
            gameDao.insertGamesWithPlatforms(
                GameInsertionBundle(
                    games = bundle.games,
                    listEntities = emptyList(),
                    platforms = bundle.platforms,
                    crossRefs = bundle.crossRefs,
                ),
            )
            seriesDao.insertAll(bundle.seriesEntities)
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
    ): NextPage = when (loadType) {
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
                val remoteKey = remoteKeysDao.getRemoteKey(parentGameId, lastItem.game.id)
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
}

public interface SeriesRemoteMediatorFactory {
    public fun create(parentGameId: Int): RemoteMediator<Int, GameWithPlatforms>
}

public class SeriesRemoteMediatorFactoryImpl
    @Inject
    constructor(
        private val seriesApi: GameSeriesApi,
        private val gameDao: GameDao,
        private val seriesDao: SeriesDao,
        private val remoteKeysDao: SeriesRemoteKeysDao,
        private val dtoMapper: SeriesGameDtoMapper,
        private val entityMapper: SeriesGameEntityMapper,
        private val transactionProvider: TransactionProvider,
    ) : SeriesRemoteMediatorFactory {
        override fun create(parentGameId: Int): RemoteMediator<Int, GameWithPlatforms> =
            SeriesRemoteMediator(
                parentGameId = parentGameId,
                seriesApi = seriesApi,
                gameDao = gameDao,
                seriesDao = seriesDao,
                remoteKeysDao = remoteKeysDao,
                dtoMapper = dtoMapper,
                entityMapper = entityMapper,
                transactionProvider = transactionProvider,
            )
    }

private sealed interface NextPage {
    object WaitForRefresh : NextPage

    data class Value(val nextPage: Int?) : NextPage

    data class Error(val exception: Throwable) : NextPage
}
