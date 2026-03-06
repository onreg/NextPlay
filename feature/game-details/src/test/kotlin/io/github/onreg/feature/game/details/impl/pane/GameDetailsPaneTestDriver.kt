package io.github.onreg.feature.game.details.impl.pane

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.model.MovieUI
import io.github.onreg.feature.game.details.impl.model.ScreenshotUI
import io.github.onreg.feature.game.details.impl.test.GameDetailsPaneTestTags
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import kotlinx.coroutines.flow.Flow
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import androidx.compose.ui.unit.dp
import io.github.onreg.core.ui.R as CoreUiR
import io.github.onreg.feature.game.details.impl.R as DetailsR

internal class GameDetailsPaneTestDriver private constructor(
    private val composeRule: ComposeContentTestRule,
) {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val contentNode =
        composeRule.onNodeWithTag(GameDetailsPaneTestTags.GAME_DETAILS_CONTENT)

    private val loadingNode =
        composeRule.onNodeWithTag(GameDetailsPaneTestTags.GAME_DETAILS_LOADING)

    private val errorNode =
        composeRule.onNodeWithTag(GameDetailsPaneTestTags.GAME_DETAILS_ERROR)

    private val descriptionNode = composeRule.onNodeWithText(
        GameDetailsTestData.readyState.details.gameDescriptionUi.description,
        substring = true,
    )

    private val officialWebsiteNode = composeRule.onNodeWithText(
        context.getString(DetailsR.string.official_website),
    )

    private val backButtonNode = composeRule.onNodeWithContentDescription(
        context.getString(CoreUiR.string.back),
    )

    private val removeBookmarkButtonNode = composeRule.onNodeWithContentDescription(
        context.getString(CoreUiR.string.bookmark_remove),
    )

    private val readMoreNode = composeRule.onNodeWithText(
        context.getString(DetailsR.string.read_more),
    )

    private val companiesSectionNode =
        composeRule.onNodeWithTag(GameDetailsPaneTestTags.GAME_DETAILS_COMPANIES_SECTION)

    private val screenshotsSectionNode =
        composeRule.onNodeWithTag(GameDetailsPaneTestTags.GAME_DETAILS_SCREENSHOTS_SECTION)

    private val moviesSectionNode =
        composeRule.onNodeWithTag(GameDetailsPaneTestTags.GAME_DETAILS_MOVIES_SECTION)

    private val seriesSectionNode =
        composeRule.onNodeWithTag(GameDetailsPaneTestTags.GAME_DETAILS_SERIES_SECTION)

    private val screenshotNode: (Int) -> SemanticsNodeInteraction =
        { screenshotId ->
            composeRule.onNodeWithTag(
                "${GameDetailsPaneTestTags.GAME_DETAILS_SCREENSHOT_PREFIX}$screenshotId",
            )
        }

    private val movieNode: (Int) -> SemanticsNodeInteraction =
        { movieId ->
            composeRule.onNodeWithTag(
                "${GameDetailsPaneTestTags.GAME_DETAILS_MOVIE_PREFIX}$movieId",
            )
        }

    private val seriesNode: (Int) -> SemanticsNodeInteraction =
        { gameId ->
            composeRule.onNodeWithTag(
                "${GameDetailsPaneTestTags.GAME_DETAILS_SERIES_PREFIX}$gameId",
            )
        }

    private val errorDescriptionNode = composeRule.onNodeWithText(
        context.getString(CoreUiR.string.error_network_message),
    )

    private var backClickedCount: Int = 0
    private var websiteClickedCount: Int = 0
    private var bookmarkClickedCount: Int = 0
    private var descriptionToggleClickedCount: Int = 0
    private var lastScreenshotUrl: String? = null
    private var lastMovieUrl: String? = null
    private var lastSeriesGameId: Int? = null
    private val overflowValues = mutableListOf<Boolean>()

    class Builder(private val composeRule: ComposeContentTestRule) {
        private var gameDetailsState: GameDetailsState = GameDetailsTestData.readyState
        private var screenshots: Flow<PagingData<ScreenshotUI>> = GameDetailsTestData.screenshots
        private var movies: Flow<PagingData<MovieUI>> = GameDetailsTestData.movies
        private var series: Flow<PagingData<GameCardUI>> = GameDetailsTestData.seriesState

        fun loading(): Builder = apply {
            gameDetailsState = GameDetailsTestData.loadingState
            screenshots = GameDetailsTestData.emptyScreenshots
            movies = GameDetailsTestData.emptyMovies
            series = GameDetailsTestData.emptySeries
        }

        fun error(): Builder = apply {
            gameDetailsState = GameDetailsState.Error(
                headerUi = GameDetailsTestData.readyState.headerUi,
            )
            screenshots = GameDetailsTestData.emptyScreenshots
            movies = GameDetailsTestData.emptyMovies
            series = GameDetailsTestData.emptySeries
        }

        fun withoutCompanies(): Builder = apply {
            val readyState = gameDetailsState as? GameDetailsState.Ready ?: GameDetailsTestData.readyState
            gameDetailsState = readyState.copy(
                details = readyState.details.copy(companies = emptyList()),
            )
        }

        fun withoutMediaSections(): Builder = apply {
            screenshots = GameDetailsTestData.emptyScreenshots
            movies = GameDetailsTestData.emptyMovies
            series = GameDetailsTestData.emptySeries
        }

        fun withLongDescription(): Builder = apply {
            val readyState = gameDetailsState as? GameDetailsState.Ready ?: GameDetailsTestData.readyState
            gameDetailsState = readyState.copy(
                details = readyState.details.copy(
                    gameDescriptionUi = readyState.details.gameDescriptionUi.copy(
                        description = GameDetailsTestData.longDescription,
                    ),
                ),
            )
        }

        fun build(): GameDetailsPaneTestDriver {
            val driver = GameDetailsPaneTestDriver(composeRule)
            composeRule.setContent {
                NextPlayTheme {
                    Box(modifier = Modifier.requiredSize(width = 360.dp, height = 800.dp)) {
                        GameDetailsPaneScreen(
                            modifier = Modifier.fillMaxSize(),
                            gameDetailsState = gameDetailsState,
                            screenshots = screenshots,
                            movies = movies,
                            series = series,
                            onBackClicked = { driver.backClickedCount += 1 },
                            onWebsiteClicked = { driver.websiteClickedCount += 1 },
                            onBookmarkClicked = { driver.bookmarkClickedCount += 1 },
                            onDescriptionOverflowChanged = { overflow ->
                                driver.overflowValues += overflow
                            },
                            onDescriptionToggleClicked = { driver.descriptionToggleClickedCount += 1 },
                            onScreenshotClicked = { url -> driver.lastScreenshotUrl = url },
                            onMovieClicked = { url -> driver.lastMovieUrl = url },
                            onSeriesClicked = { gameId -> driver.lastSeriesGameId = gameId },
                        )
                    }
                }
            }
            return driver
        }
    }

    fun assertContentDisplayed() {
        contentNode.assertIsDisplayed()
        officialWebsiteNode.assertIsDisplayed()
        descriptionNode.assertIsDisplayed()
        companiesSectionNode.performScrollTo()
        companiesSectionNode.assertIsDisplayed()
        screenshotsSectionNode.performScrollTo()
        screenshotsSectionNode.assertIsDisplayed()
        moviesSectionNode.performScrollTo()
        moviesSectionNode.assertIsDisplayed()
        seriesSectionNode.performScrollTo()
        seriesSectionNode.assertIsDisplayed()
    }

    fun assertContentIsNotDisplayed() {
        contentNode.assertIsNotDisplayed()
    }

    fun assertLoadingDisplayed() {
        loadingNode.assertIsDisplayed()
    }

    fun assertLoadingIsNotDisplayed() {
        loadingNode.assertIsNotDisplayed()
    }

    fun assertErrorDisplayed() {
        errorNode.assertIsDisplayed()
        errorDescriptionNode.assertIsDisplayed()
    }

    fun assertErrorIsNotDisplayed() {
        errorNode.assertIsNotDisplayed()
    }

    fun assertCompaniesSectionIsNotDisplayed() {
        companiesSectionNode.assertIsNotDisplayed()
    }

    fun assertMediaSectionsAreNotDisplayed() {
        screenshotsSectionNode.assertIsNotDisplayed()
        moviesSectionNode.assertIsNotDisplayed()
        seriesSectionNode.assertIsNotDisplayed()
    }

    fun clickBackButton() {
        backButtonNode.performClick()
    }

    fun clickOfficialWebsite() {
        officialWebsiteNode.performClick()
    }

    fun clickBookmarkButton() {
        removeBookmarkButtonNode.performClick()
    }

    fun clickReadMore() {
        readMoreNode.performScrollTo()
        readMoreNode.performClick()
    }

    fun clickScreenshot(screenshotId: Int) {
        screenshotsSectionNode.performScrollTo()
        screenshotNode(screenshotId).performClick()
    }

    fun clickMovie(movieId: Int) {
        moviesSectionNode.performScrollTo()
        movieNode(movieId).performClick()
    }

    fun clickSeries(gameId: Int) {
        seriesSectionNode.performScrollTo()
        seriesNode(gameId).performClick()
    }

    fun assertBackClicked() {
        composeRule.runOnIdle {
            assertEquals(1, backClickedCount)
        }
    }

    fun assertWebsiteClicked() {
        composeRule.runOnIdle {
            assertEquals(1, websiteClickedCount)
        }
    }

    fun assertBookmarkClicked() {
        composeRule.runOnIdle {
            assertEquals(1, bookmarkClickedCount)
        }
    }

    fun assertDescriptionToggleClicked() {
        composeRule.runOnIdle {
            assertEquals(1, descriptionToggleClickedCount)
        }
    }

    fun assertScreenshotClicked(expectedUrl: String) {
        composeRule.runOnIdle {
            assertEquals(expectedUrl, lastScreenshotUrl)
        }
    }

    fun assertMovieClicked(expectedUrl: String) {
        composeRule.runOnIdle {
            assertEquals(expectedUrl, lastMovieUrl)
        }
    }

    fun assertSeriesClicked(expectedGameId: Int) {
        composeRule.runOnIdle {
            assertEquals(expectedGameId, lastSeriesGameId)
        }
    }

    fun assertDescriptionOverflowChangedCalled() {
        composeRule.runOnIdle {
            assertNotNull(overflowValues.lastOrNull())
        }
    }
}
