package io.github.onreg.feature.game.details.impl

import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.components.header.AppHeaderMenu
import io.github.onreg.core.ui.components.header.AppHeaderUi
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.feature.game.details.impl.model.DescriptionToggleUi
import io.github.onreg.feature.game.details.impl.model.GameDescriptionUi
import io.github.onreg.feature.game.details.impl.model.GameDetailsEvent
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.model.GameDetailsUi
import io.github.onreg.testing.unit.coroutines.MainDispatcherRule
import io.github.onreg.testing.unit.flow.test
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GameDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val gameId = 7
    private val refreshFailure = IllegalStateException("refresh failed")
    private val initialLoadingState = GameDetailsState.Loading(headerUi = AppHeaderUi())
    private val loadingState = GameDetailsState.Loading(headerUi = defaultHeader)
    private val errorState = GameDetailsState.Error(headerUi = defaultHeader)
    private val gameDetails = GameDetails(
        gameId = gameId,
        title = "Game title",
        imageUrl = "image-url",
        releaseDate = null,
        platforms = emptySet(),
        website = "https://nextplay.app",
        rating = 4.2,
        description = "Game description",
        companies = emptyList(),
    )
    private val readyState = gameDetails.readyState()

    private val defaultDriverBuilder = GameDetailsViewModelTestDriver
        .Builder()
        .gameId(gameId)
        .detailsRepositoryObserveGameDetails(flowOf(gameDetails))
        .detailsRepositoryRefreshGameDetails(Result.success(Unit))

    @Test
    fun `should trigger refresh when state starts being collected`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .gameId(gameId)
            .detailsRepositoryObserveGameDetails(emptyFlow())
            .detailsRepositoryRefreshGameDetails(Result.success(Unit))
            .build()

        val viewModel = driver.viewModel

        verify(driver.detailsRepository, never()).refreshGameDetails(gameId)

        viewModel.state.test(this) {
            assertLatest(initialLoadingState)
        }

        verify(driver.detailsRepository).refreshGameDetails(gameId)
    }

    @Test
    fun `should call refreshGameDetails with the current gameId on refresh`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.refresh()
        advanceUntilIdle()

        verify(driver.detailsRepository).refreshGameDetails(gameId)
    }

    @Test
    fun `should expose Loading state before details are available`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .gameId(gameId)
            .detailsRepositoryObserveGameDetails(emptyFlow())
            .detailsRepositoryRefreshGameDetails(Result.success(Unit))
            .build()

        driver.viewModel.state.test(this) {
            assertLatest(initialLoadingState)
        }
    }

    @Test
    fun `should expose Ready state when repository emits game details`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.state.test(this) {
            assertLatest(readyState)
        }
    }

    @Test
    fun `should expose Error state when repository emits null and refresh fails`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .gameId(gameId)
            .detailsRepositoryObserveGameDetails(flowOf(null))
            .detailsRepositoryRefreshGameDetails(Result.failure(refreshFailure))
            .build()

        driver.viewModel.state.test(this) {
            assertLatest(errorState)
        }
    }

    @Test
    fun `should keep Loading state when repository emits null and refresh succeeds but no details are available`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .gameId(gameId)
            .detailsRepositoryObserveGameDetails(flowOf(null))
            .detailsRepositoryRefreshGameDetails(Result.success(Unit))
            .build()

        driver.viewModel.state.test(this) {
            assertLatest(loadingState)
        }
    }

    @Test
    fun `should emit GoBack event when onBackClicked is called`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.events.test(this) {
            driver.viewModel.onBackClicked()
            assertLatest(GameDetailsEvent.GoBack)
        }
    }

    @Test
    fun `should emit GoToGameDetails event when onSeriesClicked is called`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.events.test(this) {
            driver.viewModel.onSeriesClicked(42)
            assertLatest(GameDetailsEvent.GoToGameDetails(gameId = 42))
        }
    }

    @Test
    fun `should open website when onWebsiteClicked is called in Ready state with non-blank website`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.state.test(this) {
            assertLatest(readyState)

            driver.viewModel.onWebsiteClicked()

            verify(driver.urlOpener).open("https://nextplay.app")
        }
    }

    @Test
    fun `should not open website when onWebsiteClicked is called in Ready state with blank website`() = runTest {
        val blankWebsiteGameDetails = gameDetails.withBlankWebsite()
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .gameId(gameId)
            .detailsRepositoryObserveGameDetails(flowOf(blankWebsiteGameDetails))
            .detailsRepositoryRefreshGameDetails(Result.success(Unit))
            .build()

        driver.viewModel.state.test(this) {
            assertLatest(blankWebsiteGameDetails.readyState())

            driver.viewModel.onWebsiteClicked()
        }

        verifyNoInteractions(driver.urlOpener)
    }

    @Test
    fun `should not open website when onWebsiteClicked is called outside Ready state`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .gameId(gameId)
            .detailsRepositoryObserveGameDetails(emptyFlow())
            .detailsRepositoryRefreshGameDetails(Result.success(Unit))
            .build()

        driver.viewModel.onWebsiteClicked()

        verifyNoInteractions(driver.urlOpener)
    }

    @Test
    fun `should open banner url when onBannerClicked is called with non-blank url`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.onBannerClicked("https://nextplay.app/banner")

        verify(driver.urlOpener).open("https://nextplay.app/banner")
    }

    @Test
    fun `should not open banner url when onBannerClicked is called with blank url`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.onBannerClicked(" ")

        verifyNoInteractions(driver.urlOpener)
    }

    @Test
    fun `should open screenshot url when onScreenshotClicked is called with non-blank url`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.onScreenshotClicked("https://nextplay.app/screenshot")

        verify(driver.urlOpener).open("https://nextplay.app/screenshot")
    }

    @Test
    fun `should not open screenshot url when onScreenshotClicked is called with blank url`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.onScreenshotClicked(" ")

        verifyNoInteractions(driver.urlOpener)
    }

    @Test
    fun `should open movie url when onMovieClicked is called with non-blank url`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.onMovieClicked("https://nextplay.app/movie")

        verify(driver.urlOpener).open("https://nextplay.app/movie")
    }

    @Test
    fun `should not open movie url when onMovieClicked is called with blank url`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.onMovieClicked(" ")

        verifyNoInteractions(driver.urlOpener)
    }

    @Test
    fun `should toggle bookmark state when onBookmarkClicked is called in Ready state`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.state.test(this) {
            assertLatest(readyState)

            driver.viewModel.onBookmarkClicked()

            assertLatest(readyState.withBookmarked())
        }
    }

    @Test
    fun `should not change bookmark state when onBookmarkClicked is called outside Ready state`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .gameId(gameId)
            .detailsRepositoryObserveGameDetails(emptyFlow())
            .detailsRepositoryRefreshGameDetails(Result.success(Unit))
            .build()

        driver.viewModel.onBookmarkClicked()

        assertEquals(initialLoadingState, driver.viewModel.state.value)
    }

    @Test
    fun `should update read more visibility when onDescriptionOverflowChanged is called with true`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.state.test(this) {
            assertLatest(readyState)

            driver.viewModel.onDescriptionOverflowChanged(true)

            assertLatest(readyState.withReadMoreVisible())
        }
    }

    @Test
    fun `should update read more visibility when onDescriptionOverflowChanged is called with false`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.state.test(this) {
            driver.viewModel.onDescriptionOverflowChanged(true)
            assertLatest(readyState.withReadMoreVisible())

            driver.viewModel.onDescriptionOverflowChanged(false)

            assertLatest(readyState)
        }
    }

    @Test
    fun `should expand description when onDescriptionToggleClicked is called while description is collapsed`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.state.test(this) {
            assertLatest(readyState)

            driver.viewModel.onDescriptionToggleClicked()

            assertLatest(readyState.withExpandedDescription())
        }
    }

    @Test
    fun `should collapse description when onDescriptionToggleClicked is called while description is expanded`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.state.test(this) {
            driver.viewModel.onDescriptionToggleClicked()
            assertLatest(readyState.withExpandedDescription())

            driver.viewModel.onDescriptionToggleClicked()

            assertLatest(readyState)
        }
    }

    @Test
    fun `should not change description state when onDescriptionToggleClicked is called outside Ready state`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .gameId(gameId)
            .detailsRepositoryObserveGameDetails(emptyFlow())
            .detailsRepositoryRefreshGameDetails(Result.success(Unit))
            .build()

        driver.viewModel.onDescriptionToggleClicked()

        assertEquals(initialLoadingState, driver.viewModel.state.value)
    }
}

