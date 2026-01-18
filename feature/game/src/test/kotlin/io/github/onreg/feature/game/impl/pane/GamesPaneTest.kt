package io.github.onreg.feature.game.impl.pane

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.paging.LoadState
import io.github.onreg.ui.game.presentation.components.list.test.GameListTestData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
internal class GamesPaneTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val defaultCard = GameListTestData.generateGameCards(1).first()

    @Test
    fun `should show full screen error when refresh fails without cached data`() {
        val driver = GamesPaneTestDriver
            .Builder(composeRule)
            .pagingState(
                emptyList(),
                refresh = LoadState.Error(IllegalStateException("boom")),
            ).build()

        driver.assertListIsNotDisplayed()
        driver.asserErrorDisplayed()
        driver.assertEmptyStateIsNotDisplayed()
    }

    @Test
    fun `should show network error message when refresh fails with io exception`() {
        val driver = GamesPaneTestDriver
            .Builder(composeRule)
            .pagingState(
                emptyList(),
                refresh = LoadState.Error(IOException("boom")),
            ).build()

        driver.assertListIsNotDisplayed()
        driver.assertNetworkErrorMessageDisplayed()
        driver.assertEmptyStateIsNotDisplayed()
    }

    @Test
    fun `should show empty state when not loading and no cached data`() {
        val driver = GamesPaneTestDriver
            .Builder(composeRule)
            .pagingState(
                emptyList(),
                append = LoadState.NotLoading(true),
            ).build()

        driver.assertListIsNotDisplayed()
        driver.assertEmptyStateDisplayed()
        driver.assertFullScreenErrorIsNotDisplayed()
    }

    @Test
    fun `should display list`() {
        val driver = GamesPaneTestDriver
            .Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .build()
        driver.assertListDisplayed()
        driver.assertEmptyStateIsNotDisplayed()
        driver.assertFullScreenErrorIsNotDisplayed()
    }

    @Test
    fun `should trigger callback on retry after refresh error`() {
        val driver = GamesPaneTestDriver
            .Builder(composeRule)
            .pagingState(
                emptyList(),
                refresh = LoadState.Error(IllegalStateException("boom")),
            ).build()

        driver.clickRetryButton()
        driver.assertPageRetryClicked()
    }

    @Test
    fun `should trigger callback on retry after append error`() {
        val driver = GamesPaneTestDriver
            .Builder(composeRule)
            .pagingState(
                listOf(defaultCard),
                append = LoadState.Error(IllegalStateException("boom")),
            ).build()

        driver.clickRetryButton()
        driver.assertPageRetryClicked()
    }

    @Test
    fun `should trigger callback on pull to refresh`() {
        val driver = GamesPaneTestDriver
            .Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .build()

        driver.pullToRefresh()
        driver.assertRefreshClicked()
    }

    @Test
    fun `should trigger callback on bookmark click`() {
        val driver = GamesPaneTestDriver
            .Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .build()
        driver.clickBookmarkButton()
        driver.assertBookmarkClicked(defaultCard.id)
    }

    @Test
    fun `should trigger callback on card click`() {
        val driver = GamesPaneTestDriver
            .Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .build()
        driver.clickCard(defaultCard.id)
        driver.assertCardClicked(defaultCard.id)
    }
}
