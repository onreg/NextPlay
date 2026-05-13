package io.github.onreg.data.screenshots.impl

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.screenshots.dao.GameScreenshotsDao
import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.data.screenshots.api.GameScreenshotsRepository
import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotEntityMapper
import io.github.onreg.data.screenshots.impl.paging.GameScreenshotsRemoteMediator
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

internal class GameScreenshotsRepositoryTestDriver private constructor(
    val dao: GameScreenshotsDao,
    val entityMapper: ScreenshotEntityMapper,
    val remoteMediatorFactory: GameScreenshotsRemoteMediatorFactory,
) : GameScreenshotsRepository {
    private val repository: GameScreenshotsRepository by lazy {
        GameScreenshotsRepositoryImpl(
            screenshotsDao = dao,
            entityMapper = entityMapper,
            remoteMediatorFactory = remoteMediatorFactory,
        )
    }

    override fun getScreenshots(gameId: Int) = repository.getScreenshots(gameId)

    class Builder {
        private val dao: GameScreenshotsDao = mock()
        private val entityMapper: ScreenshotEntityMapper = mock()
        private val remoteMediator: GameScreenshotsRemoteMediator = mock {
            onBlocking { load(any(), any()) } doReturn RemoteMediator.MediatorResult.Success(
                endOfPaginationReached = true,
            )
        }
        private val remoteMediatorFactory: GameScreenshotsRemoteMediatorFactory = mock {
            on { create(any()) } doReturn remoteMediator
        }

        fun entityMapperMap(
            entity: ScreenshotEntity,
            mapped: Screenshot,
        ): Builder = apply {
            entityMapper.stub { on { map(entity) } doReturn mapped }
        }

        fun daoPagingSource(pagingSource: List<ScreenshotEntity>): Builder = apply {
            val source: PagingSource<Int, ScreenshotEntity> = mock {
                onBlocking { load(any()) } doReturn LoadResult.Page(
                    data = pagingSource,
                    prevKey = null,
                    nextKey = null,
                )
            }
            dao.stub { on { pagingSource(any()) } doReturn source }
        }

        fun build(): GameScreenshotsRepositoryTestDriver = GameScreenshotsRepositoryTestDriver(
            dao = dao,
            entityMapper = entityMapper,
            remoteMediatorFactory = remoteMediatorFactory,
        )
    }
}
