package io.github.onreg.feature.game.impl.pane

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.paging.LoadState
import io.github.onreg.core.ui.components.list.test.GameListTestData
import io.github.onreg.feature.game.impl.model.GamePaneState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class GamesPaneTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val defaultCard = GameListTestData.generateGameCards(1).first()

    @Test
    fun `shows full screen error`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(emptyList())
            .gamePaneState(GamePaneState.Error)
            .build()

        driver.assertListIsNotDisplayed()
        driver.assertFullScreenErrorDisplayed()
        driver.assertEmptyStateIsNotDisplayed()
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

        driver.assertListIsNotDisplayed()
        driver.assertFullScreenErrorDisplayed()
        driver.assertEmptyStateIsNotDisplayed()
    }

    @Test
    fun `no cached data and not loading shows empty state`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(emptyList())
            .gamePaneState(GamePaneState.Ready)
            .build()

        driver.assertListIsNotDisplayed()
        driver.assertEmptyStateDisplayed()
        driver.assertFullScreenErrorIsNotDisplayed()
    }

    @Test
    fun `should display list`(){
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .gamePaneState(GamePaneState.Ready)
            .build()
        driver.assertListDisplayed()
        driver.assertEmptyStateIsNotDisplayed()
        driver.assertFullScreenErrorIsNotDisplayed()
    }

    @Test
    fun `retry triggers callback`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(emptyList())
            .gamePaneState(GamePaneState.Error)
            .build()
        driver.clickRetryButton()
        driver.assertRetryCount(1)
    }

    @Test
    fun `retry after refresh error triggers callback`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(
                emptyList(),
                refresh = LoadState.Error(IllegalStateException("boom"))
            )
            .gamePaneState(GamePaneState.Ready)
            .build()

        driver.clickRetryButton()
        driver.assertPageRetryCount(1)
    }

    @Test
    fun `retry after append error triggers callback`() {
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
    fun `pull to refresh triggers callback`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .gamePaneState(GamePaneState.Ready)
            .build()

        driver.pullToRefresh()
        driver.assertRefreshCount(1)
    }

    @Test
    fun `bookmark click triggers callback`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .gamePaneState(GamePaneState.Ready)
            .build()
        driver.clickBookmarkButton()
        driver.assertBookmarkClicked(defaultCard.id)
    }

    @Test
    fun `card click triggers callback`() {
        val driver = GamesPaneTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .gamePaneState(GamePaneState.Ready)
            .build()
        driver.clickCard(defaultCard.id)
        driver.assertCardClicked(defaultCard.id)
    }
}
