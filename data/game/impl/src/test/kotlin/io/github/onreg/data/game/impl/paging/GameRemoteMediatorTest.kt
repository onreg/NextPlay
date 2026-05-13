package io.github.onreg.data.game.impl.paging

import androidx.paging.LoadType
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.list.entity.GameListEntity
import io.github.onreg.core.db.game.list.entity.GameListRemoteKeysEntity
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.rawg.dto.PlatformDto
import io.github.onreg.core.network.rawg.dto.PlatformWrapperDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertTrue

internal class GameRemoteMediatorTest {
    private val dto = GameDto(
        id = 1,
        title = "Title",
        imageUrl = "image",
        releaseDate = null,
        rating = 4.5,
        platforms = listOf(PlatformWrapperDto(platform = PlatformDto(GamePlatform.PC.id))),
    )

    private val mappedGame = Game(
        id = 1,
        title = "Title",
        imageUrl = "image",
        releaseDate = null,
        rating = 4.5,
        platforms = setOf(GamePlatform.PC),
    )

    private val gameEntity = GameEntity(
        id = 1,
        title = "Title",
        imageUrl = "image",
        releaseDate = null,
        rating = 4.5,
    )

    private val insertionBundle = GameInsertionBundle(
        games = listOf(gameEntity),
        platforms = listOf(PlatformEntity(GamePlatform.PC.id)),
        crossRefs = listOf(GamePlatformCrossRef(gameId = 1, platformId = GamePlatform.PC.id)),
    )
    private val gameListEntries = listOf(
        GameListEntity(
            gameId = 1,
            position = 0,
        ),
    )
    private val entityWithPlatforms = GameWithPlatforms(
        game = gameEntity,
        platforms = listOf(PlatformEntity(GamePlatform.PC.id)),
    )

    private val driver = GameRemoteMediatorTestDriver
        .Builder()
        .gameApiGetGames(
            NetworkResponse.Success(
                PaginatedResponseDto(
                    count = 1,
                    next = "https://example.com?page=2",
                    previous = null,
                    results = listOf(dto),
                ),
            ),
        ).gameDtoMapperMap(dto, mappedGame)
        .gameEntityMapperMap(listOf(mappedGame), bundle = insertionBundle)
        .gameEntityMapperMapGameListEntries(
            games = listOf(mappedGame),
            startPosition = 0,
            entities = gameListEntries,
        ).build()

    @Test
    fun `load refresh inserts games and remote keys`() = runTest {
        val result = driver.load(LoadType.REFRESH, driver.emptyPagingState())

        assertTrue(
            result is RemoteMediator.MediatorResult.Success && !result.endOfPaginationReached,
        )

        verify(driver.gameApi).getGames(
            page = 1,
            pageSize = driver.pagingConfig.pageSize,
        )
        verify(driver.dtoMapper).map(dto)
        verify(driver.entityMapper).map(listOf(mappedGame))
        verify(driver.entityMapper).mapGameListEntries(listOf(mappedGame), 0)
        verify(driver.gameListDao).deleteAll()
        verify(driver.remoteKeysDao).deleteAll()
        verify(driver.gameDao).insertGamesWithPlatforms(insertionBundle)
        verify(driver.gameListDao).insertGameListEntries(gameListEntries)
        verify(driver.remoteKeysDao).insertRemoteKeys(
            listOf(
                GameListRemoteKeysEntity(
                    gameId = 1,
                    prevKey = null,
                    nextKey = 2,
                ),
            ),
        )
    }

    @Test
    fun `load prepend returns end of pagination without api call`() = runTest {
        val result = driver.load(LoadType.PREPEND, driver.emptyPagingState())

        assertTrue(
            result is RemoteMediator.MediatorResult.Success && result.endOfPaginationReached,
        )
        verify(driver.gameApi, never()).getGames(
            page = 1,
            pageSize = driver.pagingConfig.pageSize,
        )
    }

