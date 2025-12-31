package io.github.onreg.data.game.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.TransactionProvider
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.dao.GameRemoteKeysDao
import io.github.onreg.core.db.game.entity.GameRemoteKeysEntity
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.network.rawg.api.GameApi
import io.github.onreg.data.game.impl.mapper.GameDtoMapper
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import java.net.URI

public class GameRemoteMediator(
    private val gameApi: GameApi,
    private val gameDao: GameDao,
    private val remoteKeysDao: GameRemoteKeysDao,
    private val pagingConfig: GamePagingConfig,
    private val dtoMapper: GameDtoMapper,
    private val entityMapper: GameEntityMapper,
    private val transactionProvider: TransactionProvider
) : RemoteMediator<Int, GameWithPlatforms>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, GameWithPlatforms>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> pagingConfig.startingPage
            LoadType.PREPEND -> null
            LoadType.APPEND -> getRemoteKeyForLastItem(state)?.nextKey
        }

        if (page == null) return MediatorResult.Success(endOfPaginationReached = true)

        val response = gameApi.getGames(
            page = page,
            pageSize = pagingConfig.pageSize
        )

        val games = response.results.map(dtoMapper::map)
        val insertionOrderStart =
            (page - pagingConfig.startingPage).toLong() * pagingConfig.pageSize
        val databaseBundle = entityMapper.map(games, insertionOrderStart)
        val nextPage = response.next?.let(::getNextPage)

        val keys = databaseBundle.games.map { entity ->
            GameRemoteKeysEntity(
                gameId = entity.id,
                prevKey = if (page == pagingConfig.startingPage) null else page - 1,
                nextKey = nextPage
            )
        }
        transactionProvider.run {
            if (loadType == LoadType.REFRESH) {
                gameDao.clearGames()
            }
            gameDao.insertGamesWithPlatforms(databaseBundle)
            remoteKeysDao.insertRemoteKeys(keys)
        }

        return MediatorResult.Success(endOfPaginationReached = nextPage == null)
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, GameWithPlatforms>): GameRemoteKeysEntity? {
        val lastItem = state.pages.lastOrNull()?.data?.lastOrNull() ?: return null
        return remoteKeysDao.getRemoteKey(lastItem.game.id)
    }

    private fun getNextPage(next: String): Int? {
        val query = runCatching { URI(next).query }.getOrNull() ?: return null
        return query.split('&')
            .mapNotNull {
                val parts = it.split('=', limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .firstOrNull { it.first == "page" }
            ?.second
            ?.toIntOrNull()
    }
}
