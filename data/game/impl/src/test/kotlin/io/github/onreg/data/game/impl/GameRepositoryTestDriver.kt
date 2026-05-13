package io.github.onreg.data.game.impl

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.game.list.dao.GameListDao
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import io.github.onreg.data.game.impl.paging.GameSeriesRemoteMediator
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

internal class GameRepositoryTestDriver private constructor(
    val gameDao: GameListDao,
    val seriesDao: SeriesDao,
    val entityMapper: GameEntityMapper,
    val pagingConfig: PagingConfig,
    val remoteMediator: RemoteMediator<Int, GameWithPlatforms>,
    val gameSeriesRemoteMediatorFactory: GameSeriesRemoteMediatorFactory,
) : GameRepository {
    private val repository: GameRepository by lazy {
        GameRepositoryImpl(
            gameListDao = gameDao,
            seriesDao = seriesDao,
            pagingConfig = pagingConfig,
            gameEntityMapper = entityMapper,
            gameRemoteMediatorProvider = { remoteMediator },
            gameSeriesRemoteMediatorFactory = gameSeriesRemoteMediatorFactory,
        )
    }

    override fun getGames() = repository.getGames()

    override fun getSeries(gameId: Int) = repository.getSeries(gameId)

    class Builder {
        private val gameDao: GameListDao = mock()
        private val seriesDao: SeriesDao = mock()
        private val entityMapper: GameEntityMapper = mock()
        private val pagingConfig = PagingConfig(
            pageSize = 2,
            prefetchDistance = 1,
            initialLoadSize = 2,
            maxSize = 10,
        )
        private val remoteMediator: RemoteMediator<Int, GameWithPlatforms> = mock {
            onBlocking { load(any(), any()) } doReturn RemoteMediator.MediatorResult.Success(
                endOfPaginationReached = true,
            )
        }
        private val seriesRemoteMediator: GameSeriesRemoteMediator = mock()
        private val gameSeriesRemoteMediatorFactory: GameSeriesRemoteMediatorFactory = mock {
            on { create(any()) } doReturn seriesRemoteMediator
        }

        fun gameEntityMapperMap(
            gameWithPlatforms: GameWithPlatforms,
            mapped: Game,
        ): Builder = apply {
            entityMapper.stub { on { map(gameWithPlatforms) } doReturn mapped }
        }

        fun gameDaoPagingSource(pagingSource: List<GameWithPlatforms>): Builder = apply {
            val source: PagingSource<Int, GameWithPlatforms> = mock {
                onBlocking { load(any()) } doReturn LoadResult.Page(
                    data = pagingSource,
                    prevKey = null,
                    nextKey = null,
                )
            }
            gameDao.stub { on { pagingSource() } doReturn source }
        }

        fun seriesDaoPagingSource(pagingSource: List<GameWithPlatforms>): Builder = apply {
            val source: PagingSource<Int, GameWithPlatforms> = mock {
                onBlocking { load(any()) } doReturn LoadResult.Page(
                    data = pagingSource,
                    prevKey = null,
                    nextKey = null,
                )
            }
            seriesDao.stub { on { pagingSource() } doReturn source }
        }

        fun build(): GameRepositoryTestDriver = GameRepositoryTestDriver(
            gameDao = gameDao,
            seriesDao = seriesDao,
            entityMapper = entityMapper,
            pagingConfig = pagingConfig,
            remoteMediator = remoteMediator,
            gameSeriesRemoteMediatorFactory = gameSeriesRemoteMediatorFactory,
        )
    }
}
