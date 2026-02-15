package io.github.onreg.data.game.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.RemoteMediator
import androidx.paging.map
import io.github.onreg.core.db.game.list.dao.GameListDao
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import io.github.onreg.data.game.impl.paging.GameSeriesRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Provider

public class GameRepositoryImpl
    @Inject
    constructor(
        private val gameListDao: GameListDao,
        private val seriesDao: SeriesDao,
        private val pagingConfig: PagingConfig,
        private val gameEntityMapper: GameEntityMapper,
        private val gameRemoteMediatorProvider: Provider<RemoteMediator<Int, GameWithPlatforms>>,
        private val gameSeriesRemoteMediatorFactory: GameSeriesRemoteMediatorFactory,
    ) : GameRepository {
        override fun getGames(): Flow<PagingData<Game>> = Pager(
            config = pagingConfig,
            remoteMediator = gameRemoteMediatorProvider.get(),
        ) {
            gameListDao.pagingSource()
        }.flow.map { pagingData ->
            pagingData.map(gameEntityMapper::map)
        }

        override fun getSeries(gameId: Int): Flow<PagingData<Game>> = Pager(
            config = pagingConfig,
            remoteMediator = gameSeriesRemoteMediatorFactory.create(gameId),
        ) {
            seriesDao.pagingSource()
        }.flow.map { pagingData ->
            pagingData.map(gameEntityMapper::map)
        }
    }

public fun interface GameSeriesRemoteMediatorFactory {
    public fun create(gameId: Int): GameSeriesRemoteMediator
}
