package io.github.onreg.core.db.series.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.onreg.core.db.series.entity.SeriesRemoteKeysEntity

@Dao
public interface SeriesRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertRemoteKeys(items: List<SeriesRemoteKeysEntity>)

    @androidx.room.Query(
        """
            SELECT * FROM series_remote_keys
            WHERE parentGameId = :parentGameId
            AND gameId = :gameId
        """,
    )
    public suspend fun getRemoteKey(
        parentGameId: Int,
        gameId: Int,
    ): SeriesRemoteKeysEntity?

    @androidx.room.Query(
        """
            DELETE FROM series_remote_keys
            WHERE parentGameId = :parentGameId
        """,
    )
    public suspend fun clearForParent(parentGameId: Int)
}
