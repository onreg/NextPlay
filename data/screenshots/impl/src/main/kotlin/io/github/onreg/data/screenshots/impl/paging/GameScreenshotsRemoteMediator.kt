package io.github.onreg.data.screenshots.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.screenshots.dao.GameScreenshotRemoteKeysDao
import io.github.onreg.core.db.screenshots.dao.GameScreenshotsDao
import io.github.onreg.core.db.screenshots.entity.GameScreenshotRemoteKeysEntity
import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.core.network.rawg.api.GameScreenshotsApi
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotDtoMapper
import java.net.URI

private const val INITIAL_PAGE = 1

public class GameScreenshotsRemoteMediator(
    private val gameId: Int,
    private val screenshotsApi: GameScreenshotsApi,
    private val screenshotsDao: GameScreenshotsDao,
    private val remoteKeysDao: GameScreenshotRemoteKeysDao,
    private val dtoMapper: ScreenshotDtoMapper,
) : RemoteMediator<Int, ScreenshotEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ScreenshotEntity>,
    ): MediatorResult {
        val pageResolution = resolvePage(loadType, state)
        if (pageResolution is PageResolution.Finished) {
            return MediatorResult.Success(
                endOfPaginationReached = pageResolution.endOfPaginationReached,
            )
        }
        val page = (pageResolution as PageResolution.LoadPage).page

        return when (
            val response = screenshotsApi.getScreenshots(
                gameId,
                page,
                state.config.pageSize,
            )
        ) {
            is NetworkResponse.Success -> {
                val nextPage = response.body.next?.let(::parseNextPage)
                val positionStart = (page - INITIAL_PAGE).toLong() * state.config.pageSize
                val items = response.body.results
                    .mapIndexed { index, dto ->
                        dtoMapper.map(
                            dto = dto,
                            gameId = gameId,
                            position = positionStart + index,
                        )
                    }
                persist(
                    items = items,
                    loadType = loadType,
                    page = page,
                    nextPage = nextPage,
                )
                MediatorResult.Success(endOfPaginationReached = nextPage == null)
            }

            is NetworkResponse.Failure -> {
                MediatorResult.Error(
                    response.exception ?: IllegalStateException("Unknown screenshot error"),
                )
            }
        }
    }

    private suspend fun resolvePage(
        loadType: LoadType,
        state: PagingState<Int, ScreenshotEntity>,
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

    private suspend fun persist(
        items: List<ScreenshotEntity>,
        loadType: LoadType,
        page: Int,
        nextPage: Int?,
    ) {
        if (loadType == LoadType.REFRESH) {
            screenshotsDao.deleteByGameId(gameId)
        }

        screenshotsDao.insertScreenshots(items)
        remoteKeysDao.insertRemoteKeys(
            items.map {
                GameScreenshotRemoteKeysEntity(
                    gameId = gameId,
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
