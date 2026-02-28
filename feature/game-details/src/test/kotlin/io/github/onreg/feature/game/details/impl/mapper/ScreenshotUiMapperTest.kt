package io.github.onreg.feature.game.details.impl.mapper

import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.feature.game.details.impl.model.ScreenshotUI
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenshotUiMapperTest {
    private val mapper = ScreenshotUiMapperImpl()

    @Test
    fun `should map screenshot imageUrl`() {
        val model = Screenshot(
            id = 1,
            imageUrl = "img",
            width = null,
            height = null,
        )

        val result = mapper.map(model = model)

        assertEquals(
            ScreenshotUI(imageUrl = "img"),
            result,
        )
    }
}
