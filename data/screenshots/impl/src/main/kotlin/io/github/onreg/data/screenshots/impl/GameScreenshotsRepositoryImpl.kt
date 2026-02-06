package io.github.onreg.data.screenshots.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.onreg.core.db.screenshots.dao.ScreenshotDao
import io.github.onreg.data.screenshots.api.GameScreenshotsRepository
import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotEntityMapper
import io.github.onreg.data.screenshots.impl.paging.ScreenshotsRemoteMediatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

public class GameScreenshotsRepositoryImpl
    @Inject
    constructor(
        private val screenshotDao: ScreenshotDao,
        private val pagingConfig: PagingConfig,
        private val remoteMediatorFactory: ScreenshotsRemoteMediatorFactory,
        private val entityMapper: ScreenshotEntityMapper,
    ) : GameScreenshotsRepository {
        override fun getScreenshots(gameId: Int): Flow<PagingData<Screenshot>> = Pager(
            config = pagingConfig,
            remoteMediator = remoteMediatorFactory.create(gameId),
        ) {
            screenshotDao.pagingSource(gameId)
        }.flow.map { pagingData ->
            pagingData.map(entityMapper::map)
        }
    }
