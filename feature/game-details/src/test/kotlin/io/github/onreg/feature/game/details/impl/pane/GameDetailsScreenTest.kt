package io.github.onreg.feature.game.details.impl.pane

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.data.game.list.api.model.Game
import io.github.onreg.data.game.list.api.model.GamePlatform
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestTags
import io.github.onreg.feature.game.details.impl.ui.model.GameCompanyRoleUi
import io.github.onreg.feature.game.details.impl.ui.model.GameCompanyUi
import io.github.onreg.feature.game.details.impl.ui.model.GameDetailsUi
import io.github.onreg.ui.platform.model.PlatformUI
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import io.github.onreg.core.ui.R as CoreUiR

@RunWith(RobolectricTestRunner::class)
internal class GameDetailsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `loaded state should render required section labels and app header`() {
        setGameDetailsScreenContent(
            details = details(),
            screenshots = listOf(
                Screenshot(
                    id = 1,
                    imageUrl = "https://screenshot",
                    width = 1920,
                    height = 1080,
                ),
            ),
            movies = listOf(
                Movie(
                    id = 1,
                    name = "Trailer",
                    previewUrl = "https://preview",
                    videoUrl = "https://video",
                ),
            ),
            series = listOf(game()),
        )

        composeRule.onNodeWithText("Description").assertIsDisplayed()
        scrollToText("Developers & Publishers")
        composeRule.onNodeWithText("Developers & Publishers").assertIsDisplayed()
        scrollToText("Screenshots")
        composeRule.onNodeWithText("Screenshots").assertIsDisplayed()
        scrollToText("Movies")
        composeRule.onNodeWithText("Movies").assertIsDisplayed()
        scrollToText("Series games")
        composeRule.onNodeWithText("Series games").assertIsDisplayed()
        composeRule.onNodeWithText("Game details title").assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(context.getString(CoreUiR.string.back_text))
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("Share").assertCountEquals(0)
    }

    @Test
    fun `description section should render content`() {
        setGameDetailsScreenContent(details = details(description = "Long text ".repeat(2000)))

        scrollToText("Description")
        composeRule.onNodeWithText("Description").assertIsDisplayed()
        composeRule.onNodeWithText("Long text", substring = true).assertIsDisplayed()
    }

    @Test
    fun `companies should render with role labels`() {
        setGameDetailsScreenContent(
            details = details(
                companies = listOf(
                    GameCompanyUi(
                        name = "Dev studio",
                        logoUrl = null,
                        role = GameCompanyRoleUi.Developer,
                    ),
                    GameCompanyUi(
                        name = "Pub house",
                        logoUrl = "https://publisher-logo",
                        role = GameCompanyRoleUi.Publisher,
                    ),
                ),
            ),
        )

        scrollToText("Developer")
        composeRule.onNodeWithText("Developer").assertIsDisplayed()
        scrollToText("Publisher")
        composeRule.onNodeWithText("Publisher").assertIsDisplayed()
    }

    @Test
    fun `screenshots section hides on error and movies shows while loading`() {
        setGameDetailsScreenContent(
            details = details(),
            screenshotsLoadState = LoadState.Error(IllegalStateException("boom")),
            moviesLoadState = LoadState.Loading,
        )

        composeRule.onAllNodesWithText("Screenshots").assertCountEquals(0)
        scrollToText("Movies")
        composeRule.onNodeWithText("Movies").assertIsDisplayed()
    }

    @Test
    fun `official website action should trigger callback`() {
        var clickCount = 0
        setGameDetailsScreenContent(
            details = details(),
            onWebsiteClicked = { clickCount += 1 },
        )

        scrollToText("Official Website")
        composeRule.onNodeWithText("Official Website").performClick()

        composeRule.runOnIdle {
            assertEquals(1, clickCount)
        }
    }

    @Test
    fun `error content retry should trigger callback`() {
        var retryCount = 0
        composeRule.setContent {
            ErrorContent(
                modifier = androidx.compose.ui.Modifier,
                onRetry = { retryCount += 1 },
            )
        }

        composeRule.onNodeWithText(context.getString(CoreUiR.string.retry_text)).performClick()
        composeRule.runOnIdle {
            assertEquals(1, retryCount)
        }
    }

    private fun setGameDetailsScreenContent(
        details: GameDetailsUi = details(),
        screenshots: List<Screenshot> = emptyList(),
        movies: List<Movie> = emptyList(),
        series: List<Game> = emptyList(),
        screenshotsLoadState: LoadState = LoadState.NotLoading(false),
        moviesLoadState: LoadState = LoadState.NotLoading(false),
        seriesLoadState: LoadState = LoadState.NotLoading(false),
        onWebsiteClicked: () -> Unit = {},
    ) {
        val screenshotsState = MutableStateFlow(pagingData(screenshots, screenshotsLoadState))
        val moviesState = MutableStateFlow(pagingData(movies, moviesLoadState))
        val seriesState = MutableStateFlow(pagingData(series, seriesLoadState))
        composeRule.setContent {
            val screenshotItems = screenshotsState.collectAsLazyPagingItems()
            val movieItems = moviesState.collectAsLazyPagingItems()
            val seriesItems = seriesState.collectAsLazyPagingItems()

            GameDetailsScreen(
                state = GameDetailsState(
                    gameId = details.gameId,
                    details = details,
                ),
                details = details,
                screenshots = screenshotItems,
                movies = movieItems,
                series = seriesItems,
                onWebsiteClicked = onWebsiteClicked,
                mapPlatforms = {
                    it
                        .map { platform ->
                            PlatformUI(
                                platform.name,
                                CoreUiR.drawable.ic_controller_24,
                            )
                        }.toSet()
                },
            )
        }
    }

    private fun details(
        description: String = "Short description",
        companies: List<GameCompanyUi> = listOf(
            GameCompanyUi(
                name = "Developer One",
                logoUrl = null,
                role = GameCompanyRoleUi.Developer,
            ),
            GameCompanyUi(
                name = "Publisher One",
                logoUrl = null,
                role = GameCompanyRoleUi.Publisher,
            ),
        ),
    ): GameDetailsUi = GameDetailsUi(
        gameId = 1,
        title = "Game details title",
        imageUrl = "https://image",
        ratingChip = ChipUI(text = "4.2", isSelected = true),
        releaseDate = "Jan 1, 2020",
        platforms = setOf(PlatformUI("PC", CoreUiR.drawable.ic_controller_24)),
        website = "https://example.com",
        isWebsiteVisible = true,
        description = description,
        companies = companies,
    )

    private fun game(): Game = Game(
        id = 77,
        title = "Series game",
        imageUrl = "https://image",
        releaseDate = null,
        rating = 3.8,
        platforms = setOf(GamePlatform.PC),
    )

    private fun <T : Any> pagingData(
        items: List<T>,
        refresh: LoadState,
    ): PagingData<T> = PagingData.from(
        items,
        sourceLoadStates = LoadStates(
            refresh = refresh,
            prepend = LoadState.NotLoading(false),
            append = LoadState.NotLoading(false),
        ),
    )

    private fun scrollToText(text: String) {
        composeRule
            .onNodeWithTag(GameDetailsTestTags.CONTENT)
            .performScrollToNode(hasText(text))
    }
}
