package io.github.onreg.core.db.game.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.entity.GameWithPlatformsEntity
import io.github.onreg.core.db.platform.dao.PlatformDao
import io.github.onreg.core.db.platform.entity.PlatformEntity

@Dao
public abstract class GameDao private constructor(private val platformDao: PlatformDao) {

    public constructor(database: NextPlayDatabase) : this(database.platformDao())

    @Transaction
    @Query("SELECT * FROM ${GameEntity.TABLE_NAME} ORDER BY ${GameEntity.INSERTION_ORDER}")
    public abstract fun pagingSource(): PagingSource<Int, GameWithPlatformsEntity>

    @Query("DELETE FROM ${GameEntity.TABLE_NAME}")
    public abstract suspend fun clearGames()

    @Transaction
    public suspend fun insertGamesWithPlatforms(
        games: List<GameEntity>,
        platforms: List<PlatformEntity>,
        crossRefs: List<GamePlatformCrossRef>
    ) {
        platformDao.insertPlatforms(platforms)
        insertGames(games)
        insertGamePlatformCrossRefs(crossRefs)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertGames(games: List<GameEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertGamePlatformCrossRefs(crossRefs: List<GamePlatformCrossRef>)
}
