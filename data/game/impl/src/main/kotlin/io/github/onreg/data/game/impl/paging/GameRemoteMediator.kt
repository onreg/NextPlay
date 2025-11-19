package io.github.onreg.data.game.impl.paging

import android.net.Uri
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.dao.GameRemoteKeysDao
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GameRemoteKeysEntity
import io.github.onreg.core.network.rawg.api.GameApi
import io.github.onreg.data.game.api.Game
import io.github.onreg.data.game.impl.mapper.GameDtoMapper
import io.github.onreg.data.game.impl.mapper.GameEntityMapper

@OptIn(ExperimentalPagingApi::class)
internal class GameRemoteMediator(
    private val gameApi: GameApi,
    private val gameDao: GameDao,
    private val remoteKeysDao: GameRemoteKeysDao,
    private val pagingConfig: GamePagingConfig,
    private val dtoMapper: GameDtoMapper,
    private val entityMapper: GameEntityMapper
) : RemoteMediator<Int, GameEntity>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, GameEntity>): MediatorResult {
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
        val entities = games.map(entityMapper::map)
        val nextPage = response.next?.let(::getNextPage)

        if (loadType == LoadType.REFRESH) {
            remoteKeysDao.clearRemoteKeys()
            gameDao.clearGames()
        }

        val keys = entities.map { entity ->
            GameRemoteKeysEntity(
                gameId = entity.id,
                prevKey = if (page == pagingConfig.startingPage) null else page - 1,
                nextKey = nextPage
            )
        }
        remoteKeysDao.insertAll(keys)
        gameDao.insertAll(entities)

        return MediatorResult.Success(endOfPaginationReached = nextPage == null)
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, GameEntity>): GameRemoteKeysEntity? {
        val lastItem = state.pages.lastOrNull()?.data?.lastOrNull() ?: return null
        return remoteKeysDao.getRemoteKey(lastItem.id)
    }

    private fun getNextPage(next: String): Int? {
        val uri = runCatching { Uri.parse(next) }.getOrNull()
        return uri?.getQueryParameter("page")?.toIntOrNull()
    }
}
