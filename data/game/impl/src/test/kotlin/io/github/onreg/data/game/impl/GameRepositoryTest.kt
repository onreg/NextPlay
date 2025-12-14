package io.github.onreg.data.game.impl

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingSource
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class GameRepositoryTest {

    private val dispatcher = StandardTestDispatcher()
    private val differ = AsyncPagingDataDiffer(
        diffCallback = object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean = oldItem == newItem
        },
        updateCallback = mock(),
        mainDispatcher = dispatcher,
        workerDispatcher = dispatcher
    )

    @Test
    fun `should get games`() = runTest(dispatcher) {
        val mappedGame = Game(
            id = 1,
            title = "Title",
            imageUrl = "image",
            releaseDate = null,
            rating = 4.5,
            platforms = setOf(GamePlatform.PC)
        )
        val gameEntity = GameEntity(
            id = 1,
            title = "Title",
            imageUrl = "image",
            releaseDate = null,
            rating = 4.5,
            insertionOrder = 0
        )
        val entityWithPlatforms = GameWithPlatforms(
            game = gameEntity,
            platforms = listOf(PlatformEntity(GamePlatform.PC.id))
        )

        val driver = GameRepositoryTestDriver.Builder()
            .gameDaoPagingSource(listOf(entityWithPlatforms))
            .entityMapperMap(entityWithPlatforms, mappedGame)
            .build()

        val pagingData = driver.getGames().first()

        val submitJob = launch { differ.submitData(pagingData) }
        advanceUntilIdle()

        assertEquals(listOf(mappedGame), differ.snapshot().items)
        submitJob.cancelAndJoin()
    }
}