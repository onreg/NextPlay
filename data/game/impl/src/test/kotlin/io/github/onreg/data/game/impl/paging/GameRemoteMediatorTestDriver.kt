package io.github.onreg.data.game.impl.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import io.github.onreg.core.db.TransactionProvider
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.dao.GameRemoteKeysDao
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.network.rawg.api.GameApi
import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.impl.mapper.GameDtoMapper
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.wheneverBlocking

@OptIn(ExperimentalPagingApi::class)
internal class GameRemoteMediatorTestDriver private constructor(
    val gameApi: GameApi,
    val gameDao: GameDao,
    val remoteKeysDao: GameRemoteKeysDao,
    val dtoMapper: GameDtoMapper,
    val entityMapper: GameEntityMapper,
    val pagingConfig: GamePagingConfig,
    private val transactionProvider: TransactionProvider
) {

    private val mediator = GameRemoteMediator(
        gameApi = gameApi,
        gameDao = gameDao,
        remoteKeysDao = remoteKeysDao,
        pagingConfig = pagingConfig,
        dtoMapper = dtoMapper,
        entityMapper = entityMapper,
        transactionProvider = transactionProvider
    )

    suspend fun load(loadType: LoadType, state: PagingState<Int, GameWithPlatforms>) =
        mediator.load(loadType, state)

    fun emptyPagingState(): PagingState<Int, GameWithPlatforms> =
        PagingState(
            pages = emptyList(),
            anchorPosition = null,
            config = pagingConfig.asPagingConfig(),
            leadingPlaceholderCount = 0
        )

    class Builder {
        private val gameApi: GameApi = mock()
        private val gameDao: GameDao = mock()
        private val remoteKeysDao: GameRemoteKeysDao = mock()
        private val dtoMapper: GameDtoMapper = mock()
        private val entityMapper: GameEntityMapper = mock()
        private val transactionProvider = object : TransactionProvider {
            override suspend fun <T> run(block: suspend () -> T): T = block()
        }
        private val pagingConfig = GamePagingConfig(
            pageSize = 2,
            prefetchDistance = 1,
            initialLoadSize = 2,
            maxSize = 10
        )

        fun gameApiResponse(response: PaginatedResponseDto<GameDto>): Builder = apply {
            wheneverBlocking {
                gameApi.getGames(
                    page = pagingConfig.startingPage,
                    pageSize = pagingConfig.pageSize
                )
            }.thenReturn(response)
        }

        fun dtoMapperMap(dto: GameDto, mapped: Game): Builder = apply {
            whenever(dtoMapper.map(dto)).thenReturn(mapped)
        }

        fun entityMapperMap(
            games: List<Game>,
            startOrder: Long,
            bundle: GameInsertionBundle
        ): Builder = apply {
            whenever(entityMapper.map(games, startOrder)).thenReturn(bundle)
        }

        fun build(): GameRemoteMediatorTestDriver = GameRemoteMediatorTestDriver(
            gameApi = gameApi,
            gameDao = gameDao,
            remoteKeysDao = remoteKeysDao,
            dtoMapper = dtoMapper,
            entityMapper = entityMapper,
            pagingConfig = pagingConfig,
            transactionProvider = transactionProvider
        )
    }
}
