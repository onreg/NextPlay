package io.github.onreg.feature.game.details.impl.pane

import kotlin.test.Test
import kotlin.test.assertEquals

internal class GameDetailsPaneHelpersTest {
    @Test
    fun `collapsed description max lines should be six`() {
        assertEquals(6, COLLAPSED_DESCRIPTION_MAX_LINES)
    }

    @Test
    fun `read more text should switch by expansion state`() {
        assertEquals("Read more", readMoreText(isExpanded = false))
        assertEquals("Read less", readMoreText(isExpanded = true))
    }
}
