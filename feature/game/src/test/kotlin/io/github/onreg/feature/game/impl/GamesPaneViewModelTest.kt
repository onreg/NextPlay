package io.github.onreg.feature.game.impl

import androidx.paging.PagingData
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.feature.game.impl.model.GamesPaneEvent
import io.github.onreg.feature.game.impl.model.GamesPaneListEvent
import io.github.onreg.testing.unit.coroutines.MainDispatcherRule
import io.github.onreg.testing.unit.flow.test
import io.github.onreg.testing.unit.paging.asSnapshot
import io.github.onreg.ui.game.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.platform.model.PlatformUI
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GamesPaneViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val game = Game(
        id = 1,
        title = "Title",
        imageUrl = "image",
        releaseDate = null,
        rating = 4.5,
        platforms = setOf(GamePlatform.PC)
    )
    private val pagingData = PagingData.from(listOf(game))
    private val defaultCard = GameCardUI(
        id = "1",
        title = "Title",
        imageUrl = "image",
        releaseDate = "",
        platforms = setOf(PlatformUI(name = "PC", iconRes = 1)),
        rating = ChipUI(text = "4.5"),
        isBookmarked = false
    )
    private val bookmarkedCard = defaultCard.copy(isBookmarked = true)
    private val mappedDefault = PagingData.from(listOf(defaultCard))
    private val mappedBookmarked = PagingData.from(listOf(bookmarkedCard))
    private val gamesFlow = flowOf(pagingData)

    private val defaultDriverBuilder = GamesPaneViewModelTestDriver.Builder()
        .repositoryGames(gamesFlow)
        .gameUiMapperMap(pagingData, emptySet(), mappedDefault)
        .gameUiMapperMap(pagingData, setOf("1"), mappedBookmarked)

    @Test
    fun `should navigate to details`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.events.test(this) {
            driver.viewModel.onCardClicked("42")
            assertLatest(GamesPaneEvent.GoToDetails("42"))
        }
    }

    @Test
    fun `should load games`() = runTest {
        val driver = defaultDriverBuilder.build()
        driver.viewModel.pagingState.test(this) {
            val items = latestValue().asSnapshot()
            assertEquals(listOf(defaultCard), items)
        }
        verify(driver.gameUiMapper).map(pagingData, emptySet())
    }

    @Test
    fun `should bookmark the game`() = runTest {
        val driver = defaultDriverBuilder.build()
        val testItemId = "1"
        driver.viewModel.pagingState.test(this) {
            val itemsBefore = latestValue().asSnapshot()
            assertFalse(itemsBefore.first { it.id == testItemId }.isBookmarked)

            driver.viewModel.onBookMarkClicked(testItemId)

            val itemsAfter = latestValue().asSnapshot()
            assertTrue(itemsAfter.first { it.id == testItemId }.isBookmarked)
        }
        verify(driver.gameUiMapper).map(pagingData, setOf(testItemId))
    }

    @Test
    fun `should emit event on page retry`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.pagingEvents.test(this) {
            driver.viewModel.onRetryClicked()
            assertLatest(GamesPaneListEvent.Retry)
        }
    }

    @Test
    fun `should emit event on refresh`() = runTest {
        val driver = defaultDriverBuilder.build()

        driver.viewModel.pagingEvents.test(this) {
            driver.viewModel.onRefreshClicked()
            assertLatest(GamesPaneListEvent.Refresh)
        }
    }
}
