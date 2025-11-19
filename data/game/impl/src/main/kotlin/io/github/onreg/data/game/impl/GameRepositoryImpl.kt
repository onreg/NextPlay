package io.github.onreg.data.game.impl

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.dao.GameRemoteKeysDao
import io.github.onreg.core.network.rawg.api.GameApi
import io.github.onreg.data.game.api.Game
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.game.impl.mapper.GameDtoMapper
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import io.github.onreg.data.game.impl.paging.GamePagingConfig
import io.github.onreg.data.game.impl.paging.GameRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalPagingApi::class)
public class GameRepositoryImpl(
    private val gameApi: GameApi,
    private val gameDao: GameDao,
    private val remoteKeysDao: GameRemoteKeysDao,
    private val pagingConfig: GamePagingConfig,
    private val gameDtoMapper: GameDtoMapper,
    private val gameEntityMapper: GameEntityMapper,
) : GameRepository {

    override fun getGames(): Flow<PagingData<Game>> {
        val mediator = GameRemoteMediator(
            gameApi = gameApi,
            gameDao = gameDao,
            remoteKeysDao = remoteKeysDao,
            pagingConfig = pagingConfig,
            dtoMapper = gameDtoMapper,
            entityMapper = gameEntityMapper
        )
        return Pager(
            config = pagingConfig.asPagingConfig(),
            remoteMediator = mediator
        ) {
            gameDao.pagingSource()
        }.flow.map { pagingData ->
            pagingData.map(gameEntityMapper::map)
        }
    }
}
