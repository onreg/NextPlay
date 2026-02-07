package io.github.onreg.data.series.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.list.dao.GameListDao
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.core.db.series.dao.SeriesRemoteKeysDao
import io.github.onreg.core.network.rawg.api.GameSeriesApi
import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.game.list.api.model.Game
import io.github.onreg.data.game.list.api.model.GamePlatform
import io.github.onreg.data.game.list.impl.mapper.GameDtoMapper
import io.github.onreg.data.game.list.impl.mapper.GameEntityMapper
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertTrue

internal class GameSeriesRemoteMediatorTest {
    private val gameSeriesApi: GameSeriesApi = mock()
    private val gameListDao: GameListDao = mock()
    private val seriesDao: SeriesDao = mock()
    private val seriesRemoteKeysDao: SeriesRemoteKeysDao = mock()
    private val dtoMapper: GameDtoMapper = mock()
    private val entityMapper: GameEntityMapper = mock()

    private val mediator = GameSeriesRemoteMediator(
        parentGameId = 77,
        gameSeriesApi = gameSeriesApi,
        gameListDao = gameListDao,
        seriesDao = seriesDao,
        seriesRemoteKeysDao = seriesRemoteKeysDao,
        dtoMapper = dtoMapper,
        entityMapper = entityMapper,
    )

    @Test
    fun `refresh clears and inserts using parent scoped keys`() = runTest {
        val dto = GameDto(
            id = 1,
            title = "Series",
            imageUrl = "https://img",
            releaseDate = null,
            rating = 4.0,
            platforms = emptyList(),
        )
        val game = Game(
            id = 1,
            title = "Series",
            imageUrl = "https://img",
            releaseDate = null,
            rating = 4.0,
            platforms = setOf(GamePlatform.PC),
        )
        val bundle = GameInsertionBundle(
            games = listOf(
                GameEntity(
                    id = 1,
                    title = "Series",
                    imageUrl = "https://img",
                    releaseDate = null,
                    rating = 4.0,
                ),
            ),
            platforms = listOf(PlatformEntity(GamePlatform.PC.id)),
            crossRefs = listOf(GamePlatformCrossRef(gameId = 1, platformId = GamePlatform.PC.id)),
        )

        gameSeriesApi.stub {
            onBlocking { getGameSeries(77, 1, 10) } doReturn NetworkResponse.Success(
                PaginatedResponseDto(
                    count = 1,
                    next = null,
                    previous = null,
                    results = listOf(dto),
                ),
            )
        }
        dtoMapper.stub { on { map(dto) } doReturn game }
        entityMapper.stub { on { map(listOf(game)) } doReturn bundle }

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        verify(seriesDao).clearByParentGameId(77)
        verify(seriesRemoteKeysDao).clearByParentGameId(77)
        verify(gameListDao).insertGamesWithPlatforms(bundle)
        verify(seriesRemoteKeysDao).insert(
            org.mockito.kotlin.check {
                assertTrue(it.all { key -> key.parentGameId == 77 })
            },
        )
    }

    private fun emptyPagingState(): PagingState<Int, GameWithPlatforms> = PagingState(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 10),
        leadingPlaceholderCount = 0,
    )
}
