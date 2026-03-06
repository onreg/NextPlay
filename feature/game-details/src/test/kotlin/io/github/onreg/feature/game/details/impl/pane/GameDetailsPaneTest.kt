package io.github.onreg.feature.game.details.impl.pane

import androidx.compose.ui.test.junit4.createComposeRule
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class GameDetailsPaneTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `should show game details`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .build()

        driver.assertContentDisplayed()
        driver.assertLoadingIsNotDisplayed()
        driver.assertErrorIsNotDisplayed()
    }

    @Test
    fun `should show loading`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .loading()
            .build()

        driver.assertLoadingDisplayed()
        driver.assertContentIsNotDisplayed()
        driver.assertErrorIsNotDisplayed()
    }

    @Test
    fun `should show error`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .error()
            .build()

        driver.assertErrorDisplayed()
        driver.assertContentIsNotDisplayed()
        driver.assertLoadingIsNotDisplayed()
    }

    @Test
    fun `should hide companies section`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .withoutCompanies()
            .build()

        driver.assertCompaniesSectionIsNotDisplayed()
    }

    @Test
    fun `should hide screenshots movies and series`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .withoutMediaSections()
            .build()

        driver.assertMediaSectionsAreNotDisplayed()
    }

    @Test
    fun `should call onBackClicked when back icon is tapped`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .build()

        driver.clickBackButton()

        driver.assertBackClicked()
    }

    @Test
    fun `should call onWebsiteClicked when official website is tapped`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .build()

        driver.clickOfficialWebsite()

        driver.assertWebsiteClicked()
    }

    @Test
    fun `should call onBookmarkClicked when bookmark toggle is tapped`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .build()

        driver.clickBookmarkButton()

        driver.assertBookmarkClicked()
    }

    @Test
    fun `should call onDescriptionToggleClicked when read more is tapped`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .build()

        driver.clickReadMore()

        driver.assertDescriptionToggleClicked()
    }

    @Test
    fun `should call onScreenshotClicked with screenshot url when screenshot item is tapped`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .build()

        driver.clickScreenshot(GameDetailsTestData.screenshotsItems.first().id)

        driver.assertScreenshotClicked(GameDetailsTestData.screenshotsItems.first().imageUrl)
    }

    @Test
    fun `should call onMovieClicked with movie url when movie item is tapped`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .build()

        driver.clickMovie(GameDetailsTestData.moviesItems.first().id)

        driver.assertMovieClicked(GameDetailsTestData.moviesItems.first().videoUrl)
    }

    @Test
    fun `should call onSeriesClicked with game id when series card is tapped`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .build()

        driver.clickSeries(GameDetailsTestData.seriesItems.first().id)

        driver.assertSeriesClicked(GameDetailsTestData.seriesItems.first().id)
    }

    @Test
    fun `should call onDescriptionOverflowChanged when description layout is measured`() {
        val driver = GameDetailsPaneTestDriver
            .Builder(composeRule)
            .withLongDescription()
            .build()

        driver.assertDescriptionOverflowChangedCalled()
    }
}
