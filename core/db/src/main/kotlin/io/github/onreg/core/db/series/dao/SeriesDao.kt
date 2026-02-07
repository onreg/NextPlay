package io.github.onreg.core.db.series.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.series.entity.SeriesEntity

@Dao
public interface SeriesDao {
    @Transaction
    @Query(
        """
            SELECT ${GameEntity.TABLE_NAME}.*
            FROM ${GameEntity.TABLE_NAME}
            INNER JOIN ${SeriesEntity.TABLE_NAME} s
            ON ${GameEntity.TABLE_NAME}.${GameEntity.ID} = s.${SeriesEntity.GAME_ID}
            WHERE s.${SeriesEntity.PARENT_GAME_ID} = :parentGameId
            ORDER BY s.${SeriesEntity.POSITION}
        """,
    )
    public fun pagingSource(parentGameId: Int): PagingSource<Int, GameWithPlatforms>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertSeries(items: List<SeriesEntity>)

    @Query(
        """
            DELETE FROM ${SeriesEntity.TABLE_NAME}
            WHERE ${SeriesEntity.PARENT_GAME_ID} = :parentGameId
        """,
    )
    public suspend fun clearByParentGameId(parentGameId: Int)
}
