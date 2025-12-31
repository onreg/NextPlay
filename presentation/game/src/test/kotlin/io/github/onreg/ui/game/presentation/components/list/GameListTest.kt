package io.github.onreg.ui.game.presentation.components.list

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.paging.LoadState
import io.github.onreg.ui.game.presentation.components.list.test.GameListTestData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class GameListTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val defaultCard = GameListTestData.twoItems[1]

    @Test
    fun `should show full screen loading when no cached data and loading`() {
        val driver = GameListTestDriver.Builder(composeRule)
            .pagingState(emptyList(), refresh = LoadState.Loading)
            .build()

        driver.assertFullScreenLoadingDisplayed()
        driver.assertListIsNotDisplayed()
        driver.assertErrorContentCount(0)
        driver.assertEmptyContentCount(0)
    }

    @Test
    fun `should show full screen error when no cached data and refresh error`() {
        val driver = GameListTestDriver.Builder(composeRule)
            .pagingState(
                emptyList(),
                refresh = LoadState.Error(IllegalStateException("boom"))
            )
            .build()

        driver.assertListIsNotDisplayed()
        driver.assertFullScreenLoadingIsNotDisplayed()
        driver.assertErrorContentCount(1)
        driver.assertEmptyContentCount(0)
    }

    @Test
    fun `should show empty state when no cached data and not loading`() {
        val driver = GameListTestDriver.Builder(composeRule)
            .pagingState(emptyList())
            .build()

        driver.assertListIsNotDisplayed()
        driver.assertFullScreenLoadingIsNotDisplayed()
        driver.assertErrorContentCount(0)
        driver.assertEmptyContentCount(1)
    }

    @Test
    fun `should show list when cached data is available`() {
        val driver = GameListTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .build()

        driver.assertListDisplayed()
        driver.assertFullScreenLoadingIsNotDisplayed()
        driver.assertAppendLoadingIsNotDisplayed()
        driver.assertAppendErrorIsNotDisplayed()
        driver.assertErrorContentCount(0)
        driver.assertEmptyContentCount(0)
    }

    @Test
    fun `should show append loading when cached data and appending`() {
        val driver = GameListTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard), append = LoadState.Loading)
            .build()

        driver.assertListDisplayed()
        driver.assertAppendLoadingDisplayed()
        driver.assertAppendErrorIsNotDisplayed()
    }

    @Test
    fun `should show append error when cached data and append fails`() {
        val driver = GameListTestDriver.Builder(composeRule)
            .pagingState(
                listOf(defaultCard),
                append = LoadState.Error(IllegalStateException("boom"))
            )
            .build()

        driver.assertListDisplayed()
        driver.assertAppendErrorDisplayed()
        driver.assertAppendLoadingIsNotDisplayed()
    }

    @Test
    fun `should trigger callback on retry after append error`() {
        val driver = GameListTestDriver.Builder(composeRule)
            .pagingState(
                listOf(defaultCard),
                append = LoadState.Error(IllegalStateException("boom"))
            )
            .build()
        driver.clickRetryButton()
        driver.assertPageRetryCount(1)
    }

    @Test
    fun `should keep list visible and show indicator when refreshing`() {
        val driver = GameListTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard), refresh = LoadState.Loading)
            .build()

        driver.assertListDisplayed()
        driver.assertPullToRefreshIndicatorDisplayed()
        driver.assertFullScreenLoadingIsNotDisplayed()
        driver.assertErrorContentCount(0)
        driver.assertEmptyContentCount(0)
    }

    @Test
    fun `should trigger callback on pull to refresh`() {
        val driver = GameListTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .build()

        driver.pullToRefresh()
        driver.assertRefreshCount(1)
    }

    @Test
    fun `should trigger callback on bookmark click`() {
        val driver = GameListTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .build()
        driver.clickBookmarkButton()
        driver.assertBookmarkClicked(defaultCard.id)
    }

    @Test
    fun `should trigger callback on card click`() {
        val driver = GameListTestDriver.Builder(composeRule)
            .pagingState(listOf(defaultCard))
            .build()
        driver.clickCard(defaultCard.id)
        driver.assertCardClicked(defaultCard.id)
    }
}
