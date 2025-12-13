package io.github.onreg.data.game.impl

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingSource
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.rawg.dto.PlatformDto
import io.github.onreg.core.network.rawg.dto.PlatformWrapperDto
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class GameRepositoryTest {

    private val dispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should get games`() = runTest(dispatcher) {
        val dto = GameDto(
            id = 1,
            title = "Title",
            imageUrl = "image",
            releaseDate = null,
            rating = 4.5,
            platforms = listOf(PlatformWrapperDto(platform = PlatformDto(GamePlatform.PC.id)))
        )
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
        val bundle = GameInsertionBundle(
            games = listOf(gameEntity),
            platforms = listOf(PlatformEntity(GamePlatform.PC.id)),
            crossRefs = listOf(GamePlatformCrossRef(gameId = 1, platformId = GamePlatform.PC.id))
        )
        val entityWithPlatforms = GameWithPlatforms(
            game = gameEntity,
            platforms = listOf(PlatformEntity(GamePlatform.PC.id))
        )
        val pagingSource = object : PagingSource<Int, GameWithPlatforms>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GameWithPlatforms> {
                return LoadResult.Page(
                    data = listOf(entityWithPlatforms),
                    prevKey = null,
                    nextKey = null
                )
            }

            override fun getRefreshKey(state: androidx.paging.PagingState<Int, GameWithPlatforms>): Int? = null
        }

        val robot = GameRepositoryTestDriver.Builder()
            .gameApiGetGames(
                PaginatedResponseDto(
                    count = 1,
                    next = null,
                    previous = null,
                    results = listOf(dto)
                )
            )
            .dtoMapperMap(dto, mappedGame)
            .entityMapperMap(listOf(mappedGame), bundle)
            .gameDaoPagingSource(pagingSource)
            .entityMapperMap(entityWithPlatforms, mappedGame)
            .remoteKeysDaoGetRemoteKey(null)
            .build()

        val pagingData = robot.getGames().first()
        val differ = AsyncPagingDataDiffer(
            diffCallback = object : DiffUtil.ItemCallback<Game>() {
                override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean = oldItem.id == newItem.id
                override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean = oldItem == newItem
            },
            updateCallback = NoopListUpdateCallback,
            workerDispatcher = dispatcher
        )

        differ.submitData(pagingData)
        advanceUntilIdle()

        assertEquals(listOf(mappedGame), differ.snapshot().items)
    }
}

private object NoopListUpdateCallback : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
