package io.github.onreg.core.db.game.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.game.entity.GameRemoteKeysEntity

@Dao
public interface GameRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertAll(remoteKeys: List<GameRemoteKeysEntity>)

    @Query("SELECT * FROM game_remote_keys WHERE gameId = :id")
    public suspend fun getRemoteKey(id: Int): GameRemoteKeysEntity?
}
