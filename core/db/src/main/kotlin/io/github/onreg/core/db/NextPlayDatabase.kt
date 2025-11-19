package io.github.onreg.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.dao.GameRemoteKeysDao
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GameRemoteKeysEntity

@Database(
    entities = [
        GameEntity::class,
        GameRemoteKeysEntity::class
    ],
    version = 1,
    exportSchema = true
)
public abstract class NextPlayDatabase : RoomDatabase() {
    public abstract fun gameDao(): GameDao
    public abstract fun gameRemoteKeysDao(): GameRemoteKeysDao
}