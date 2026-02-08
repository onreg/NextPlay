package io.github.onreg.feature.game.details.impl.pane

import androidx.paging.LoadState
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

    @Test
    fun `section visibility should show content when there are items`() {
        val visibility = resolveSectionVisibility(
            itemCount = 1,
            refreshLoadState = LoadState.NotLoading(endOfPaginationReached = true),
        )

        assertEquals(SectionVisibility.ShowContent, visibility)
    }

    @Test
    fun `section visibility should show loading for pending empty state`() {
        val visibility = resolveSectionVisibility(
            itemCount = 0,
            refreshLoadState = LoadState.NotLoading(endOfPaginationReached = false),
        )

        assertEquals(SectionVisibility.ShowLoading, visibility)
    }

    @Test
    fun `section visibility should hide for terminal empty state`() {
        val visibility = resolveSectionVisibility(
            itemCount = 0,
            refreshLoadState = LoadState.NotLoading(endOfPaginationReached = true),
        )

        assertEquals(SectionVisibility.Hide, visibility)
    }
}
