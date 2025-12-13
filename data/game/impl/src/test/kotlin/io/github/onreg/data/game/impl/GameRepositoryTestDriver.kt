package io.github.onreg.data.game.impl

import androidx.paging.PagingSource
import io.github.onreg.core.db.TransactionProvider
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.dao.GameRemoteKeysDao
import io.github.onreg.core.db.game.entity.GameRemoteKeysEntity
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.network.rawg.api.GameApi
import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.impl.mapper.GameDtoMapper
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import io.github.onreg.data.game.impl.paging.GamePagingConfig
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.wheneverBlocking

internal class GameRepositoryTestDriver private constructor(
    val gameApi: GameApi,
    val gameDao: GameDao,
    val remoteKeysDao: GameRemoteKeysDao,
    val dtoMapper: GameDtoMapper,
    val entityMapper: GameEntityMapper,
    val pagingConfig: GamePagingConfig,
    private val transactionProvider: TransactionProvider
) : GameRepository {

    private val repository: GameRepository = GameRepositoryImpl(
        gameApi = gameApi,
        gameDao = gameDao,
        remoteKeysDao = remoteKeysDao,
        pagingConfig = pagingConfig,
        gameDtoMapper = dtoMapper,
        gameEntityMapper = entityMapper,
        transactionProvider = transactionProvider
    )

    override fun getGames() = repository.getGames()

    class Builder {
        private val transactionProvider = object : TransactionProvider {
            override suspend fun <T> run(block: suspend () -> T): T = block()
        }
        private val gameApi: GameApi = mock()
        private val gameDao: GameDao = mock()
        private val remoteKeysDao: GameRemoteKeysDao = mock()
        private val dtoMapper: GameDtoMapper = mock()
        private val entityMapper: GameEntityMapper = mock()
        private val pagingConfig = GamePagingConfig(
            pageSize = 2,
            prefetchDistance = 1,
            initialLoadSize = 2,
            maxSize = 10
        )

        fun gameApiGetGames(response: PaginatedResponseDto<GameDto>): Builder = apply {
            wheneverBlocking { gameApi.getGames(page = 1, pageSize = pagingConfig.pageSize) }
                .thenReturn(response)
        }

        fun dtoMapperMap(gameDto: GameDto, game: Game): Builder = apply {
            whenever(dtoMapper.map(gameDto)).thenReturn(game)
        }

        fun entityMapperMap(games: List<Game>, bundle: GameInsertionBundle): Builder = apply {
            whenever(entityMapper.map(games, 0)).thenReturn(bundle)
        }

        fun entityMapperMap(gameWithPlatforms: GameWithPlatforms, mapped: Game): Builder = apply {
            whenever(entityMapper.map(gameWithPlatforms)).thenReturn(mapped)
        }

        fun gameDaoPagingSource(pagingSource: PagingSource<Int, GameWithPlatforms>): Builder = apply {
            whenever(gameDao.pagingSource()).thenReturn(pagingSource)
        }

        fun remoteKeysDaoGetRemoteKey(entity: GameRemoteKeysEntity?): Builder = apply {
            wheneverBlocking { remoteKeysDao.getRemoteKey(any()) }.thenReturn(entity)
        }

        fun build(): GameRepositoryTestDriver = GameRepositoryTestDriver(
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
