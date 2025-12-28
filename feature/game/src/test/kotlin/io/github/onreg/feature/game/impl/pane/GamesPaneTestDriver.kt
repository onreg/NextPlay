package io.github.onreg.feature.game.impl.pane

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.feature.game.impl.model.GamePaneState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.assertEquals
import io.github.onreg.core.ui.R as CoreUiR

internal class GamesPaneTestDriver private constructor(
    private val composeRule: ComposeContentTestRule
) {
    private val fullScreenLoadingNode =
        composeRule.onNodeWithTag(GamesPaneTestTags.GAME_PANE_FULL_SCREEN_LOADING)

    private val fullScreenErrorNode =
        composeRule.onNodeWithTag(GamesPaneTestTags.GAME_PANE_FULL_SCREEN_ERROR)

    private val emptyStateNode = composeRule.onNodeWithTag(GamesPaneTestTags.GAME_PANE_EMPTY)

    private val listNode = composeRule.onNodeWithTag(GamesPaneTestTags.GAME_PANE_LIST)

    private val appendLoadingNode =
        composeRule.onNodeWithTag(GamesPaneTestTags.GAME_PANE_APPEND_LOADING)

    private val appendErrorNode =
        composeRule.onNodeWithTag(GamesPaneTestTags.GAME_PANE_APPEND_ERROR)

    private val pullToRefreshIndicatorNode =
        composeRule.onNodeWithTag(GamesPaneTestTags.GAME_PANE_PULL_TO_REFRESH_INDICATOR)

    private val retryButtonNode = composeRule.onNodeWithText(
        ApplicationProvider.getApplicationContext<android.content.Context>()
            .getString(CoreUiR.string.retry)
    )
    private val bookmarkButtonNode = composeRule.onNodeWithContentDescription(
        ApplicationProvider.getApplicationContext<android.content.Context>()
            .getString(CoreUiR.string.add_bookmark)
    )
    private val cardNode: (String) -> SemanticsNodeInteraction =
        { cardId ->
            composeRule.onNodeWithTag("${GamesPaneTestTags.GAME_PANE_CARD_PREFIX}$cardId")
        }

    private var retryCount: Int = 0
    private var refreshCount: Int = 0
    private var pageRetryCount: Int = 0
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

    fun assertFullScreenLoadingDisplayed() {
        fullScreenLoadingNode.assertIsDisplayed()
    }

    fun assertFullScreenErrorDisplayed() {
        fullScreenErrorNode.assertIsDisplayed()
    }

    fun assertEmptyStateDisplayed() {
        emptyStateNode.assertIsDisplayed()
    }

    fun assertListDisplayed() {
        listNode.assertIsDisplayed()
    }

    fun assertListIsNotDisplayed() {
        listNode.assertIsNotDisplayed()
    }

    fun assertAppendLoadingDisplayed() {
        appendLoadingNode.assertIsDisplayed()
    }

    fun assertAppendErrorDisplayed() {
        appendErrorNode.assertIsDisplayed()
    }

    fun assertFullScreenErrorIsNotDisplayed() {
        fullScreenErrorNode.assertIsNotDisplayed()
    }

    fun assertPullToRefreshIndicatorDisplayed() {
        pullToRefreshIndicatorNode.assertIsDisplayed()
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

    fun assertLastBookmarkedId(expected: String?) {
        composeRule.runOnIdle {
            assertEquals(expected, lastBookmarkedId)
        }
    }

    fun assertLastCardClickedId(expected: String?) {
        composeRule.runOnIdle {
            assertEquals(expected, lastCardClickedId)
        }
    }

    fun clickRetryButton() {
        retryButtonNode.performClick()
    }

    fun clickBookmarkButton() {
        bookmarkButtonNode.performClick()
    }

    fun clickCard(cardId: String) {
        cardNode(cardId).performClick()
    }
}
