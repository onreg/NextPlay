package io.github.onreg.core.db.series.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.series.entity.SeriesRemoteKeysEntity

@Dao
public interface SeriesRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insert(items: List<SeriesRemoteKeysEntity>)

    @Query(
        """
            SELECT * FROM ${SeriesRemoteKeysEntity.TABLE_NAME}
            WHERE ${SeriesRemoteKeysEntity.PARENT_GAME_ID} = :parentGameId
            AND ${SeriesRemoteKeysEntity.GAME_ID} = :gameId
        """,
    )
    public suspend fun get(
        parentGameId: Int,
        gameId: Int,
    ): SeriesRemoteKeysEntity?

    @Query(
        """
            DELETE FROM ${SeriesRemoteKeysEntity.TABLE_NAME}
            WHERE ${SeriesRemoteKeysEntity.PARENT_GAME_ID} = :parentGameId
        """,
    )
    public suspend fun clearByParentGameId(parentGameId: Int)
}
