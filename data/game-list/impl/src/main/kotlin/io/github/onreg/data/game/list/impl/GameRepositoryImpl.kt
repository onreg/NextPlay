package io.github.onreg.data.game.list.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.RemoteMediator
import androidx.paging.map
import io.github.onreg.core.db.game.list.dao.GameListDao
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.data.game.list.api.GameRepository
import io.github.onreg.data.game.list.api.model.Game
import io.github.onreg.data.game.list.impl.mapper.GameEntityMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Provider

private const val DEFAULT_LIST_KEY = "default"

public class GameRepositoryImpl
    @Inject
    constructor(
        private val gameDao: GameListDao,
        private val pagingConfig: PagingConfig,
        private val gameEntityMapper: GameEntityMapper,
        private val gameRemoteMediatorProvider: Provider<RemoteMediator<Int, GameWithPlatforms>>,
    ) : GameRepository {
        override fun getGames(): Flow<PagingData<Game>> = Pager(
            config = pagingConfig,
            remoteMediator = gameRemoteMediatorProvider.get(),
        ) {
            gameDao.pagingSource(DEFAULT_LIST_KEY)
        }.flow.map { pagingData ->
            pagingData.map(gameEntityMapper::map)
        }
    }
