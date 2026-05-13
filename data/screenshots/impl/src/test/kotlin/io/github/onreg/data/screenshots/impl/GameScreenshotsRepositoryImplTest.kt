package io.github.onreg.data.screenshots.impl

import androidx.paging.testing.asSnapshot
import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.data.screenshots.api.model.Screenshot
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GameScreenshotsRepositoryImplTest {
    private val entity = ScreenshotEntity(
        id = 1,
        gameId = 7,
        position = 0,
        imageUrl = "https://img",
        width = 100,
        height = 200,
    )
    private val mapped = Screenshot(
        id = 1,
        imageUrl = "https://img",
        width = 100,
        height = 200,
    )

    private val driver = GameScreenshotsRepositoryTestDriver
        .Builder()
        .daoPagingSource(listOf(entity))
        .entityMapperMap(entity, mapped)
        .build()

    @Test
    fun `getScreenshots maps entities and uses game scoped paging`() = runTest {
        val gameId = 7

        val items = driver.getScreenshots(gameId).asSnapshot()

        verify(driver.dao).pagingSource(gameId)
        verify(driver.remoteMediatorFactory).create(gameId)
        verify(driver.entityMapper).map(entity)
        assertEquals(listOf(mapped), items)
    }
}
