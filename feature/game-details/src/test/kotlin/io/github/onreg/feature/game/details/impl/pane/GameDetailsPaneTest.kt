package io.github.onreg.feature.game.details.impl.pane

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestTags
import io.github.onreg.ui.details.presentation.model.GameDetailsUi
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class GameDetailsPaneTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val detailsUi = GameDetailsUi(
        id = "1",
        title = "Title",
        bannerImageUrl = null,
        releaseDate = "Jan 1, 2024",
        rating = "4.5",
        websiteUrl = null,
        isWebsiteVisible = false,
        descriptionHtml = "<p>Desc</p>",
        developers = emptyList(),
    )

    @Test
    fun `should show loading when initial loading`() {
        composeRule.setContent {
            GameDetailsScreen(
                detailsUi = null,
                isInitialLoading = true,
                initialError = null,
                isDescriptionExpanded = false,
                screenshots = flowOf(PagingData.empty()),
                movies = flowOf(PagingData.empty()),
                series = flowOf(PagingData.empty()),
                onBackClick = {},
                onRetryClick = {},
                onWebsiteClick = {},
                onScreenshotClick = {},
                onMovieClick = {},
                onSeriesClick = {},
                onToggleDescription = {},
                onBookmarkClick = {},
            )
        }

        composeRule.onNodeWithTag(GameDetailsTestTags.LOADING).assertIsDisplayed()
    }

    @Test
    fun `should show error when refresh fails without cached data`() {
        composeRule.setContent {
            GameDetailsScreen(
                detailsUi = null,
                isInitialLoading = false,
                initialError = "boom",
                isDescriptionExpanded = false,
                screenshots = flowOf(PagingData.empty()),
                movies = flowOf(PagingData.empty()),
                series = flowOf(PagingData.empty()),
                onBackClick = {},
                onRetryClick = {},
                onWebsiteClick = {},
                onScreenshotClick = {},
                onMovieClick = {},
                onSeriesClick = {},
                onToggleDescription = {},
                onBookmarkClick = {},
            )
        }

        composeRule.onNodeWithTag(GameDetailsTestTags.ERROR).assertIsDisplayed()
    }

    @Test
    fun `should show content when details available`() {
        composeRule.setContent {
            GameDetailsScreen(
                detailsUi = detailsUi,
                isInitialLoading = false,
                initialError = null,
                isDescriptionExpanded = false,
                screenshots = flowOf(PagingData.empty()),
                movies = flowOf(PagingData.empty()),
                series = flowOf(PagingData.empty()),
                onBackClick = {},
                onRetryClick = {},
                onWebsiteClick = {},
                onScreenshotClick = {},
                onMovieClick = {},
                onSeriesClick = {},
                onToggleDescription = {},
                onBookmarkClick = {},
            )
        }

        composeRule.onNodeWithTag(GameDetailsTestTags.CONTENT).assertIsDisplayed()
    }

    @Test
    fun `should show read more when collapsed`() {
        composeRule.setContent {
            GameDetailsScreen(
                detailsUi = detailsUi,
                isInitialLoading = false,
                initialError = null,
                isDescriptionExpanded = false,
                screenshots = flowOf(PagingData.empty()),
                movies = flowOf(PagingData.empty()),
                series = flowOf(PagingData.empty()),
                onBackClick = {},
                onRetryClick = {},
                onWebsiteClick = {},
                onScreenshotClick = {},
                onMovieClick = {},
                onSeriesClick = {},
                onToggleDescription = {},
                onBookmarkClick = {},
            )
        }

        composeRule.onNodeWithText("Read more").assertIsDisplayed()
    }

    @Test
    fun `should show read less when expanded`() {
        composeRule.setContent {
            GameDetailsScreen(
                detailsUi = detailsUi,
                isInitialLoading = false,
                initialError = null,
                isDescriptionExpanded = true,
                screenshots = flowOf(PagingData.empty()),
                movies = flowOf(PagingData.empty()),
                series = flowOf(PagingData.empty()),
                onBackClick = {},
                onRetryClick = {},
                onWebsiteClick = {},
                onScreenshotClick = {},
                onMovieClick = {},
                onSeriesClick = {},
                onToggleDescription = {},
                onBookmarkClick = {},
            )
        }

        composeRule.onNodeWithText("Read less").assertIsDisplayed()
    }
}
