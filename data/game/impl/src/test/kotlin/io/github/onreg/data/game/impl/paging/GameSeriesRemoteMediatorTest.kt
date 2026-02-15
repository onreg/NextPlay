package io.github.onreg.data.game.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.core.db.series.dao.SeriesRemoteKeysDao
import io.github.onreg.core.db.series.entity.SeriesEntity
import io.github.onreg.core.db.series.entity.SeriesRemoteKeysEntity
import io.github.onreg.core.network.rawg.api.GameSeriesApi
import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.data.game.impl.mapper.GameDtoMapper
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.check
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertTrue

internal class GameSeriesRemoteMediatorTest {
    private val gameSeriesApi: GameSeriesApi = mock()
    private val gameDao: GameDao = mock()
    private val seriesDao: SeriesDao = mock()
    private val seriesRemoteKeysDao: SeriesRemoteKeysDao = mock()
    private val dtoMapper: GameDtoMapper = mock()
    private val entityMapper: GameEntityMapper = mock()
    private val dto = GameDto(
        id = 1,
        title = "Series",
        imageUrl = "https://img",
        releaseDate = null,
        rating = 4.0,
        platforms = emptyList(),
    )
    private val mappedGame = Game(
        id = 1,
        title = "Series",
        imageUrl = "https://img",
        releaseDate = null,
        rating = 4.0,
        platforms = setOf(GamePlatform.PC),
    )
    private val insertionBundle = GameInsertionBundle(
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
    private val seriesEntries = listOf(
        SeriesEntity(
            gameId = 1,
            position = 0,
        ),
    )
    private val entityWithPlatforms = GameWithPlatforms(
        game = GameEntity(
            id = 1,
            title = "Series",
            imageUrl = "https://img",
            releaseDate = null,
            rating = 4.0,
        ),
        platforms = listOf(PlatformEntity(GamePlatform.PC.id)),
    )

    private val mediator = GameSeriesRemoteMediator(
        gameId = 77,
        gameSeriesApi = gameSeriesApi,
        gameDao = gameDao,
        seriesDao = seriesDao,
        seriesRemoteKeysDao = seriesRemoteKeysDao,
        dtoMapper = dtoMapper,
        entityMapper = entityMapper,
    )

    @Test
    fun `refresh clears and inserts using game scoped keys`() = runTest {
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
        dtoMapper.stub { on { map(dto) } doReturn mappedGame }
        entityMapper.stub { on { map(listOf(mappedGame)) } doReturn insertionBundle }
        entityMapper.stub {
            on { mapSeriesEntries(listOf(mappedGame), 0) } doReturn seriesEntries
        }

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        verify(seriesDao).deleteAll()
        verify(seriesRemoteKeysDao).deleteAll()
        verify(gameDao).insertGamesWithPlatforms(insertionBundle)
        verify(entityMapper).mapSeriesEntries(listOf(mappedGame), 0)
        verify(seriesDao).insertSeriesEntries(seriesEntries)
        verify(seriesRemoteKeysDao).insertRemoteKeys(
            org.mockito.kotlin.check {
                assertTrue(it.all { key -> key.gameId == 1 })
            },
        )
    }

    @Test
    fun `refresh parses next page and keeps paging open`() = runTest {
        gameSeriesApi.stub {
            onBlocking { getGameSeries(77, 1, 10) } doReturn NetworkResponse.Success(
                PaginatedResponseDto(
                    count = 1,
                    next = "https://example.com?page=2",
                    previous = null,
                    results = listOf(dto),
                ),
            )
        }
        dtoMapper.stub { on { map(dto) } doReturn mappedGame }
        entityMapper.stub { on { map(listOf(mappedGame)) } doReturn insertionBundle }
        entityMapper.stub {
            on { mapSeriesEntries(listOf(mappedGame), 0) } doReturn seriesEntries
        }

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(
            result is RemoteMediator.MediatorResult.Success && !result.endOfPaginationReached,
        )
        verify(seriesRemoteKeysDao).insertRemoteKeys(
            check {
                assertTrue(it.all { key -> key.nextKey == 2 })
            },
        )
    }

    @Test
    fun `prepend ends pagination without api call`() = runTest {
        val result = mediator.load(LoadType.PREPEND, emptyPagingState())

        assertTrue(
            result is RemoteMediator.MediatorResult.Success && result.endOfPaginationReached,
        )
        verify(gameSeriesApi, never()).getGameSeries(77, 1, 10)
    }

    @Test
    fun `append with empty pages waits for refresh`() = runTest {
        val result = mediator.load(LoadType.APPEND, emptyPagingState())

        assertTrue(
            result is RemoteMediator.MediatorResult.Success && !result.endOfPaginationReached,
        )
        verify(gameSeriesApi, never()).getGameSeries(77, 1, 10)
    }

    @Test
    fun `append with null next key ends pagination`() = runTest {
        seriesRemoteKeysDao.stub {
            onBlocking { getByGameId(1) } doReturn SeriesRemoteKeysEntity(
                gameId = 1,
                prevKey = 1,
                nextKey = null,
            )
        }

        val result = mediator.load(LoadType.APPEND, pagingStateWithLastItem(entityWithPlatforms))

        assertTrue(
            result is RemoteMediator.MediatorResult.Success && result.endOfPaginationReached,
        )
        verify(gameSeriesApi, never()).getGameSeries(77, 1, 10)
    }

    @Test
    fun `append with next key loads next page`() = runTest {
        val appendedEntries = listOf(
            SeriesEntity(
                gameId = 1,
                position = 20,
            ),
        )

        seriesRemoteKeysDao.stub {
            onBlocking { getByGameId(1) } doReturn SeriesRemoteKeysEntity(
                gameId = 1,
                prevKey = 2,
                nextKey = 3,
            )
        }
        gameSeriesApi.stub {
            onBlocking { getGameSeries(77, 3, 10) } doReturn NetworkResponse.Success(
                PaginatedResponseDto(
                    count = 1,
                    next = "https://example.com?page=4",
                    previous = null,
                    results = listOf(dto),
                ),
            )
        }
        dtoMapper.stub { on { map(dto) } doReturn mappedGame }
        entityMapper.stub { on { map(listOf(mappedGame)) } doReturn insertionBundle }
        entityMapper.stub {
            on { mapSeriesEntries(listOf(mappedGame), 20) } doReturn appendedEntries
        }

        val result = mediator.load(LoadType.APPEND, pagingStateWithLastItem(entityWithPlatforms))

        assertTrue(
            result is RemoteMediator.MediatorResult.Success && !result.endOfPaginationReached,
        )
        verify(gameSeriesApi).getGameSeries(77, 3, 10)
        verify(entityMapper).mapSeriesEntries(listOf(mappedGame), 20)
        verify(seriesDao).insertSeriesEntries(appendedEntries)
        verify(seriesRemoteKeysDao).insertRemoteKeys(
            check {
                assertTrue(it.all { key -> key.nextKey == 4 })
            },
        )
    }

    @Test
    fun `load returns error on network failure`() = runTest {
        gameSeriesApi.stub {
            onBlocking { getGameSeries(77, 1, 10) } doReturn NetworkResponse.Failure.NetworkError(
                IOException("boom"),
            )
        }

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    @Test
    fun `load returns error on server failure`() = runTest {
        gameSeriesApi.stub {
            onBlocking { getGameSeries(77, 1, 10) } doReturn NetworkResponse.Failure.OtherError(
                null,
            )
        }

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    private fun emptyPagingState(): PagingState<Int, GameWithPlatforms> = PagingState(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 10),
        leadingPlaceholderCount = 0,
    )

    private fun pagingStateWithLastItem(
        lastItem: GameWithPlatforms,
    ): PagingState<Int, GameWithPlatforms> = PagingState(
        pages = listOf(
            PagingSource.LoadResult.Page(
                data = listOf(lastItem),
                prevKey = null,
                nextKey = null,
            ),
        ),
        anchorPosition = 0,
        config = PagingConfig(pageSize = 10),
        leadingPlaceholderCount = 0,
    )
}
