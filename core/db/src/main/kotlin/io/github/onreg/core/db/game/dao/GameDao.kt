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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract suspend fun insertGames(entities: List<GameEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertPlatformCrossRefs(entities: List<GamePlatformCrossRef>)

    @Transaction
    public open suspend fun insertGamesWithPlatforms(bundle: GameInsertionBundle) {
        platformDao.insertPlatforms(bundle.platforms)
        insertGames(bundle.games)
        insertPlatformCrossRefs(bundle.crossRefs)
    }
}
