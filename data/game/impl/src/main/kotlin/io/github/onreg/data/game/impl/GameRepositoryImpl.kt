package io.github.onreg.data.game.impl

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.RemoteMediator
import androidx.paging.map
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import io.github.onreg.data.game.impl.paging.GamePagingConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Provider

public class GameRepositoryImpl @Inject constructor(
    private val gameDao: GameDao,
    private val pagingConfig: GamePagingConfig,
    private val gameEntityMapper: GameEntityMapper,
    private val gameRemoteMediatorProvider: Provider<RemoteMediator<Int, GameWithPlatforms>>
) : GameRepository {

    override fun getGames(): Flow<PagingData<Game>> {
        return Pager(
            config = pagingConfig.asPagingConfig(),
            remoteMediator = gameRemoteMediatorProvider.get()
        ) {
            gameDao.pagingSource()
        }.flow.map { pagingData ->
            pagingData.map(gameEntityMapper::map)
        }
    }
}
