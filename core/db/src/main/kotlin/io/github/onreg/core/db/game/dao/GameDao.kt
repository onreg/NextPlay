package io.github.onreg.core.db.game.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.platform.dao.PlatformDao

@Dao
public abstract class GameDao internal constructor(private val platformDao: PlatformDao) {
    public constructor(database: NextPlayDatabase) : this(database.platformDao())

    @androidx.room.Query("DELETE FROM games")
    public abstract suspend fun clearGames()

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
