package io.github.onreg.core.db.game.list.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.game.list.entity.GameListRemoteKeysEntity

@Dao
public interface GameListRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertRemoteKeys(entities: List<GameListRemoteKeysEntity>)

    @Query(
        """
            SELECT * FROM ${GameListRemoteKeysEntity.TABLE_NAME}
            WHERE ${GameListRemoteKeysEntity.GAME_ID} = :gameId
        """,
    )
    public suspend fun getByGameId(gameId: Int): GameListRemoteKeysEntity?

}
