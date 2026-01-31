package io.github.onreg.data.game.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.TransactionProvider
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.dao.GameRemoteKeysDao
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.network.rawg.api.GameApi
import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.impl.mapper.GameDtoMapper
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

internal class GameRemoteMediatorTestDriver private constructor(
    val gameApi: GameApi,
    val gameDao: GameDao,
    val remoteKeysDao: GameRemoteKeysDao,
    val dtoMapper: GameDtoMapper,
    val entityMapper: GameEntityMapper,
    val pagingConfig: PagingConfig,
    private val transactionProvider: TransactionProvider,
) : RemoteMediator<Int, GameWithPlatforms>() {
    private val mediator by lazy {
        GameRemoteMediator(
            gameApi = gameApi,
            gameDao = gameDao,
            remoteKeysDao = remoteKeysDao,
            dtoMapper = dtoMapper,
            entityMapper = entityMapper,
            transactionProvider = transactionProvider,
        )
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, GameWithPlatforms>,
    ) = mediator.load(loadType, state)

    fun emptyPagingState(): PagingState<Int, GameWithPlatforms> = PagingState(
        pages = emptyList(),
        anchorPosition = null,
        config = pagingConfig,
        leadingPlaceholderCount = 0,
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
        private val pagingConfig = PagingConfig(
            pageSize = 2,
            prefetchDistance = 1,
            initialLoadSize = 2,
            maxSize = 10,
        )

        fun gameApiGetGames(response: NetworkResponse<PaginatedResponseDto<GameDto>>): Builder =
            apply {
                gameApi.stub {
                    onBlocking {
                        getGames(
                            page = 0,
                            pageSize = pagingConfig.pageSize,
                        )
                    } doReturn response
                }
            }

        fun gameDtoMapperMap(
            dto: GameDto,
            mapped: Game,
        ): Builder = apply {
            dtoMapper.stub { on { map(dto) } doReturn mapped }
        }

        fun gameEntityMapperMap(
            games: List<Game>,
            startOrder: Long,
            bundle: GameInsertionBundle,
        ): Builder = apply {
            entityMapper.stub { on { map(games, startOrder) } doReturn bundle }
        }

        fun build() = GameRemoteMediatorTestDriver(
            gameApi = gameApi,
            gameDao = gameDao,
            remoteKeysDao = remoteKeysDao,
            dtoMapper = dtoMapper,
            entityMapper = entityMapper,
            pagingConfig = pagingConfig,
            transactionProvider = transactionProvider,
        )
    }
}
