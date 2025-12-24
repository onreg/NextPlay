package io.github.onreg.data.game.impl.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.entity.GameRemoteKeysEntity
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.rawg.dto.PlatformDto
import io.github.onreg.core.network.rawg.dto.PlatformWrapperDto
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalPagingApi::class)
internal class GameRemoteMediatorTest {

    private val dto = GameDto(
        id = 1,
        title = "Title",
        imageUrl = "image",
        releaseDate = null,
        rating = 4.5,
        platforms = listOf(PlatformWrapperDto(platform = PlatformDto(GamePlatform.PC.id)))
    )

    private val mappedGame = Game(
        id = 1,
        title = "Title",
        imageUrl = "image",
        releaseDate = null,
        rating = 4.5,
        platforms = setOf(GamePlatform.PC)
    )

    private val gameEntity = GameEntity(
        id = 1,
        title = "Title",
        imageUrl = "image",
        releaseDate = null,
        rating = 4.5,
        insertionOrder = 0
    )

    private val insertionBundle = GameInsertionBundle(
        games = listOf(gameEntity),
        platforms = listOf(PlatformEntity(GamePlatform.PC.id)),
        crossRefs = listOf(GamePlatformCrossRef(gameId = 1, platformId = GamePlatform.PC.id))
    )

    private val driver = GameRemoteMediatorTestDriver.Builder()
        .gameApiGetGames(
            PaginatedResponseDto(
                count = 1,
                next = "https://example.com?page=2",
                previous = null,
                results = listOf(dto)
            )
        )
        .gameDtoMapperMap(dto, mappedGame)
        .gameEntityMapperMap(listOf(mappedGame), startOrder = 0, bundle = insertionBundle)
        .build()

    @Test
    fun `load refresh inserts games and remote keys`() = runTest {
        val result = driver.load(LoadType.REFRESH, driver.emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success && !result.endOfPaginationReached)

        verify(driver.gameApi).getGames(
            page = driver.pagingConfig.startingPage,
            pageSize = driver.pagingConfig.pageSize
        )
        verify(driver.dtoMapper).map(dto)
        verify(driver.entityMapper).map(listOf(mappedGame), 0)
        verify(driver.gameDao).clearGames()
        verify(driver.gameDao).insertGamesWithPlatforms(insertionBundle)
        verify(driver.remoteKeysDao).insertRemoteKeys(
            listOf(
                GameRemoteKeysEntity(
                    gameId = 1,
                    prevKey = null,
                    nextKey = 2
                )
            )
        )
    }
}
