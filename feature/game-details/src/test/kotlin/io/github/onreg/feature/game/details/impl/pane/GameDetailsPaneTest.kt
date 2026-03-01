package io.github.onreg.feature.game.details.impl.pane

import androidx.compose.ui.test.junit4.createComposeRule
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.feature.game.details.impl.model.MovieUI
import io.github.onreg.feature.game.details.impl.model.ScreenshotUI
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.platform.model.PlatformUI
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import io.github.onreg.core.ui.R as CoreUiR

@RunWith(RobolectricTestRunner::class)
internal class GameDetailsPaneTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val screenshotItem = ScreenshotUI(
        id = 1,
        imageUrl = "example",
    )

    private val movieItem = MovieUI(
        id = 1,
        videoUrl = "https://example.com/video-1.mp4",
        previewUrl = "example",
        name = "Launch Trailer",
    )

    private val seriesItem = GameCardUI(
        id = 2,
        title = "Dark Souls III",
        imageUrl = "example",
        releaseDate = "Mar 24, 2016",
        platforms = setOf(
            PlatformUI(
                name = "PC",
                iconRes = CoreUiR.drawable.ic_controller_24,
            ),
        ),
        rating = ChipUI(text = "4.6", isSelected = true),
        isBookmarked = false,
    )

    @Test
    fun `should show loading branch`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .state(GameDetailsPaneTestDriver.loadingState())
            .build()

        driver.assertLoadingBranchDisplayed()
    }

    @Test
    fun `should show error branch and network message`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .state(GameDetailsPaneTestDriver.errorState())
            .build()

        driver.assertErrorBranchDisplayed()
        driver.assertNetworkErrorMessageDisplayed()
    }

    @Test
    fun `should trigger retry callback`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .state(GameDetailsPaneTestDriver.errorState())
            .build()

        driver.clickRetry()

        driver.assertRetryClicks(1)
    }

    @Test
    fun `should show ready branch`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .state(GameDetailsTestData.readyState)
            .screenshots(GameDetailsPaneTestDriver.loaded(listOf(screenshotItem)))
            .movies(GameDetailsPaneTestDriver.loaded(listOf(movieItem)))
            .series(GameDetailsPaneTestDriver.loaded(listOf(seriesItem)))
            .build()

        driver.assertContentBranchDisplayed()
    }

    @Test
    fun `should trigger back callback`() {
        val driver = GameDetailsPaneTestDriver.Builder(composeRule).build()

        driver.clickBack()

        driver.assertBackClicks(1)
    }

    @Test
    fun `should trigger website callback`() {
        val driver = GameDetailsPaneTestDriver.Builder(composeRule).build()

        driver.clickWebsite()

        driver.assertWebsiteClicks(1)
    }

    @Test
    fun `should trigger bookmark callback`() {
        val driver = GameDetailsPaneTestDriver.Builder(composeRule).build()

        driver.clickBookmark()

        driver.assertBookmarkClicks(1)
    }

    @Test
    fun `should trigger description toggle callback`() {
        val driver = GameDetailsPaneTestDriver.Builder(composeRule).build()

        driver.clickDescriptionToggle()

        driver.assertDescriptionToggleClicks(1)
    }

    @Test
    fun `should show media sections when paging is loading`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .screenshots(GameDetailsPaneTestDriver.loading())
            .movies(GameDetailsPaneTestDriver.loading())
            .series(GameDetailsPaneTestDriver.loading())
            .build()

        driver.assertScreenshotsSectionExists()
        driver.assertMoviesSectionExists()
        driver.assertSeriesSectionExists()
    }

    @Test
    fun `should hide media sections when paging is empty`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .screenshots(GameDetailsPaneTestDriver.empty())
            .movies(GameDetailsPaneTestDriver.empty())
            .series(GameDetailsPaneTestDriver.empty())
            .build()

        driver.assertScreenshotsSectionDoesNotExist()
        driver.assertMoviesSectionDoesNotExist()
        driver.assertSeriesSectionDoesNotExist()
    }

    @Test
    fun `should hide media sections when paging has error`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .screenshots(GameDetailsPaneTestDriver.error(IllegalStateException("boom")))
            .movies(GameDetailsPaneTestDriver.error(IllegalStateException("boom")))
            .series(GameDetailsPaneTestDriver.error(IllegalStateException("boom")))
            .build()

        driver.assertScreenshotsSectionDoesNotExist()
        driver.assertMoviesSectionDoesNotExist()
        driver.assertSeriesSectionDoesNotExist()
    }

    @Test
    fun `should trigger screenshot callback on screenshot item click`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .screenshots(GameDetailsPaneTestDriver.loaded(listOf(screenshotItem)))
            .movies(GameDetailsPaneTestDriver.error(IllegalStateException("boom")))
            .series(GameDetailsPaneTestDriver.error(IllegalStateException("boom")))
            .build()

        driver.clickScreenshot(screenshotItem.id)

        driver.assertLastScreenshotClickedUrl(screenshotItem.imageUrl)
    }

    @Test
    fun `should trigger movie callback on movie item click`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .screenshots(GameDetailsPaneTestDriver.error(IllegalStateException("boom")))
            .movies(GameDetailsPaneTestDriver.loaded(listOf(movieItem)))
            .series(GameDetailsPaneTestDriver.error(IllegalStateException("boom")))
            .build()

        driver.clickMovie(movieItem.id)

        driver.assertLastMovieClickedUrl(movieItem.videoUrl)
    }

    @Test
    fun `should trigger series callback on series item click`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .screenshots(GameDetailsPaneTestDriver.error(IllegalStateException("boom")))
            .movies(GameDetailsPaneTestDriver.error(IllegalStateException("boom")))
            .series(GameDetailsPaneTestDriver.loaded(listOf(seriesItem)))
            .build()

        driver.clickSeriesGame(seriesItem.id)

        driver.assertLastSeriesClickedId(seriesItem.id)
    }
}
