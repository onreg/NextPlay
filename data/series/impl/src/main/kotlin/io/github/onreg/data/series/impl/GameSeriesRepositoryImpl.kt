package io.github.onreg.data.series.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.data.game.list.api.model.Game
import io.github.onreg.data.game.list.impl.mapper.GameEntityMapper
import io.github.onreg.data.series.api.GameSeriesRepository
import io.github.onreg.data.series.impl.paging.GameSeriesRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

public class GameSeriesRepositoryImpl
    @Inject
    constructor(
        private val seriesDao: SeriesDao,
        private val gameEntityMapper: GameEntityMapper,
        private val remoteMediatorFactory: GameSeriesRemoteMediatorFactory,
    ) : GameSeriesRepository {
        private val pagingConfig: PagingConfig = PagingConfig(
            pageSize = 10,
            prefetchDistance = 3,
            initialLoadSize = 20,
            maxSize = 200,
            enablePlaceholders = false,
        )

        override fun getSeries(parentGameId: Int): Flow<PagingData<Game>> = Pager(
            config = pagingConfig,
            remoteMediator = remoteMediatorFactory.create(parentGameId),
        ) {
            seriesDao.pagingSource(parentGameId)
        }.flow.map { pagingData ->
            pagingData.map(gameEntityMapper::map)
        }
    }

public fun interface GameSeriesRemoteMediatorFactory {
    public fun create(parentGameId: Int): GameSeriesRemoteMediator
}
