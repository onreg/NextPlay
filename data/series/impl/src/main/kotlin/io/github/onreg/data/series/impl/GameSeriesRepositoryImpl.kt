package io.github.onreg.data.series.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.series.api.GameSeriesRepository
import io.github.onreg.data.series.impl.mapper.SeriesGameMapper
import io.github.onreg.data.series.impl.paging.SeriesRemoteMediatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

public class GameSeriesRepositoryImpl
    @Inject
    constructor(
        private val seriesDao: SeriesDao,
        private val pagingConfig: PagingConfig,
        private val remoteMediatorFactory: SeriesRemoteMediatorFactory,
        private val seriesGameMapper: SeriesGameMapper,
    ) : GameSeriesRepository {
        override fun getSeries(parentGameId: Int): Flow<PagingData<Game>> = Pager(
            config = pagingConfig,
            remoteMediator = remoteMediatorFactory.create(parentGameId),
        ) {
            seriesDao.pagingSource(parentGameId)
        }.flow.map { pagingData ->
            pagingData.map(seriesGameMapper::map)
        }
    }
