package io.github.onreg.feature.game.details.impl

import androidx.paging.PagingData
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.data.details.api.model.GameCompany
import io.github.onreg.data.details.api.model.GameCompanyRole
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.feature.game.details.impl.model.GameDetailsEvent
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.model.MovieUI
import io.github.onreg.feature.game.details.impl.model.ScreenshotUI
import io.github.onreg.testing.unit.coroutines.MainDispatcherRule
import io.github.onreg.testing.unit.flow.test
import io.github.onreg.testing.unit.paging.asSnapshot
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.platform.model.PlatformUI
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.Test

internal class GameDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init calls repositories with correct game id`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .gameId(123)
            .build()

        driver.viewModel

        verify(driver.detailsRepository).observeGameDetails(123)
        verify(driver.screenshotsRepository).getScreenshots(123)
        verify(driver.moviesRepository).getMovies(123)
        verify(driver.gameRepository).getSeries(123)
    }

    @Test
    fun `state subscription refreshes details`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .gameId(321)
            .initialDetails(null)
            .refreshResult(Result.success(Unit))
            .build()

        driver.viewModel.state.test(this) {
            verify(driver.detailsRepository).refreshGameDetails(321)
        }
    }

    @Test
    fun `state is error when refresh fails and details are absent`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .initialDetails(null)
            .refreshResult(Result.failure(IllegalStateException("boom")))
            .build()

        driver.viewModel.state.test(this) {
            assertIs<GameDetailsState.Error>(latestValue())
        }
    }

    @Test
    fun `onBackClicked emits go back event`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .build()

        driver.viewModel.events.test(this) {
            driver.viewModel.onBackClicked()
            assertLatest(GameDetailsEvent.GoBack)
        }
    }

    @Test
    fun `onSeriesClicked emits go game details event`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .build()

        driver.viewModel.events.test(this) {
            driver.viewModel.onSeriesClicked(42)
            assertLatest(GameDetailsEvent.GoGameDetails(42))
        }
    }

    @Test
    fun `onWebsiteClicked opens website when state is ready`() = runTest {
        val website = "https://nextplay.test/game"
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .initialDetails(gameDetails(website = website))
            .build()

        driver.viewModel.state.test(this) {
            driver.viewModel.onWebsiteClicked()
            verify(driver.urlOpener).open(website)
        }
    }

    @Test
    fun `onWebsiteClicked does nothing when state is not ready`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .initialDetails(null)
            .build()

        driver.viewModel.onWebsiteClicked()

        verifyNoInteractions(driver.urlOpener)
    }

    @Test
    fun `direct url handlers ignore blank urls`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .build()

        driver.viewModel.onBannerClicked("")
        driver.viewModel.onScreenshotClicked("")
        driver.viewModel.onMovieClicked("")

        verifyNoInteractions(driver.urlOpener)
    }

    @Test
    fun `direct url handlers open non blank urls`() = runTest {
        val bannerUrl = "https://nextplay.test/banner"
        val screenshotUrl = "https://nextplay.test/screenshot"
        val movieUrl = "https://nextplay.test/movie"
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .build()

        driver.viewModel.onBannerClicked(bannerUrl)
        driver.viewModel.onScreenshotClicked(screenshotUrl)
        driver.viewModel.onMovieClicked(movieUrl)

        verify(driver.urlOpener).open(bannerUrl)
        verify(driver.urlOpener).open(screenshotUrl)
        verify(driver.urlOpener).open(movieUrl)
    }

    @Test
    fun `ready state reflects bookmark and description toggles`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .initialDetails(gameDetails())
            .build()

        driver.viewModel.state.test(this) {
            val initial = latestValue() as GameDetailsState.Ready
            assertFalse(initial.details.isBookmarked)
            assertFalse(initial.details.gameDescriptionUi.isExpanded)
            assertFalse(initial.details.gameDescriptionUi.descriptionToggleUi.isVisible)

            driver.viewModel.onBookmarkClicked()
            val bookmarked = latestValue() as GameDetailsState.Ready
            assertTrue(bookmarked.details.isBookmarked)

            driver.viewModel.onBookmarkClicked()
            val unBookmarked = latestValue() as GameDetailsState.Ready
            assertFalse(unBookmarked.details.isBookmarked)

            driver.viewModel.onDescriptionOverflowChanged(true)
            val withOverflow = latestValue() as GameDetailsState.Ready
            assertTrue(withOverflow.details.gameDescriptionUi.descriptionToggleUi.isVisible)

            driver.viewModel.onDescriptionToggleClicked()
            val expanded = latestValue() as GameDetailsState.Ready
            assertTrue(expanded.details.gameDescriptionUi.isExpanded)
        }
    }

    @Test
    fun `screenshots maps paging flow`() = runTest {
        val screenshot = Screenshot(
            id = 1,
            imageUrl = "https://nextplay.test/screenshot",
            width = 100,
            height = 200,
        )
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .screenshots(flowOf(PagingData.from(listOf(screenshot))))
            .build()

        val items = driver.viewModel.screenshots.first().asSnapshot()

        assertEquals(
            listOf(
                ScreenshotUI(
                    id = screenshot.id,
                    imageUrl = screenshot.imageUrl,
                ),
            ),
            items,
        )
    }

    @Test
    fun `movies maps paging flow`() = runTest {
        val movie = Movie(
            id = 1,
            name = "Trailer",
            previewUrl = "https://nextplay.test/preview",
            videoUrl = "https://nextplay.test/video",
        )
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .movies(flowOf(PagingData.from(listOf(movie))))
            .build()

        val items = driver.viewModel.movies.first().asSnapshot()

        assertEquals(
            listOf(
                MovieUI(
                    id = movie.id,
                    videoUrl = movie.videoUrl,
                    previewUrl = movie.previewUrl.orEmpty(),
                    name = movie.name.orEmpty(),
                ),
            ),
            items,
        )
    }

    @Test
    fun `series maps paging flow`() = runTest {
        val game = Game(
            id = 11,
            title = "Series game",
            imageUrl = "https://nextplay.test/game",
            releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
            rating = 4.4,
            platforms = setOf(GamePlatform.PC),
        )
        val mappedCard = GameCardUI(
            id = game.id,
            title = game.title,
            imageUrl = game.imageUrl,
            releaseDate = "Jan 1, 2024",
            platforms = setOf(PlatformUI(name = "PC", iconRes = 1)),
            rating = ChipUI(text = "4.4", isSelected = true),
            isBookmarked = false,
        )
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .series(flowOf(PagingData.from(listOf(game))))
            .gameCardUiMapperMap(game = game, mapped = mappedCard)
            .build()

        val items = driver.viewModel.series.first().asSnapshot()

        assertEquals(listOf(mappedCard), items)
    }

    private fun gameDetails(website: String? = "https://nextplay.test") = GameDetails(
        gameId = 1,
        title = "NextPlay",
        imageUrl = "https://nextplay.test/image",
        releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
        platforms = setOf(GamePlatform.PC),
        website = website,
        rating = 4.2,
        description = "Some description",
        companies = listOf(
            GameCompany(
                name = "Studio",
                logoUrl = null,
                role = GameCompanyRole.Developer,
            ),
        ),
    )
}
