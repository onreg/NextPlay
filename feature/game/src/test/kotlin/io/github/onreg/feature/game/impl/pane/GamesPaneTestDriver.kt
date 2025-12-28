package io.github.onreg.feature.game.impl.pane

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.ui.components.card.GameCardTestTags
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.core.ui.components.list.test.GameListTestTags
import io.github.onreg.feature.game.impl.model.GamePaneState
import io.github.onreg.feature.game.impl.test.GamesPaneTestTags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.assertEquals
import io.github.onreg.core.ui.R as CoreUiR

internal class GamesPaneTestDriver private constructor(
    private val composeRule: ComposeContentTestRule
) {
    private val fullScreenErrorNode =
        composeRule.onNodeWithTag(GamesPaneTestTags.TAG_COMPONENT_ERROR)

    private val emptyStateNode = composeRule.onNodeWithTag(GamesPaneTestTags.TAG_COMPONENT_EMPTY)

    private val listNode = composeRule.onNodeWithTag(GameListTestTags.GAME_LIST)

    private val pullToRefreshIndicatorNode =
        composeRule.onNodeWithTag(GameListTestTags.GAME_LIST_PULL_TO_REFRESH_INDICATOR)

    private val retryButtonNode = composeRule.onNodeWithText(
        ApplicationProvider.getApplicationContext<Context>().getString(CoreUiR.string.retry)
    )
    private val bookmarkButtonNode =
        composeRule.onNodeWithTag(GameCardTestTags.GAME_CARD_ADD_BOOKMARK_BUTTON)

    private val cardNode: (String) -> SemanticsNodeInteraction =
        { cardId ->
            composeRule.onNodeWithTag("${GameListTestTags.GAME_LIST_CARD_PREFIX}$cardId")
        }

    private var retryCount: Int = 0
    private var pageRetryCount: Int = 0
    private var refreshCount: Int = 0
    private var lastBookmarkedId: String? = null
    private var lastCardClickedId: String? = null

    class Builder(private val composeRule: ComposeContentTestRule) {
        private val gamePaneState = MutableStateFlow<GamePaneState>(GamePaneState.Ready)
        private val pagingState = MutableStateFlow<PagingData<GameCardUI>>(PagingData.empty())

        fun gamePaneState(state: GamePaneState): Builder = apply {
            gamePaneState.value = state
        }

        fun pagingState(
            data: List<GameCardUI>,
            refresh: LoadState = LoadState.NotLoading(false),
            append: LoadState = LoadState.NotLoading(false),
            prepend: LoadState = LoadState.NotLoading(false)
        ): Builder = apply {
            pagingState.value = PagingData.from(
                data,
                sourceLoadStates = LoadStates(
                    refresh = refresh,
                    append = append,
                    prepend = prepend
                )
            )
        }

        fun build(isLargeScreen: Boolean = false): GamesPaneTestDriver {
            val driver = GamesPaneTestDriver(composeRule)
            composeRule.setContent {
                val lazyPagingItems = pagingState.collectAsLazyPagingItems()
                val currentState by gamePaneState.collectAsState()
                GamesPaneScreen(
                    isLargeScreen = isLargeScreen,
                    gamePaneState = currentState,
                    pagingState = lazyPagingItems,
                    onRetry = { driver.retryCount += 1 },
                    onRefreshClicked = { driver.refreshCount += 1 },
                    onPageRetryClicked = { driver.pageRetryCount += 1 },
                    onBookMarkClicked = { driver.lastBookmarkedId = it },
                    onCardClicked = { driver.lastCardClickedId = it }
                )
            }
            return driver
        }
    }

    fun assertFullScreenErrorDisplayed() {
        fullScreenErrorNode.assertIsDisplayed()
    }

    fun assertEmptyStateDisplayed() {
        emptyStateNode.assertIsDisplayed()
    }

    fun assertEmptyStateIsNotDisplayed() {
        emptyStateNode.assertIsNotDisplayed()
    }

    fun assertListDisplayed() {
        listNode.assertIsDisplayed()
    }

    fun assertListIsNotDisplayed() {
        listNode.assertIsNotDisplayed()
    }

    fun assertFullScreenErrorIsNotDisplayed() {
        fullScreenErrorNode.assertIsNotDisplayed()
    }

    fun assertPullToRefreshIndicatorDisplayed() {
        pullToRefreshIndicatorNode.assertIsDisplayed()
    }

    fun assertRefreshCount(expected: Int) {
        composeRule.runOnIdle {
            assertEquals(expected, refreshCount)
        }
    }

    fun assertRetryCount(expected: Int) {
        composeRule.runOnIdle {
            assertEquals(expected, retryCount)
        }
    }

    fun assertPageRetryCount(expected: Int) {
        composeRule.runOnIdle {
            assertEquals(expected, pageRetryCount)
        }
    }

    fun assertBookmarkClicked(expected: String) {
        composeRule.runOnIdle {
            assertEquals(expected, lastBookmarkedId)
        }
    }

    fun assertCardClicked(expected: String) {
        composeRule.runOnIdle {
            assertEquals(expected, lastCardClickedId)
        }
    }

    fun clickRetryButton() {
        retryButtonNode.performClick()
    }

    fun pullToRefresh() {
        listNode.performTouchInput { swipeDown() }
    }

    fun clickBookmarkButton() {
        bookmarkButtonNode.performClick()
    }

    fun clickCard(id: String) {
        cardNode(id).performClick()
    }
}
