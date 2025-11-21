package io.github.onreg.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.onreg.core.db.common.converter.InstantTypeConverter
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.dao.GameRemoteKeysDao
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.entity.GameRemoteKeysEntity
import io.github.onreg.core.db.platform.dao.PlatformDao
import io.github.onreg.core.db.platform.entity.PlatformEntity

@Database(
    entities = [
        GameEntity::class,
        GameRemoteKeysEntity::class,
        PlatformEntity::class,
        GamePlatformCrossRef::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(InstantTypeConverter::class)
public abstract class NextPlayDatabase : RoomDatabase() {
    public abstract fun gameDao(): GameDao
    public abstract fun gameRemoteKeysDao(): GameRemoteKeysDao
    internal abstract fun platformDao(): PlatformDao
}