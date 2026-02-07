package io.github.onreg.data.screenshots.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.screenshots.dao.GameScreenshotRemoteKeysDao
import io.github.onreg.core.db.screenshots.dao.GameScreenshotsDao
import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.core.network.rawg.api.GameScreenshotsApi
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.rawg.dto.ScreenshotDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotDtoMapper
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import kotlin.test.Test
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
    fun `refresh clears and inserts using game scoped keys`() = runTest {
        val dto = ScreenshotDto(id = 1, imageUrl = "https://img", width = 100, height = 100)
        val entity = ScreenshotEntity(id = 1, imageUrl = "https://img", width = 100, height = 100)

        api.stub {
            onBlocking { getScreenshots(7, 1, 10) } doReturn NetworkResponse.Success(
                PaginatedResponseDto(
                    count = 1,
                    next = null,
                    previous = null,
                    results = listOf(dto),
                ),
            )
        }
        mapper.stub { on { map(dto) } doReturn entity }

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        verify(dao).clearByGameId(7)
        verify(keysDao).clearByGameId(7)
        verify(dao).insertScreenshots(listOf(entity))
        verify(keysDao).insert(
            org.mockito.kotlin.check {
                assertTrue(it.all { key -> key.gameId == 7 })
            },
        )
    }

    private fun emptyPagingState(): PagingState<Int, ScreenshotEntity> = PagingState(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 10),
        leadingPlaceholderCount = 0,
    )
}
