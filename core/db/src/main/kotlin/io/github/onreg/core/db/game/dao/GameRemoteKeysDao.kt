package io.github.onreg.core.db.game.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.game.entity.GameRemoteKeysEntity
import kotlin.jvm.JvmSuppressWildcards

@Dao
@JvmSuppressWildcards
public interface GameRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertRemoteKeys(remoteKeys: List<GameRemoteKeysEntity>): List<Long>

    @Query(
        """
            SELECT * FROM ${GameRemoteKeysEntity.TABLE_NAME}
            WHERE ${GameRemoteKeysEntity.GAME_ID} = :id
        """
    )
    public suspend fun getRemoteKey(id: Int): GameRemoteKeysEntity?
}
