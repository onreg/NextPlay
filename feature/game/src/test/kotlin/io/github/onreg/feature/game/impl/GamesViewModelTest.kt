package io.github.onreg.feature.game.impl

import androidx.paging.PagingData
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.core.ui.components.card.PlatformUI
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.feature.game.impl.model.Event
import io.github.onreg.feature.game.impl.model.GamePaneState
import io.github.onreg.testing.unit.coroutines.MainDispatcherRule
import io.github.onreg.testing.unit.flow.test
import io.github.onreg.testing.unit.paging.asSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class GamesViewModelTest {

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

    private val defaultDriver = GamesViewModelTestDriver.Builder()
        .repositoryGames(gamesFlow)
        .gameUiMapperMap(pagingData, emptySet(), mappedDefault)
        .gameUiMapperMap(pagingData, setOf("1"), mappedBookmarked)
        .build()

    @Test
    fun `should navigate to details`() = runTest {
        val driver = GamesViewModelTestDriver.Builder().build()

        driver.viewModel.onCardClicked("42")

        val event = driver.viewModel.events.receive()
        assertEquals(Event.GoToDetails("42"), event)
    }

    @Test
    fun `should load games`() = runTest {
        defaultDriver.viewModel.state.test(this) {
            val items = (latestValue() as GamePaneState.Ready).gameCardsUI.asSnapshot()
            assertEquals(listOf(defaultCard), items)
        }
        verify(defaultDriver.gameUiMapper).map(pagingData, emptySet())
    }

    @Test
    fun `should bookmark the game`() = runTest {
        val testItemId = "1"
        defaultDriver.viewModel.state.test(this) {
            val itemsBefore = (latestValue() as GamePaneState.Ready).gameCardsUI.asSnapshot()
            assertFalse(itemsBefore.first { it.id == testItemId }.isBookmarked)

            defaultDriver.viewModel.onBookMarkClicked(testItemId)

            val itemsAfter = (latestValue() as GamePaneState.Ready).gameCardsUI.asSnapshot()
            assertTrue(itemsAfter.first { it.id == testItemId }.isBookmarked)
        }
        verify(defaultDriver.gameUiMapper).map(pagingData, setOf(testItemId))
    }
}
