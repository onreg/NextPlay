package io.github.onreg.ui.game.presentation.mapper

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.core.ui.components.card.PlatformUI
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GameUiMapperTest {
    private val dispatcher = StandardTestDispatcher()
    private val platformUiMapper: PlatformUiMapper = mock()
    private val mapper = GameUiMapperImpl(platformUiMapper)
    private val differ = AsyncPagingDataDiffer(
        diffCallback = object : DiffUtil.ItemCallback<GameCardUI>() {
            override fun areItemsTheSame(oldItem: GameCardUI, newItem: GameCardUI): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: GameCardUI, newItem: GameCardUI): Boolean {
                return oldItem == newItem
            }
        },
        updateCallback = mock(),
        mainDispatcher = dispatcher,
        workerDispatcher = dispatcher
    )

    @Test
    fun `should map game to ui`() {
        val platformUi = setOf(PlatformUI(name = "PC", iconRes = 1))
        whenever(platformUiMapper.mapPlatform(setOf(GamePlatform.PC))).thenReturn(platformUi)

        val game = Game(
            id = 7,
            title = "Halo",
            imageUrl = "image",
            releaseDate = null,
            rating = 4.0,
            platforms = setOf(GamePlatform.PC)
        )

        val result = mapper.map(game, isBookmarked = true)

        val expected = GameCardUI(
            id = "7",
            title = "Halo",
            imageUrl = "image",
            releaseDate = "",
            platforms = platformUi,
            rating = ChipUI(text = "4.0"),
            isBookmarked = true
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should map paging data and apply bookmarks`() = runTest(dispatcher) {
        val platformUi = setOf(PlatformUI(name = "PC", iconRes = 1))
        whenever(platformUiMapper.mapPlatform(any())).thenReturn(platformUi)

        val games = listOf(
            Game(
                id = 1,
                title = "First",
                imageUrl = "image-1",
                releaseDate = null,
                rating = 3.5,
                platforms = setOf(GamePlatform.PC)
            ),
            Game(
                id = 2,
                title = "Second",
                imageUrl = "image-2",
                releaseDate = null,
                rating = 4.5,
                platforms = setOf(GamePlatform.PC)
            )
        )

        val pagingData = mapper.map(PagingData.from(games), setOf("2"))
        val submitJob = launch { differ.submitData(pagingData) }
        advanceUntilIdle()

        val expected = listOf(
            GameCardUI(
                id = "1",
                title = "First",
                imageUrl = "image-1",
                releaseDate = "",
                platforms = platformUi,
                rating = ChipUI(text = "3.5"),
                isBookmarked = false
            ),
            GameCardUI(
                id = "2",
                title = "Second",
                imageUrl = "image-2",
                releaseDate = "",
                platforms = platformUi,
                rating = ChipUI(text = "4.5"),
                isBookmarked = true
            )
        )

        assertEquals(expected, differ.snapshot().items)
        submitJob.cancelAndJoin()
    }
}
