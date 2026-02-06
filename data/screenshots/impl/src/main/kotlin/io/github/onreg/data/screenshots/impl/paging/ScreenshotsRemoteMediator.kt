package io.github.onreg.data.screenshots.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.TransactionProvider
import io.github.onreg.core.db.screenshots.dao.ScreenshotDao
import io.github.onreg.core.db.screenshots.dao.ScreenshotRemoteKeysDao
import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.core.db.screenshots.entity.ScreenshotRemoteKeysEntity
import io.github.onreg.core.network.rawg.api.GameScreenshotsApi
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.rawg.dto.ScreenshotDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotDtoMapper
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotEntityMapper
import java.net.URI
import javax.inject.Inject

private const val INITIAL_PAGE = 1

public class ScreenshotsRemoteMediator(
    private val gameId: Int,
    private val screenshotsApi: GameScreenshotsApi,
    private val screenshotDao: ScreenshotDao,
    private val remoteKeysDao: ScreenshotRemoteKeysDao,
    private val dtoMapper: ScreenshotDtoMapper,
    private val entityMapper: ScreenshotEntityMapper,
    private val transactionProvider: TransactionProvider,
) : RemoteMediator<Int, ScreenshotEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ScreenshotEntity>,
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
                when (val response = screenshotsApi.getScreenshots(gameId, page, config.pageSize)) {
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
        response: PaginatedResponseDto<ScreenshotDto>,
        isRefresh: Boolean,
        config: PagingConfig,
    ): Int? {
        val models = response.results.mapNotNull(dtoMapper::map)
        val insertionOrderStart = (page - INITIAL_PAGE).toLong() * config.pageSize
        val entities = models.mapIndexed { index, model ->
            entityMapper.mapToEntity(
                model = model,
                gameId = gameId,
                insertionOrder = insertionOrderStart + index,
            )
        }
        val nextPage = response.next?.let(::getNextPageFromResponse)
        val keys = entities.map { entity ->
            ScreenshotRemoteKeysEntity(
                gameId = entity.gameId,
                screenshotId = entity.id,
                prevKey = if (page == INITIAL_PAGE) null else page - 1,
                nextKey = nextPage,
            )
        }
        transactionProvider.run {
            if (isRefresh) {
                screenshotDao.clearForGame(gameId)
                remoteKeysDao.clearForGame(gameId)
            }
            screenshotDao.upsertAll(entities)
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
        state: PagingState<Int, ScreenshotEntity>,
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
                val remoteKey = remoteKeysDao.getRemoteKey(gameId, lastItem.id)
                if (remoteKey == null) {
                    NextPage.Error(
                        IllegalStateException("Missing remote key for id=${lastItem.id}"),
                    )
                } else {
                    NextPage.Value(nextPage = remoteKey.nextKey)
                }
            }
        }
    }
}

public interface ScreenshotsRemoteMediatorFactory {
    public fun create(gameId: Int): RemoteMediator<Int, ScreenshotEntity>
}

public class ScreenshotsRemoteMediatorFactoryImpl
    @Inject
    constructor(
        private val screenshotsApi: GameScreenshotsApi,
        private val screenshotDao: ScreenshotDao,
        private val remoteKeysDao: ScreenshotRemoteKeysDao,
        private val dtoMapper: ScreenshotDtoMapper,
        private val entityMapper: ScreenshotEntityMapper,
        private val transactionProvider: TransactionProvider,
    ) : ScreenshotsRemoteMediatorFactory {
        override fun create(gameId: Int): RemoteMediator<Int, ScreenshotEntity> =
            ScreenshotsRemoteMediator(
                gameId = gameId,
                screenshotsApi = screenshotsApi,
                screenshotDao = screenshotDao,
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
