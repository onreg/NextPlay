package io.github.onreg.core.db.game.list.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.list.entity.GameListEntity
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.dao.PlatformDao

@Dao
public abstract class GameListDao internal constructor(private val platformDao: PlatformDao) {
    public constructor(database: NextPlayDatabase) : this(database.platformDao())

    @Transaction
    @Query(
        """
            SELECT ${GameEntity.TABLE_NAME}.*
            FROM ${GameEntity.TABLE_NAME}
            INNER JOIN ${GameListEntity.TABLE_NAME} l
            ON ${GameEntity.TABLE_NAME}.${GameEntity.ID} = l.${GameListEntity.GAME_ID}
            WHERE l.${GameListEntity.LIST_KEY} = :listKey
            ORDER BY l.${GameListEntity.POSITION}
        """,
    )
    public abstract fun pagingSource(listKey: String): PagingSource<Int, GameWithPlatforms>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract suspend fun insertGameList(items: List<GameListEntity>)

    @Query(
        """
            DELETE FROM ${GameListEntity.TABLE_NAME}
            WHERE ${GameListEntity.LIST_KEY} = :listKey
        """,
    )
    public abstract suspend fun clearGameListByKey(listKey: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertGames(games: List<GameEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertGamePlatformCrossRefs(crossRefs: List<GamePlatformCrossRef>)

    @Transaction
    public open suspend fun insertGamesWithPlatforms(bundle: GameInsertionBundle) {
        platformDao.insertPlatforms(bundle.platforms)
        insertGames(bundle.games)
        insertGamePlatformCrossRefs(bundle.crossRefs)
    }
}