private val defaultHeader = AppHeaderUi(
    navigationItem = AppHeaderMenu(
        iconResId = io.github.onreg.core.ui.R.drawable.ic_back_24,
        contentDescriptionResId = io.github.onreg.core.ui.R.string.back,
    ),
)

private fun GameDetails.readyState(): GameDetailsState.Ready = GameDetailsState.Ready(
    details = GameDetailsUi(
        image = imageUrl,
        rating = ChipUI(
            text = rating.toString(),
            isSelected = true,
        ),
        releaseDate = "",
        platforms = emptySet(),
        website = website,
        gameDescriptionUi = GameDescriptionUi(
            description = description,
            isExpanded = false,
            descriptionToggleUi = DescriptionToggleUi(
                text = "Read more",
                isVisible = false,
            ),
        ),
        companies = emptyList(),
        isBookmarked = false,
    ),
    headerUi = defaultHeader.copy(title = title),
)

private fun GameDetails.withBlankWebsite(): GameDetails = copy(website = " ")

private fun GameDetailsState.Ready.withBookmarked(): GameDetailsState.Ready = copy(
    details = details.copy(isBookmarked = true),
)

private fun GameDetailsState.Ready.withReadMoreVisible(): GameDetailsState.Ready = copy(
    details = details.copy(
        gameDescriptionUi = details.gameDescriptionUi.copy(
            descriptionToggleUi = details.gameDescriptionUi.descriptionToggleUi.copy(
                isVisible = true,
            ),
        ),
    ),
)

private fun GameDetailsState.Ready.withExpandedDescription(): GameDetailsState.Ready = copy(
    details = details.copy(
        gameDescriptionUi = details.gameDescriptionUi.copy(
            isExpanded = true,
            descriptionToggleUi = details.gameDescriptionUi.descriptionToggleUi.copy(
                text = "Read less",
                isVisible = true,
            ),
        ),
    ),
)
