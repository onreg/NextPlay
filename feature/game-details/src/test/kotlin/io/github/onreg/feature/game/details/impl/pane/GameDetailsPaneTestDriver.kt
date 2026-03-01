package io.github.onreg.feature.game.details.impl.pane

import android.content.Context
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.click
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.ui.components.header.AppHeaderUi
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.model.MovieUI
import io.github.onreg.feature.game.details.impl.model.ScreenshotUI
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestTags
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.assertEquals
import io.github.onreg.core.ui.R as CoreUiR
import io.github.onreg.feature.game.details.impl.R as DetailsR

internal class GameDetailsPaneTestDriver private constructor(
    private val composeRule: ComposeContentTestRule,
) {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val loadingNode = composeRule.onNodeWithTag(GameDetailsTestTags.LOADING)
    private val errorNode = composeRule.onNodeWithTag(GameDetailsTestTags.ERROR)
    private val contentNode = composeRule.onNodeWithTag(GameDetailsTestTags.CONTENT)

    private val backNode = composeRule.onNodeWithContentDescription(
        context.getString(CoreUiR.string.back),
    )
    private val retryNode = composeRule.onAllNodesWithText(
        context.getString(CoreUiR.string.error_message),
    )
    private val websiteNode = composeRule.onNodeWithText(
        context.getString(DetailsR.string.official_website),
    )
    private val bookmarkNode = composeRule.onNodeWithContentDescription(
        context.getString(CoreUiR.string.bookmark_remove),
    )
    private val networkErrorDescriptionNode = composeRule.onNodeWithText(
        context.getString(CoreUiR.string.error_network_message),
    )

    var backClicks: Int = 0
        private set
    var retryClicks: Int = 0
        private set
    var websiteClicks: Int = 0
        private set
    var bookmarkClicks: Int = 0
        private set
    var descriptionToggleClicks: Int = 0
        private set
    var lastScreenshotClickedUrl: String? = null
        private set
    var lastMovieClickedUrl: String? = null
        private set
    var lastSeriesClickedId: Int? = null
        private set

    private var descriptionToggleText: String? = null

    class Builder(private val composeRule: ComposeContentTestRule) {
        private var state: GameDetailsState = GameDetailsTestData.readyState
        private var screenshots: Flow<PagingData<ScreenshotUI>> = GameDetailsTestData.screenshots
        private var movies: Flow<PagingData<MovieUI>> = GameDetailsTestData.movies
        private var series: Flow<PagingData<GameCardUI>> = GameDetailsTestData.seriesState

        fun state(value: GameDetailsState): Builder = apply {
            state = value
        }

        fun screenshots(value: Flow<PagingData<ScreenshotUI>>): Builder = apply {
            screenshots = value
        }

        fun movies(value: Flow<PagingData<MovieUI>>): Builder = apply {
            movies = value
        }

        fun series(value: Flow<PagingData<GameCardUI>>): Builder = apply {
            series = value
        }

        fun build(): GameDetailsPaneTestDriver {
            val driver = GameDetailsPaneTestDriver(composeRule)
            driver.descriptionToggleText = (state as? GameDetailsState.Ready)
                ?.details
                ?.gameDescriptionUi
                ?.descriptionToggleUi
                ?.text

            composeRule.setContent {
                NextPlayTheme {
                    Surface {
                        GameDetailsPaneScreen(
                            gameDetailsState = state,
                            screenshots = screenshots,
                            movies = movies,
                            series = series,
                            onBackClicked = { driver.backClicks += 1 },
                            onRetry = { driver.retryClicks += 1 },
                            onWebsiteClicked = { driver.websiteClicks += 1 },
                            onBookmarkClicked = { driver.bookmarkClicks += 1 },
                            onDescriptionToggleClicked = { driver.descriptionToggleClicks += 1 },
                            onScreenshotClicked = { driver.lastScreenshotClickedUrl = it },
                            onMovieClicked = { driver.lastMovieClickedUrl = it },
                            onSeriesClicked = { driver.lastSeriesClickedId = it },
                        )
                    }
                }
            }
            return driver
        }
    }

    fun assertLoadingBranchDisplayed() {
        loadingNode.assertIsDisplayed()
        composeRule.onAllNodesWithTag(GameDetailsTestTags.ERROR).assertCountEquals(0)
        composeRule.onAllNodesWithTag(GameDetailsTestTags.CONTENT).assertCountEquals(0)
    }

    fun assertErrorBranchDisplayed() {
        errorNode.assertIsDisplayed()
        composeRule.onAllNodesWithTag(GameDetailsTestTags.LOADING).assertCountEquals(0)
        composeRule.onAllNodesWithTag(GameDetailsTestTags.CONTENT).assertCountEquals(0)
    }

    fun assertContentBranchDisplayed() {
        contentNode.assertIsDisplayed()
        composeRule.onAllNodesWithTag(GameDetailsTestTags.LOADING).assertCountEquals(0)
        composeRule.onAllNodesWithTag(GameDetailsTestTags.ERROR).assertCountEquals(0)
    }

    fun assertNetworkErrorMessageDisplayed() {
        networkErrorDescriptionNode.assertIsDisplayed()
    }

    fun clickBack() {
        backNode.performClick()
    }

    fun clickRetry() {
        val currentRetryClicks = retryClicks
        clickFirstMatchingNode(
            nodes = retryNode,
            clicked = { retryClicks > currentRetryClicks },
        )
    }

    fun clickWebsite() {
        websiteNode.performClick()
    }

    fun clickBookmark() {
        bookmarkNode.performClick()
    }

    fun clickDescriptionToggle() {
        composeRule.onNodeWithText(requireNotNull(descriptionToggleText)).performClick()
    }

    fun clickScreenshot(expectedId: Int) {
        composeRule.onNodeWithTag(GameDetailsTestTags.SCREENSHOTS_SECTION).performScrollTo()
        val itemTag = GameDetailsTestTags.screenshotItemTag(expectedId)
        composeRule.onNodeWithTag(itemTag, useUnmergedTree = false).performScrollTo()
        composeRule.onNodeWithTag(itemTag, useUnmergedTree = false).performClick()
    }

    fun clickMovie(expectedId: Int) {
        composeRule.onNodeWithTag(GameDetailsTestTags.MOVIES_SECTION).performScrollTo()
        val itemTag = GameDetailsTestTags.movieItemTag(expectedId)
        composeRule.onNodeWithTag(itemTag, useUnmergedTree = false).performScrollTo()
        composeRule.onNodeWithTag(itemTag, useUnmergedTree = false).performClick()
    }

    fun clickSeriesGame(expectedId: Int) {
        composeRule.onNodeWithTag(GameDetailsTestTags.SERIES_SECTION).performScrollTo()
        val itemTag = GameDetailsTestTags.seriesItemTag(expectedId)
        composeRule.onNodeWithTag(itemTag, useUnmergedTree = false).performScrollTo()
        composeRule.onNodeWithTag(itemTag, useUnmergedTree = false).performClick()
    }

    fun assertScreenshotsSectionExists() {
        composeRule.onAllNodesWithTag(GameDetailsTestTags.SCREENSHOTS_SECTION).assertCountEquals(1)
    }

    fun assertMoviesSectionExists() {
        composeRule.onAllNodesWithTag(GameDetailsTestTags.MOVIES_SECTION).assertCountEquals(1)
    }

    fun assertSeriesSectionExists() {
        composeRule.onAllNodesWithTag(GameDetailsTestTags.SERIES_SECTION).assertCountEquals(1)
    }

    fun assertScreenshotsSectionDoesNotExist() {
        composeRule.onAllNodesWithTag(GameDetailsTestTags.SCREENSHOTS_SECTION).assertCountEquals(0)
    }

    fun assertMoviesSectionDoesNotExist() {
        composeRule.onAllNodesWithTag(GameDetailsTestTags.MOVIES_SECTION).assertCountEquals(0)
    }

    fun assertSeriesSectionDoesNotExist() {
        composeRule.onAllNodesWithTag(GameDetailsTestTags.SERIES_SECTION).assertCountEquals(0)
    }

    fun assertRetryClicks(expected: Int) {
        composeRule.runOnIdle {
            assertEquals(expected, retryClicks)
        }
    }

    fun assertBackClicks(expected: Int) {
        composeRule.runOnIdle {
            assertEquals(expected, backClicks)
        }
    }

    fun assertWebsiteClicks(expected: Int) {
        composeRule.runOnIdle {
            assertEquals(expected, websiteClicks)
        }
    }

    fun assertBookmarkClicks(expected: Int) {
        composeRule.runOnIdle {
            assertEquals(expected, bookmarkClicks)
        }
    }

    fun assertDescriptionToggleClicks(expected: Int) {
        composeRule.runOnIdle {
            assertEquals(expected, descriptionToggleClicks)
        }
    }

    fun assertLastScreenshotClickedUrl(expected: String?) {
        composeRule.runOnIdle {
            assertEquals(expected, lastScreenshotClickedUrl)
        }
    }

    fun assertLastMovieClickedUrl(expected: String?) {
        composeRule.runOnIdle {
            assertEquals(expected, lastMovieClickedUrl)
        }
    }

    fun assertLastSeriesClickedId(expected: Int?) {
        composeRule.runOnIdle {
            assertEquals(expected, lastSeriesClickedId)
        }
    }

    private fun clickFirstMatchingNode(
        nodes: SemanticsNodeInteractionCollection,
        clicked: () -> Boolean,
    ) {
        val semanticsNodes = nodes.fetchSemanticsNodes()
        for (index in semanticsNodes.indices) {
            val isClicked = runCatching {
                nodes[index].performScrollTo()
                nodes[index].performTouchInput {
                    click()
                }
            }.isSuccess
            if (!isClicked) {
                continue
            }
            composeRule.waitForIdle()
            if (clicked()) {
                return
            }
        }
        error("Failed to click expected node")
    }

    companion object {
        fun <T : Any> loaded(items: List<T>): Flow<PagingData<T>> = flowOf(
            PagingData.from(
                data = items,
                sourceLoadStates = LoadStates(
                    refresh = LoadState.NotLoading(endOfPaginationReached = false),
                    prepend = LoadState.NotLoading(endOfPaginationReached = false),
                    append = LoadState.NotLoading(endOfPaginationReached = false),
                ),
            ),
        )

        fun <T : Any> loading(): Flow<PagingData<T>> = flowOf(
            PagingData.empty(
                sourceLoadStates = LoadStates(
                    refresh = LoadState.Loading,
                    prepend = LoadState.NotLoading(endOfPaginationReached = false),
                    append = LoadState.NotLoading(endOfPaginationReached = false),
                ),
            ),
        )

        fun <T : Any> empty(): Flow<PagingData<T>> = flowOf(
            PagingData.from(
                data = emptyList(),
                sourceLoadStates = LoadStates(
                    refresh = LoadState.NotLoading(endOfPaginationReached = false),
                    prepend = LoadState.NotLoading(endOfPaginationReached = false),
                    append = LoadState.NotLoading(endOfPaginationReached = true),
                ),
            ),
        )

        fun <T : Any> error(throwable: Throwable): Flow<PagingData<T>> = flowOf(
            PagingData.from(
                data = emptyList(),
                sourceLoadStates = LoadStates(
                    refresh = LoadState.Error(throwable),
                    prepend = LoadState.NotLoading(endOfPaginationReached = false),
                    append = LoadState.NotLoading(endOfPaginationReached = false),
                ),
            ),
        )

        fun loadingState(): GameDetailsState = GameDetailsState.Loading(AppHeaderUi())

        fun errorState(): GameDetailsState = GameDetailsState.Error(AppHeaderUi())
    }
}
