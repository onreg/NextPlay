package io.github.onreg.data.game.impl

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.game.dao.GameListDao
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

internal class GameRepositoryTestDriver private constructor(
    val gameListDao: GameListDao,
    val entityMapper: GameEntityMapper,
    val pagingConfig: PagingConfig,
    val remoteMediator: RemoteMediator<Int, GameWithPlatforms>,
) : GameRepository {
    private val repository: GameRepository by lazy {
        GameRepositoryImpl(
            gameListDao = gameListDao,
            pagingConfig = pagingConfig,
            gameEntityMapper = entityMapper,
            gameRemoteMediatorProvider = { remoteMediator },
        )
    }

    override fun getGames() = repository.getGames()

    class Builder {
        private val gameListDao: GameListDao = mock()
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

        fun gameEntityMapperMap(
            gameWithPlatforms: GameWithPlatforms,
            mapped: Game,
        ): Builder = apply {
            entityMapper.stub { on { map(gameWithPlatforms) } doReturn mapped }
        }

        fun gameListDaoPagingSource(pagingSource: List<GameWithPlatforms>): Builder = apply {
            val source: PagingSource<Int, GameWithPlatforms> = mock {
                onBlocking { load(any()) } doReturn LoadResult.Page(
                    data = pagingSource,
                    prevKey = null,
                    nextKey = null,
                )
            }
            gameListDao.stub {
                on { pagingSource(GameRepositoryImpl.DEFAULT_LIST_KEY) } doReturn source
            }
        }

        fun build(): GameRepositoryTestDriver = GameRepositoryTestDriver(
            gameListDao = gameListDao,
            entityMapper = entityMapper,
            pagingConfig = pagingConfig,
            remoteMediator = remoteMediator,
        )
    }
}
