package io.github.onreg.data.screenshots.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.screenshots.dao.GameScreenshotRemoteKeysDao
import io.github.onreg.core.db.screenshots.dao.GameScreenshotsDao
import io.github.onreg.core.db.screenshots.entity.GameScreenshotRemoteKeysEntity
import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.core.network.rawg.api.GameScreenshotsApi
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.rawg.dto.ScreenshotDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotDtoMapper
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GameScreenshotsRemoteMediatorTest {
    private val api: GameScreenshotsApi = mock()
    private val dao: GameScreenshotsDao = mock()
    private val keysDao: GameScreenshotRemoteKeysDao = mock()
    private val mapper: ScreenshotDtoMapper = mock()

    private val mediator = GameScreenshotsRemoteMediator(
        gameId = 7,
        screenshotsApi = api,
        screenshotsDao = dao,
        remoteKeysDao = keysDao,
        dtoMapper = mapper,
    )

    @Test
    fun `refresh clears and inserts using game scoped keys with parsed next page`() = runTest {
        val dto1 = ScreenshotDto(id = 1, imageUrl = "https://img/1", width = 100, height = 100)
        val dto2 = ScreenshotDto(id = 2, imageUrl = "https://img/2", width = 200, height = 200)
        val entity1 = ScreenshotEntity(
            id = 1,
            gameId = 7,
            position = 0,
            imageUrl = "https://img/1",
            width = 100,
            height = 100,
        )
        val entity2 = ScreenshotEntity(
            id = 2,
            gameId = 7,
            position = 1,
            imageUrl = "https://img/2",
            width = 200,
            height = 200,
        )

        api.stub {
            onBlocking { getScreenshots(7, 1, 10) } doReturn NetworkResponse.Success(
                PaginatedResponseDto(
                    count = 2,
                    next = "https://example.com?page=3",
                    previous = null,
                    results = listOf(dto1, dto2),
                ),
            )
        }
        mapper.stub { on { map(dto1, 7, 0) } doReturn entity1 }
        mapper.stub { on { map(dto2, 7, 1) } doReturn entity2 }

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue(!result.endOfPaginationReached)
        verify(dao).deleteByGameId(7)
        verify(keysDao).deleteByGameId(7)
        verify(dao).insertScreenshots(
            listOf(
                entity1,
                entity2,
            ),
        )
        verify(keysDao).insertRemoteKeys(
            org.mockito.kotlin.check {
                assertTrue(it.all { key -> key.gameId == 7 })
                assertTrue(it.all { key -> key.prevKey == null })
                assertTrue(it.all { key -> key.nextKey == 3 })
            },
        )
    }

    @Test
    fun `refresh uses end of pagination when next page is missing`() = runTest {
        val dto = ScreenshotDto(id = 1, imageUrl = "https://img", width = 100, height = 100)
        val entity = ScreenshotEntity(
            id = 1,
            gameId = 7,
            position = 0,
            imageUrl = "https://img",
            width = 100,
            height = 100,
        )

        api.stub {
            onBlocking { getScreenshots(7, 1, 10) } doReturn NetworkResponse.Success(
                PaginatedResponseDto(
                    count = 1,
                    next = "https://example.com",
                    previous = null,
                    results = listOf(dto),
                ),
            )
        }
        mapper.stub { on { map(dto, 7, 0) } doReturn entity }

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue(result.endOfPaginationReached)
        verify(keysDao).insertRemoteKeys(
            org.mockito.kotlin.check {
                assertTrue(it.all { key -> key.nextKey == null })
            },
        )
    }

    @Test
    fun `prepend returns end of pagination without loading`() = runTest {
        val result = mediator.load(LoadType.PREPEND, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue(result.endOfPaginationReached)
        verify(api, never()).getScreenshots(any(), any(), any())
    }

    @Test
    fun `append with no items returns incomplete pagination without loading`() = runTest {
        val result = mediator.load(LoadType.APPEND, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue(!result.endOfPaginationReached)
        verify(api, never()).getScreenshots(any(), any(), any())
    }

    @Test
    fun `append without next key ends pagination`() = runTest {
        val lastEntity = ScreenshotEntity(
            id = 1,
            gameId = 7,
            position = 0,
            imageUrl = "https://img",
            width = 100,
            height = 100,
        )
        keysDao.stub {
            onBlocking { getByGameId(7) } doReturn GameScreenshotRemoteKeysEntity(
                gameId = 7,
                prevKey = 1,
                nextKey = null,
            )
        }

        val result = mediator.load(LoadType.APPEND, pagingStateWithLastItem(lastEntity))

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue(result.endOfPaginationReached)
        verify(api, never()).getScreenshots(any(), any(), any())
    }

    @Test
    fun `append loads next page and persists without clearing`() = runTest {
        val lastEntity = ScreenshotEntity(
            id = 1,
            gameId = 7,
            position = 9,
            imageUrl = "https://img",
            width = 100,
            height = 100,
        )
        val dto = ScreenshotDto(id = 2, imageUrl = "https://img/2", width = 200, height = 200)
        val mappedEntity = ScreenshotEntity(
            id = 2,
            gameId = 7,
            position = 10,
            imageUrl = "https://img/2",
            width = 200,
            height = 200,
        )
        keysDao.stub {
            onBlocking { getByGameId(7) } doReturn GameScreenshotRemoteKeysEntity(
                gameId = 7,
                prevKey = 1,
                nextKey = 2,
            )
        }
        api.stub {
            onBlocking { getScreenshots(7, 2, 10) } doReturn NetworkResponse.Success(
                PaginatedResponseDto(
                    count = 1,
                    next = "https://example.com?page=4",
                    previous = null,
                    results = listOf(dto),
                ),
            )
        }
        mapper.stub { on { map(dto, 7, 10) } doReturn mappedEntity }

        val result = mediator.load(LoadType.APPEND, pagingStateWithLastItem(lastEntity))

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue(!result.endOfPaginationReached)
        verify(dao, never()).deleteByGameId(7)
        verify(keysDao, never()).deleteByGameId(7)
        verify(dao).insertScreenshots(
            listOf(mappedEntity),
        )
        verify(keysDao).insertRemoteKeys(
            listOf(
                GameScreenshotRemoteKeysEntity(
                    gameId = 7,
                    prevKey = 1,
                    nextKey = 4,
                ),
            ),
        )
    }

    @Test
    fun `refresh returns error on network failure`() = runTest {
        api.stub {
            onBlocking { getScreenshots(7, 1, 10) } doReturn
                NetworkResponse.Failure.NetworkError(IOException("boom"))
        }

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertTrue(result.throwable is IOException)
    }

    @Test
    fun `refresh returns error when response has no exception`() = runTest {
        api.stub {
            onBlocking { getScreenshots(7, 1, 10) } doReturn
                NetworkResponse.Failure.OtherError(null)
        }

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertEquals("Unknown screenshot error", result.throwable.message)
    }

    private fun emptyPagingState(): PagingState<Int, ScreenshotEntity> = PagingState(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 10),
        leadingPlaceholderCount = 0,
    )

    private fun pagingStateWithLastItem(
        entity: ScreenshotEntity,
    ): PagingState<Int, ScreenshotEntity> = PagingState(
        pages = listOf(
            PagingSource.LoadResult.Page(
                data = listOf(entity),
                prevKey = null,
                nextKey = null,
            ),
        ),
        anchorPosition = null,
        config = PagingConfig(pageSize = 10),
        leadingPlaceholderCount = 0,
    )
}
