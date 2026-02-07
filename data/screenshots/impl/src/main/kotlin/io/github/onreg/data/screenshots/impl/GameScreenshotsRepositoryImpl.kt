package io.github.onreg.data.screenshots.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.onreg.core.db.screenshots.dao.GameScreenshotsDao
import io.github.onreg.data.screenshots.api.GameScreenshotsRepository
import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotEntityMapper
import io.github.onreg.data.screenshots.impl.paging.GameScreenshotsRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

public class GameScreenshotsRepositoryImpl
    @Inject
    constructor(
        private val screenshotsDao: GameScreenshotsDao,
        private val entityMapper: ScreenshotEntityMapper,
        private val remoteMediatorFactory: GameScreenshotsRemoteMediatorFactory,
    ) : GameScreenshotsRepository {
        private val pagingConfig: PagingConfig = PagingConfig(
            pageSize = 10,
            prefetchDistance = 3,
            initialLoadSize = 20,
            maxSize = 200,
            enablePlaceholders = false,
        )

        override fun getScreenshots(gameId: Int): Flow<PagingData<Screenshot>> = Pager(
            config = pagingConfig,
            remoteMediator = remoteMediatorFactory.create(gameId),
        ) {
            screenshotsDao.pagingSource(gameId)
        }.flow.map { pagingData ->
            pagingData.map(entityMapper::map)
        }
    }

public fun interface GameScreenshotsRemoteMediatorFactory {
    public fun create(gameId: Int): GameScreenshotsRemoteMediator
}
