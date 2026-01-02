package io.github.onreg.ui.game.presentation.components.list

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
import io.github.onreg.ui.game.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.game.presentation.components.card.model.GameListErrorType
import io.github.onreg.ui.game.presentation.components.list.test.GameListTestTags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.assertEquals
import kotlin.test.assertNull
import io.github.onreg.ui.game.presentation.R as GamePresentationR

internal class GameListTestDriver private constructor(
    private val composeRule: ComposeContentTestRule
) {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val fullScreenLoadingNode =
        composeRule.onNodeWithTag(GameListTestTags.GAME_LIST_FULL_SCREEN_LOADING)

    private val listNode = composeRule.onNodeWithTag(GameListTestTags.GAME_LIST)

    private val appendLoadingNode =
        composeRule.onNodeWithTag(GameListTestTags.GAME_LIST_APPEND_LOADING)

    private val appendErrorNode =
        composeRule.onNodeWithTag(GameListTestTags.GAME_LIST_APPEND_ERROR)

    private val pullToRefreshIndicatorNode =
        composeRule.onNodeWithTag(GameListTestTags.GAME_LIST_PULL_TO_REFRESH_INDICATOR)

    private val retryButtonNode = composeRule.onNodeWithText(
        context.getString(GamePresentationR.string.retry)
    )

    private val errorTitle = composeRule.onNodeWithText(
        context.getString(GamePresentationR.string.games_error_title)
    )
    private val errorDescriptionNode = composeRule.onNodeWithText(
        context.getString(io.github.onreg.core.ui.R.string.error_message)
    )
    private val networkErrorDescriptionNode = composeRule.onNodeWithText(
        context.getString(io.github.onreg.core.ui.R.string.error_network_message)
    )

    private val errorItem = composeRule.onNodeWithTag(GameListTestTags.GAME_LIST_APPEND_ERROR)

    private val bookmarkButtonNode = composeRule.onNodeWithContentDescription(
        context.getString(GamePresentationR.string.add_bookmark)
    )
    private val cardNode: (String) -> SemanticsNodeInteraction =
        { cardId ->
            composeRule.onNodeWithTag("${GameListTestTags.GAME_LIST_CARD_PREFIX}$cardId")
        }

    private var refreshCount: Int = 0
    private var pageRetryCount: Int = 0
    private var errorContentCount: Int = 0
    private var emptyContentCount: Int = 0
    private var lastErrorType: GameListErrorType? = null
    private var lastBookmarkedId: String? = null
    private var lastCardClickedId: String? = null

    class Builder(private val composeRule: ComposeContentTestRule) {
        private val pagingState = MutableStateFlow<PagingData<GameCardUI>>(PagingData.empty())

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

        fun build(): GameListTestDriver {
            val driver = GameListTestDriver(composeRule)
            composeRule.setContent {
                val lazyPagingItems = pagingState.collectAsLazyPagingItems()
                GameList(
                    modifier = Modifier.fillMaxSize(),
                    lazyPagingItems = lazyPagingItems,
                    onRefresh = { driver.refreshCount += 1 },
                    onRetry = { driver.pageRetryCount += 1 },
                    onBookmarkClicked = { driver.lastBookmarkedId = it },
                    onCardClicked = { driver.lastCardClickedId = it },
                    onError = { errorType ->
                        driver.errorContentCount += 1
                        driver.lastErrorType = errorType
                    },
                    onEmpty = { driver.emptyContentCount += 1 }
                )
            }
            return driver
        }
    }

    fun assertFullScreenLoadingDisplayed() {
        fullScreenLoadingNode.assertIsDisplayed()
    }

    fun assertListDisplayed() {
        listNode.assertIsDisplayed()
    }

    fun assertListIsNotDisplayed() {
        listNode.assertIsNotDisplayed()
    }

    fun assertFullScreenLoadingIsNotDisplayed() {
        fullScreenLoadingNode.assertIsNotDisplayed()
    }

    fun assertAppendLoadingDisplayed() {
        appendLoadingNode.assertIsDisplayed()
    }

    fun assertAppendErrorDisplayed() {
        appendErrorNode.assertIsDisplayed()
    }

    fun assertAppendLoadingIsNotDisplayed() {
        appendLoadingNode.assertIsNotDisplayed()
    }

    fun assertAppendErrorIsNotDisplayed() {
        appendErrorNode.assertIsNotDisplayed()
    }

    fun asserErrorItemDisplayed() {
        errorItem.isDisplayed()
        errorTitle.isDisplayed()
        errorDescriptionNode.isDisplayed()
    }

    fun assertNetworkErrorItemDisplayed() {
        errorItem.isDisplayed()
        errorTitle.isDisplayed()
        networkErrorDescriptionNode.isDisplayed()
    }

    fun assertPullToRefreshIndicatorDisplayed() {
        pullToRefreshIndicatorNode.assertIsDisplayed()
    }

    fun assertErrorCallbackTriggered(type: GameListErrorType?) {
        composeRule.runOnIdle {
            assertEquals(1, errorContentCount)
            assertEquals(type, lastErrorType)
        }
    }

    fun assertErrorCallbackNotTriggered() {
        composeRule.runOnIdle {
            assertEquals(0, errorContentCount)
            assertNull(lastErrorType)
        }
    }

    fun assertEmptyCallbackTriggered() {
        composeRule.runOnIdle {
            assertEquals(1, emptyContentCount)
        }
    }

    fun assertEmptyCallbackNotTriggered() {
        composeRule.runOnIdle {
            assertEquals(0, emptyContentCount)
        }
    }

    fun assertRefreshCount(expected: Int) {
        composeRule.runOnIdle {
            assertEquals(expected, refreshCount)
        }
    }

    fun assertPageRetryCount(expected: Int) {
        composeRule.runOnIdle {
            assertEquals(expected, pageRetryCount)
        }
    }

    fun assertBookmarkClicked(expected: String?) {
        composeRule.runOnIdle {
            assertEquals(expected, lastBookmarkedId)
        }
    }

    fun assertCardClicked(expected: String?) {
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

    fun clickCard(cardId: String) {
        cardNode(cardId).performClick()
    }
}
