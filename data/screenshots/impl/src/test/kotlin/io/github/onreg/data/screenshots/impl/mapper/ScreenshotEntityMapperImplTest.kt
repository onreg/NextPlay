package io.github.onreg.data.screenshots.impl.mapper

import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.data.screenshots.api.model.Screenshot
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ScreenshotEntityMapperImplTest {
    private val mapper = ScreenshotEntityMapperImpl()

    @Test
    fun `map copies entity fields`() {
        val entity = ScreenshotEntity(
            id = 3,
            gameId = 77,
            position = 4,
            imageUrl = "https://img",
            width = 320,
            height = 180,
        )

        val result = mapper.map(entity)

        assertEquals(
            Screenshot(
                id = 3,
                imageUrl = "https://img",
                width = 320,
                height = 180,
            ),
            result,
        )
    }
}
