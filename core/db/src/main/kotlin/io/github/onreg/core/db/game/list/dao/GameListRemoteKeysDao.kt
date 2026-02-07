package io.github.onreg.core.db.game.list.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.game.list.entity.GameListRemoteKeysEntity

@Dao
public interface GameListRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertRemoteKeys(remoteKeys: List<GameListRemoteKeysEntity>)

    @Query(
        """
            SELECT * FROM ${GameListRemoteKeysEntity.TABLE_NAME}
            WHERE ${GameListRemoteKeysEntity.LIST_KEY} = :listKey
            AND ${GameListRemoteKeysEntity.GAME_ID} = :id
        """,
    )
    public suspend fun getRemoteKey(
        listKey: String,
        id: Int,
    ): GameListRemoteKeysEntity?

    @Query(
        """
            DELETE FROM ${GameListRemoteKeysEntity.TABLE_NAME}
            WHERE ${GameListRemoteKeysEntity.LIST_KEY} = :listKey
        """,
    )
    public suspend fun clearByListKey(listKey: String)
}