    @Test
    fun `load append with empty pages waits for refresh`() = runTest {
        val result = driver.load(LoadType.APPEND, driver.emptyPagingState())

        assertTrue(
            result is RemoteMediator.MediatorResult.Success && !result.endOfPaginationReached,
        )
        verify(driver.gameApi, never()).getGames(
            page = 1,
            pageSize = driver.pagingConfig.pageSize,
        )
    }

    @Test
    fun `load append without remote key returns error`() = runTest {
        val driver = GameRemoteMediatorTestDriver
            .Builder()
            .remoteKeysDaoGetByGameId(1, null)
            .build()

        val pagingState = driver.pagingStateWithLastItem(entityWithPlatforms)

        val result = driver.load(LoadType.APPEND, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        verify(driver.gameApi, never()).getGames(
            page = 1,
            pageSize = driver.pagingConfig.pageSize,
        )
    }

    @Test
    fun `load append with null next key ends pagination`() = runTest {
        val driver = GameRemoteMediatorTestDriver
            .Builder()
            .remoteKeysDaoGetByGameId(
                1,
                GameListRemoteKeysEntity(
                    gameId = 1,
                    prevKey = 1,
                    nextKey = null,
                ),
            ).build()

        val pagingState = driver.pagingStateWithLastItem(entityWithPlatforms)

        val result = driver.load(LoadType.APPEND, pagingState)

        assertTrue(
            result is RemoteMediator.MediatorResult.Success && result.endOfPaginationReached,
        )
        verify(driver.gameApi, never()).getGames(
            page = 1,
            pageSize = driver.pagingConfig.pageSize,
        )
    }

    @Test
    fun `load append with remote key fetches next page`() = runTest {
        val appendedEntries = listOf(
            GameListEntity(
                gameId = 1,
                position = 4,
            ),
        )

        val driver = GameRemoteMediatorTestDriver
            .Builder()
            .remoteKeysDaoGetByGameId(
                1,
                GameListRemoteKeysEntity(
                    gameId = 1,
                    prevKey = 2,
                    nextKey = 3,
                ),
            ).gameApiGetGames(
                page = 3,
                response = NetworkResponse.Success(
                    PaginatedResponseDto(
                        count = 1,
                        next = "https://example.com?page=4",
                        previous = null,
                        results = listOf(dto),
                    ),
                ),
            ).gameDtoMapperMap(dto, mappedGame)
            .gameEntityMapperMap(listOf(mappedGame), bundle = insertionBundle)
            .gameEntityMapperMapGameListEntries(
                games = listOf(mappedGame),
                startPosition = 4,
                entities = appendedEntries,
            ).build()

        val pagingState = driver.pagingStateWithLastItem(entityWithPlatforms)

        val result = driver.load(LoadType.APPEND, pagingState)

        assertTrue(
            result is RemoteMediator.MediatorResult.Success && !result.endOfPaginationReached,
        )
        verify(driver.gameApi).getGames(
            page = 3,
            pageSize = driver.pagingConfig.pageSize,
        )
        verify(driver.entityMapper).mapGameListEntries(listOf(mappedGame), 4)
        verify(driver.gameListDao).insertGameListEntries(appendedEntries)
        verify(driver.remoteKeysDao).insertRemoteKeys(
            listOf(
                GameListRemoteKeysEntity(
                    gameId = 1,
                    prevKey = 2,
                    nextKey = 4,
                ),
            ),
        )
    }

    @Test
    fun `load refresh returns error on network failure`() = runTest {
        val driver = GameRemoteMediatorTestDriver
            .Builder()
            .gameApiGetGames(NetworkResponse.Failure.NetworkError(IOException("boom")))
            .build()

        val result = driver.load(LoadType.REFRESH, driver.emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    @Test
    fun `load refresh returns error on server failure`() = runTest {
        val driver = GameRemoteMediatorTestDriver
            .Builder()
            .gameApiGetGames(NetworkResponse.Failure.OtherError(null))
            .build()

        val result = driver.load(LoadType.REFRESH, driver.emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }
}
