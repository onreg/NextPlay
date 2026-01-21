package io.github.onreg.feature.game.impl.pane

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isDisplayed
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
import io.github.onreg.feature.game.impl.test.GamesPaneTestTags
import io.github.onreg.ui.game.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.game.presentation.components.card.test.GameCardTestTags
import io.github.onreg.ui.game.presentation.components.list.test.GameListTestTags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.assertEquals
import io.github.onreg.ui.game.presentation.R as GamePresentationR

internal class GamesPaneTestDriver private constructor(
    private val composeRule: ComposeContentTestRule,
) {
    private val emptyStateNode = composeRule.onNodeWithTag(GamesPaneTestTags.TAG_COMPONENT_EMPTY)

    private val listNode = composeRule.onNodeWithTag(GameListTestTags.GAME_LIST)

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val retryButtonNode = composeRule.onNodeWithText(
        context.getString(GamePresentationR.string.retry),
    )
    private val bookmarkButtonNode =
        composeRule.onNodeWithTag(GameCardTestTags.GAME_CARD_ADD_BOOKMARK_BUTTON)

    private val errorNode = composeRule.onNodeWithTag(GamesPaneTestTags.TAG_COMPONENT_ERROR)

    private val errorTitleNode = composeRule.onNodeWithText(
        context.getString(GamePresentationR.string.games_error_title),
    )
    private val errorDescriptionNode = composeRule.onNodeWithText(
        context.getString(io.github.onreg.core.ui.R.string.error_message),
    )
    private val networkErrorDescriptionNode = composeRule.onNodeWithText(
        context.getString(io.github.onreg.core.ui.R.string.error_network_message),
    )

    private val cardNode: (String) -> SemanticsNodeInteraction =
        { cardId ->
            composeRule.onNodeWithTag("${GameListTestTags.GAME_LIST_CARD_PREFIX}$cardId")
        }

    private var pageRetryCount: Int = 0
    private var refreshCount: Int = 0
    private var lastBookmarkedId: String? = null
    private var lastCardClickedId: String? = null

    class Builder(private val composeRule: ComposeContentTestRule) {
        private val pagingState = MutableStateFlow<PagingData<GameCardUI>>(PagingData.empty())

        fun pagingState(
            data: List<GameCardUI>,
            refresh: LoadState = LoadState.NotLoading(false),
            append: LoadState = LoadState.NotLoading(false),
            prepend: LoadState = LoadState.NotLoading(false),
        ): Builder = apply {
            pagingState.value = PagingData.from(
                data,
                sourceLoadStates = LoadStates(
                    refresh = refresh,
                    append = append,
                    prepend = prepend,
                ),
            )
        }

        fun build(isLargeScreen: Boolean = false): GamesPaneTestDriver {
            val driver = GamesPaneTestDriver(composeRule)
            composeRule.setContent {
                val lazyPagingItems = pagingState.collectAsLazyPagingItems()
                GamesPaneScreen(
                    isLargeScreen = isLargeScreen,
                    pagingState = lazyPagingItems,
                    onRefreshClicked = { driver.refreshCount += 1 },
                    onRetryClicked = { driver.pageRetryCount += 1 },
                    onBookMarkClicked = { driver.lastBookmarkedId = it },
                    onCardClicked = { driver.lastCardClickedId = it },
                )
            }
            return driver
        }
    }

    fun asserErrorDisplayed() {
        errorNode.isDisplayed()
        errorTitleNode.assertIsDisplayed()
        errorDescriptionNode.assertIsDisplayed()
    }

    fun assertNetworkErrorMessageDisplayed() {
        errorNode.isDisplayed()
        errorTitleNode.assertIsDisplayed()
        networkErrorDescriptionNode.assertIsDisplayed()
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
        errorNode.assertIsNotDisplayed()
    }

    fun assertRefreshClicked() {
        composeRule.runOnIdle {
            assertEquals(1, refreshCount)
        }
    }

    fun assertPageRetryClicked() {
        composeRule.runOnIdle {
            assertEquals(1, pageRetryCount)
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
