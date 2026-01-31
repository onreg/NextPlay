package io.github.onreg.ui.game.presentation.components.list

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.paging.LoadState
import io.github.onreg.ui.game.presentation.components.card.model.GameListErrorType
import io.github.onreg.ui.game.presentation.components.list.test.GameListTestData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
internal class GameListTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val defaultCard = GameListTestData.twoItems[1]

    @Test
    fun `should show full screen loading when no cached data and loading`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(emptyList(), refresh = LoadState.Loading)
            .build()

        driver.assertFullScreenLoadingDisplayed()
        driver.assertListIsNotDisplayed()
        driver.assertErrorCallbackNotTriggered()
        driver.assertEmptyCallbackNotTriggered()
    }

    @Test
    fun `should show full screen loading when no cached data and mediator loading`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(emptyList(), mediatorRefresh = LoadState.Loading)
            .build()

        driver.assertFullScreenLoadingDisplayed()
        driver.assertListIsNotDisplayed()
        driver.assertErrorCallbackNotTriggered()
        driver.assertEmptyCallbackNotTriggered()
    }

    @Test
    fun `should show full screen error when no cached data and refresh error`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(
                emptyList(),
                refresh = LoadState.Error(IllegalStateException("boom")),
            ).build()

        driver.assertListIsNotDisplayed()
        driver.assertFullScreenLoadingIsNotDisplayed()
        driver.assertErrorCallbackTriggered(GameListErrorType.OTHER)
        driver.assertEmptyCallbackNotTriggered()
    }

    @Test
    fun `should show full screen network error when no cached data and refresh error`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(
                emptyList(),
                refresh = LoadState.Error(IOException("boom")),
            ).build()

        driver.assertListIsNotDisplayed()
        driver.assertFullScreenLoadingIsNotDisplayed()
        driver.assertErrorCallbackTriggered(GameListErrorType.NETWORK)
        driver.assertEmptyCallbackNotTriggered()
    }

    @Test
    fun `should prefer mediator error when both refresh errors are present`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(
                emptyList(),
                refresh = LoadState.Error(IllegalStateException("boom")),
                mediatorRefresh = LoadState.Error(IOException("boom")),
            ).build()

        driver.assertListIsNotDisplayed()
        driver.assertFullScreenLoadingIsNotDisplayed()
        driver.assertErrorCallbackTriggered(GameListErrorType.NETWORK)
        driver.assertEmptyCallbackNotTriggered()
    }

    @Test
    fun `should show empty state when no cached data and end reached`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(
                emptyList(),
                append = LoadState.NotLoading(true),
            ).build()

        driver.assertListIsNotDisplayed()
        driver.assertFullScreenLoadingIsNotDisplayed()
        driver.assertErrorCallbackNotTriggered()
        driver.assertEmptyCallbackTriggered()
    }

    @Test
    fun `should show full screen loading when no cached data and initial load pending`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(emptyList())
            .build()

        driver.assertListIsNotDisplayed()
        driver.assertFullScreenLoadingDisplayed()
        driver.assertErrorCallbackNotTriggered()
        driver.assertEmptyCallbackNotTriggered()
    }

    @Test
    fun `should show list when cached data is available`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .build()

        driver.assertListDisplayed()
        driver.assertFullScreenLoadingIsNotDisplayed()
        driver.assertAppendLoadingIsNotDisplayed()
        driver.assertAppendErrorIsNotDisplayed()
        driver.assertErrorCallbackNotTriggered()
        driver.assertEmptyCallbackNotTriggered()
    }

    @Test
    fun `should keep list visible when cached data and refresh error`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(
                listOf(defaultCard),
                mediatorRefresh = LoadState.Error(IllegalStateException("boom")),
            ).build()

        driver.assertListDisplayed()
        driver.assertFullScreenLoadingIsNotDisplayed()
        driver.assertErrorCallbackNotTriggered()
        driver.assertEmptyCallbackNotTriggered()
    }

    @Test
    fun `should show append loading when cached data and appending`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(listOf(defaultCard), append = LoadState.Loading)
            .build()

        driver.assertListDisplayed()
        driver.assertAppendLoadingDisplayed()
        driver.assertAppendErrorIsNotDisplayed()
    }

    @Test
    fun `should show append error when cached data and append fails`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(
                listOf(defaultCard),
                append = LoadState.Error(IllegalStateException("boom")),
            ).build()

        driver.assertListDisplayed()
        driver.assertAppendErrorDisplayed()
        driver.assertErrorItemDisplayed()
        driver.assertAppendLoadingIsNotDisplayed()
    }

    @Test
    fun `should show network append error when cached data and append fails with network`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(
                listOf(defaultCard),
                append = LoadState.Error(IOException("boom")),
            ).build()

        driver.assertListDisplayed()
        driver.assertAppendErrorDisplayed()
        driver.assertNetworkErrorItemDisplayed()
        driver.assertAppendLoadingIsNotDisplayed()
    }

    @Test
    fun `should trigger callback on retry after append error`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(
                listOf(defaultCard),
                append = LoadState.Error(IllegalStateException("boom")),
            ).build()
        driver.clickRetryButton()
        driver.assertPageRetryCount(1)
    }

    @Test
    fun `should keep list visible and show indicator when refreshing`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(listOf(defaultCard), refresh = LoadState.Loading)
            .build()

        driver.assertListDisplayed()
        driver.assertPullToRefreshIndicatorDisplayed()
        driver.assertFullScreenLoadingIsNotDisplayed()
        driver.assertErrorCallbackNotTriggered()
        driver.assertEmptyCallbackNotTriggered()
    }

    @Test
    fun `should trigger callback on pull to refresh`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .build()

        driver.pullToRefresh()
        driver.assertRefreshCount(1)
    }

    @Test
    fun `should trigger callback on bookmark click`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .build()
        driver.clickBookmarkButton()
        driver.assertBookmarkClicked(defaultCard.id)
    }

    @Test
    fun `should trigger callback on card click`() {
        val driver = GameListTestDriver
            .Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .build()
        driver.clickCard(defaultCard.id)
        driver.assertCardClicked(defaultCard.id)
    }
}
