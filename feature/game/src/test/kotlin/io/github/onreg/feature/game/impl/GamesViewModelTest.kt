package io.github.onreg.feature.game.impl

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.core.ui.components.card.PlatformUI
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.feature.game.impl.model.Event
import io.github.onreg.feature.game.impl.model.GamePaneState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class GamesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `should navigate to details`() = runTest {
        val driver = GamesViewModelTestDriver.Builder().build()

        driver.viewModel.onCardClicked("42")

        val event = driver.viewModel.events.receive()
        assertEquals(Event.GoToDetails("42"), event)
    }

    @Test
    fun `should load games`() = runTest {
        val fixture = createFixture()
        fixture.driver.viewModel.state.test(this) {
            val items = (latestValue() as GamePaneState.Ready).gameCardsUI.asSnapshot()
            assertEquals(listOf(fixture.defaultCard), items)
        }
        verify(fixture.driver.gameUiMapper).map(fixture.pagingData, emptySet())
    }

    @Test
    fun `should bookmark the game`() = runTest() {
        val fixture = createFixture()
        val testItemId = "1"
        fixture.driver.viewModel.state.test(this) {
            val itemsBefore = (latestValue() as GamePaneState.Ready).gameCardsUI.asSnapshot()
            assertFalse(itemsBefore.first { it.id == testItemId }.isBookmarked)

            fixture.driver.viewModel.onBookMarkClicked(testItemId)

            val itemsAfter = (latestValue() as GamePaneState.Ready).gameCardsUI.asSnapshot()
            assertTrue(itemsAfter.first { it.id == testItemId }.isBookmarked)
        }
        verify(fixture.driver.gameUiMapper).map(fixture.pagingData, setOf(testItemId))
    }
}

private data class Fixture(
    val pagingData: PagingData<Game>,
    val driver: GamesViewModelTestDriver,
    val defaultCard: GameCardUI,
    val bookmarkedCard: GameCardUI
)

private fun createFixture(): Fixture {
    val game = Game(
        id = 1,
        title = "Title",
        imageUrl = "image",
        releaseDate = null,
        rating = 4.5,
        platforms = setOf(GamePlatform.PC)
    )
    val pagingData = PagingData.from(listOf(game))
    val defaultCard = GameCardUI(
        id = "1",
        title = "Title",
        imageUrl = "image",
        releaseDate = "",
        platforms = setOf(PlatformUI(name = "PC", iconRes = 1)),
        rating = ChipUI(text = "4.5"),
        isBookmarked = false
    )
    val bookmarkedCard = defaultCard.copy(isBookmarked = true)
    val mappedDefault = PagingData.from(listOf(defaultCard))
    val mappedBookmarked = PagingData.from(listOf(bookmarkedCard))
    val gamesFlow = MutableStateFlow(pagingData)
    val driver = GamesViewModelTestDriver.Builder()
        .repositoryGames(gamesFlow)
        .gameUiMapperMap(pagingData, emptySet(), mappedDefault)
        .gameUiMapperMap(pagingData, setOf("1"), mappedBookmarked)
        .build()

    return Fixture(
        pagingData = pagingData,
        driver = driver,
        defaultCard = defaultCard,
        bookmarkedCard = bookmarkedCard
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TestObserver<T>(
    private val testScope: TestScope,
    flow: Flow<T>
) {
    private val values = mutableListOf<T>()
    private val job = testScope.launch {
        flow.collect { values.add(it) }
    }

    fun assert(vararg expected: T) {
        testScope.advanceUntilIdle()
        assertEquals(expected.toList(), values)
    }

    fun assertLatest(expected: T) {
        testScope.advanceUntilIdle()
        assertEquals(expected, values.last())
    }

    fun latestValue(): T {
        testScope.advanceUntilIdle()
        return values.last()
    }

    suspend fun cancel() {
        job.cancelAndJoin()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T> Flow<T>.test(testScope: TestScope, block: suspend TestObserver<T>.() -> Unit) {
    val observer = TestObserver(testScope = testScope, flow = this)
    testScope.advanceUntilIdle()
    try {
        observer.block()
    } finally {
        observer.cancel()
    }
}

suspend fun <T : Any> PagingData<T>.asSnapshot(): List<T> {
    return flowOf(this).asSnapshot()
}
