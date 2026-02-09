package io.github.onreg.core.db.series.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.series.entity.SeriesRemoteKeysEntity

@Dao
public interface SeriesRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertRemoteKeys(entities: List<SeriesRemoteKeysEntity>)

    @Query(
        """
            SELECT * FROM ${SeriesRemoteKeysEntity.TABLE_NAME}
            WHERE ${SeriesRemoteKeysEntity.GAME_ID} = :gameId
        """,
    )
    public suspend fun getByGameId(gameId: Int): SeriesRemoteKeysEntity?

    @Query(
        """
            DELETE FROM ${SeriesRemoteKeysEntity.TABLE_NAME}
        """,
    )
    public suspend fun deleteAll()
}
