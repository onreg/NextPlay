package io.github.onreg.data.series.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.TransactionProvider
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.core.db.series.dao.SeriesRemoteKeysDao
import io.github.onreg.core.db.series.entity.SeriesGameEntity
import io.github.onreg.core.db.series.entity.SeriesRemoteKeysEntity
import io.github.onreg.core.network.rawg.api.GameSeriesApi
import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.rawg.dto.PlatformDto
import io.github.onreg.core.network.rawg.dto.PlatformWrapperDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.data.series.impl.mapper.SeriesGameDtoMapper
import io.github.onreg.data.series.impl.mapper.SeriesGameEntityMapper
import io.github.onreg.data.series.impl.mapper.SeriesInsertionBundle
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertTrue

internal class SeriesRemoteMediatorTest {
    private val api: GameSeriesApi = mock()
    private val gameDao: GameDao = mock()
    private val seriesDao: SeriesDao = mock()
    private val remoteKeysDao: SeriesRemoteKeysDao = mock()
    private val dtoMapper: SeriesGameDtoMapper = mock()
    private val entityMapper: SeriesGameEntityMapper = mock()
    private val transactionProvider = object : TransactionProvider {
        override suspend fun <T> run(block: suspend () -> T): T = block()
    }

    private val pagingConfig = PagingConfig(
        pageSize = 2,
        prefetchDistance = 1,
        initialLoadSize = 2,
        maxSize = 10,
    )

    @Test
    fun `load refresh inserts series games and remote keys`() = runTest {
        val dto = seriesDto()
        val model = seriesModel()
        val bundle = seriesBundle()

        stubApi(dto)
        stubMappers(dto, model, bundle)

        val result = buildMediator().load(
            LoadType.REFRESH,
            pagingState(),
        )

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        verify(seriesDao).clearForParent(10)
        verify(remoteKeysDao).clearForParent(10)
        verify(gameDao).insertGamesWithPlatforms(
            GameInsertionBundle(
                games = bundle.games,
                listEntities = emptyList(),
                platforms = bundle.platforms,
                crossRefs = bundle.crossRefs,
            ),
        )
        verify(seriesDao).insertAll(bundle.seriesEntities)
        verify(remoteKeysDao).insertRemoteKeys(
            listOf(
                SeriesRemoteKeysEntity(
                    parentGameId = 10,
                    gameId = 1,
                    prevKey = null,
                    nextKey = 2,
                ),
            ),
        )
    }

    private fun seriesDto(): GameDto = GameDto(
        id = 1,
        title = "Series",
        imageUrl = "image",
        releaseDate = null,
        rating = 4.5,
        platforms = listOf(
            PlatformWrapperDto(
                platform = PlatformDto(GamePlatform.PC.id),
            ),
        ),
    )

    private fun seriesModel(): Game = Game(
        id = 1,
        title = "Series",
        imageUrl = "image",
        releaseDate = null,
        rating = 4.5,
        platforms = setOf(GamePlatform.PC),
    )

    private fun seriesBundle(): SeriesInsertionBundle = SeriesInsertionBundle(
        games = listOf(
            GameEntity(
                id = 1,
                title = "Series",
                imageUrl = "image",
                releaseDate = null,
                rating = 4.5,
            ),
        ),
        platforms = listOf(PlatformEntity(GamePlatform.PC.id)),
        crossRefs = listOf(
            GamePlatformCrossRef(
                gameId = 1,
                platformId = GamePlatform.PC.id,
            ),
        ),
        seriesEntities = listOf(
            SeriesGameEntity(
                parentGameId = 10,
                gameId = 1,
                insertionOrder = 0,
            ),
        ),
    )

    private fun stubApi(dto: GameDto) {
        api.stub {
            onBlocking { getSeries(10, 1, pagingConfig.pageSize) } doReturn
                NetworkResponse.Success(
                    PaginatedResponseDto(
                        count = 1,
                        next = "https://example.com?page=2",
                        previous = null,
                        results = listOf(dto),
                    ),
                )
        }
    }

    private fun stubMappers(
        dto: GameDto,
        model: Game,
        bundle: SeriesInsertionBundle,
    ) {
        dtoMapper.stub { on { map(dto) } doReturn model }
        entityMapper.stub { on { map(listOf(model), 10, 0) } doReturn bundle }
    }

    private fun buildMediator(): SeriesRemoteMediator = SeriesRemoteMediator(
        parentGameId = 10,
        seriesApi = api,
        gameDao = gameDao,
        seriesDao = seriesDao,
        remoteKeysDao = remoteKeysDao,
        dtoMapper = dtoMapper,
        entityMapper = entityMapper,
        transactionProvider = transactionProvider,
    )

    private fun pagingState(): PagingState<Int, GameWithPlatforms> =
        PagingState(emptyList(), null, pagingConfig, 0)
}
