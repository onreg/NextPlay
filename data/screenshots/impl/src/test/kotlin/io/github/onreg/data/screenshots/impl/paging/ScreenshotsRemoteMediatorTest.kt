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
import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotDtoMapper
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotEntityMapper
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertTrue

internal class ScreenshotsRemoteMediatorTest {
    private val api: GameScreenshotsApi = mock()
    private val dao: ScreenshotDao = mock()
    private val remoteKeysDao: ScreenshotRemoteKeysDao = mock()
    private val dtoMapper: ScreenshotDtoMapper = mock()
    private val entityMapper: ScreenshotEntityMapper = mock()
    private val transactionProvider = object : TransactionProvider {
        override suspend fun <T> run(block: suspend () -> T): T = block()
    }

    private val pagingConfig = PagingConfig(
        pageSize = 2,
        prefetchDistance = 1,
        initialLoadSize = 2,
        maxSize = 10,
    )

    @Test
    fun `load refresh inserts screenshots and remote keys`() = runTest {
        val dto = screenshotDto()
        val model = screenshotModel()
        val entity = screenshotEntity()

        stubApi(dto)
        stubMappers(dto, model, entity)

        val result = buildMediator().load(
            LoadType.REFRESH,
            pagingState(),
        )

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        verify(dao).clearForGame(10)
        verify(remoteKeysDao).clearForGame(10)
        verify(dao).upsertAll(listOf(entity))
        verify(remoteKeysDao).insertRemoteKeys(
            listOf(
                ScreenshotRemoteKeysEntity(
                    gameId = 10,
                    screenshotId = 1,
                    prevKey = null,
                    nextKey = 2,
                ),
            ),
        )
    }

    private fun screenshotDto(): ScreenshotDto = ScreenshotDto(
        id = 1,
        imageUrl = "image",
        width = 100,
        height = 50,
        isDeleted = false,
    )

    private fun screenshotModel(): Screenshot = Screenshot(
        id = 1,
        imageUrl = "image",
        width = 100,
        height = 50,
    )

    private fun screenshotEntity(): ScreenshotEntity = ScreenshotEntity(
        id = 1,
        gameId = 10,
        imageUrl = "image",
        width = 100,
        height = 50,
        insertionOrder = 0,
    )

    private fun stubApi(dto: ScreenshotDto) {
        api.stub {
            onBlocking { getScreenshots(10, 1, pagingConfig.pageSize) } doReturn
                NetworkResponse.Success(
                    PaginatedResponseDto(
                        count = 1,
                        next = "https://example.com?page=2",
                        previous = null,
                        results = listOf(dto),
                    ),
                )
        }
    }

    private fun stubMappers(
        dto: ScreenshotDto,
        model: Screenshot,
        entity: ScreenshotEntity,
    ) {
        dtoMapper.stub { on { map(dto) } doReturn model }
        entityMapper.stub { on { mapToEntity(model, 10, 0) } doReturn entity }
    }

    private fun buildMediator(): ScreenshotsRemoteMediator = ScreenshotsRemoteMediator(
        gameId = 10,
        screenshotsApi = api,
        screenshotDao = dao,
        remoteKeysDao = remoteKeysDao,
        dtoMapper = dtoMapper,
        entityMapper = entityMapper,
        transactionProvider = transactionProvider,
    )

    private fun pagingState(): PagingState<Int, ScreenshotEntity> =
        PagingState(emptyList(), null, pagingConfig, 0)
}
