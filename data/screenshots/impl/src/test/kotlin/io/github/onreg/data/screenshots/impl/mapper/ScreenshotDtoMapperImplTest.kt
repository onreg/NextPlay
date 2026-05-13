package io.github.onreg.data.screenshots.impl.mapper

import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.core.network.rawg.dto.ScreenshotDto
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ScreenshotDtoMapperImplTest {
    private val mapper = ScreenshotDtoMapperImpl()

    @Test
    fun `map copies dto fields and applies defaults`() {
        val dto = ScreenshotDto(
            id = 5,
            imageUrl = "https://img",
            width = 100,
            height = 200,
        )

        val result = mapper.map(
            dto = dto,
            gameId = 12,
            position = 3L,
        )

        assertEquals(
            ScreenshotEntity(
                id = 5,
                gameId = 12,
                position = 3L,
                imageUrl = "https://img",
                width = 100,
                height = 200,
            ),
            result,
        )
    }

    @Test
    fun `map replaces null image url with empty string`() {
        val dto = ScreenshotDto(
            id = 8,
            imageUrl = null,
            width = null,
            height = null,
        )

        val result = mapper.map(
            dto = dto,
            gameId = 44,
            position = 9L,
        )

        assertEquals(
            ScreenshotEntity(
                id = 8,
                gameId = 44,
                position = 9L,
                imageUrl = "",
                width = null,
                height = null,
            ),
            result,
        )
    }
}
