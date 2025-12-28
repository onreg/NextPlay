package io.github.onreg.feature.game.impl.pane

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.paging.LoadState
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.core.ui.components.card.PlatformUI
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.feature.game.impl.model.GamePaneState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import io.github.onreg.core.ui.R as CoreUiR

@RunWith(RobolectricTestRunner::class)
internal class GamesPaneTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val defaultCard = GameCardUI(
        id = "1",
        title = "Game",
        imageUrl = "image",
        releaseDate = "2024",
        platforms = setOf(PlatformUI(name = "PC", iconRes = CoreUiR.drawable.ic_controller_24)),
        rating = ChipUI(text = "4.2"),
        isBookmarked = false
    )

    @Test
    fun `no cached data and loading shows full screen loading`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(emptyList(), refresh = LoadState.Loading)
            .gamePaneState(GamePaneState.Ready)
            .build()

        driver.assertFullScreenLoadingDisplayed()
        driver.assertListIsNotDisplayed()
        driver.assertFullScreenErrorIsNotDisplayed()
    }

    @Test
    fun `no cached data and error shows full screen error`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(emptyList())
            .gamePaneState(GamePaneState.Error)
            .build()

        driver.assertFullScreenErrorDisplayed()
        driver.assertListIsNotDisplayed()
    }

    @Test
    fun `no cached data and refresh error shows full screen error`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(
                emptyList(),
                refresh = LoadState.Error(IllegalStateException("boom"))
            )
            .gamePaneState(GamePaneState.Ready)
            .build()

        driver.assertFullScreenErrorDisplayed()
        driver.assertListIsNotDisplayed()
    }

    @Test
    fun `no cached data and not loading shows empty state`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(emptyList())
            .gamePaneState(GamePaneState.Ready)
            .build()

        driver.assertEmptyStateDisplayed()
        driver.assertFullScreenErrorIsNotDisplayed()
    }

    @Test
    fun `cached data and append loading shows bottom loading`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard), append = LoadState.Loading)
            .gamePaneState(GamePaneState.Ready)
            .build()

        driver.assertListDisplayed()
        driver.assertAppendLoadingDisplayed()
    }

    @Test
    fun `cached data and append error shows bottom error`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(
                listOf(defaultCard),
                append = LoadState.Error(IllegalStateException("boom"))
            )
            .gamePaneState(GamePaneState.Ready)
            .build()

        driver.assertAppendErrorDisplayed()
    }

    @Test
    fun `full screen retry triggers callback`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(emptyList())
            .gamePaneState(GamePaneState.Error)
            .build()
        driver.clickRetryButton()
        driver.assertRetryCount(1)
    }

    @Test
    fun `append retry triggers callback`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(
                listOf(defaultCard),
                append = LoadState.Error(IllegalStateException("boom"))
            )
            .gamePaneState(GamePaneState.Ready)
            .build()
        driver.clickRetryButton()
        driver.assertPageRetryCount(1)
    }

    @Test
    fun `pull to refresh keeps list visible and shows indicator`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard), refresh = LoadState.Loading)
            .gamePaneState(GamePaneState.Ready)
            .build()

        driver.assertListDisplayed()
        driver.assertPullToRefreshIndicatorDisplayed()
    }

    @Test
    fun `bookmark click triggers callback`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .gamePaneState(GamePaneState.Ready)
            .build()
        driver.clickBookmarkButton()
        driver.assertLastBookmarkedId(defaultCard.id)
    }

    @Test
    fun `card click triggers callback`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .gamePaneState(GamePaneState.Ready)
            .build()
        driver.clickCard(defaultCard.id)
        driver.assertLastCardClickedId(defaultCard.id)
    }
}
