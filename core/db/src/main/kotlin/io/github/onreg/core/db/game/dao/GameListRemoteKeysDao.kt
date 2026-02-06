package io.github.onreg.core.db.game.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.onreg.core.db.game.entity.GameListRemoteKeysEntity

@Dao
public interface GameListRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertRemoteKeys(remoteKeys: List<GameListRemoteKeysEntity>)

    @androidx.room.Query(
        """
            SELECT * FROM game_list_remote_keys
            WHERE listKey = :listKey
            AND gameId = :id
        """,
    )
    public suspend fun getRemoteKey(
        listKey: String,
        id: Int,
    ): GameListRemoteKeysEntity?

    @androidx.room.Query(
        """
            DELETE FROM game_list_remote_keys
            WHERE listKey = :listKey
        """,
    )
    public suspend fun clearList(listKey: String)
}
