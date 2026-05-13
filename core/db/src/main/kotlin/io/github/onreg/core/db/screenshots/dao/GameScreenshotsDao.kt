package io.github.onreg.core.db.screenshots.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity

@Dao
public interface GameScreenshotsDao {
    @Query(
        """
            SELECT * FROM ${ScreenshotEntity.TABLE_NAME}
            WHERE ${ScreenshotEntity.GAME_ID} = :gameId
            ORDER BY ${ScreenshotEntity.POSITION}
        """,
    )
    public fun pagingSource(gameId: Int): PagingSource<Int, ScreenshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertScreenshots(entities: List<ScreenshotEntity>)

    @Query(
        """
            DELETE FROM ${ScreenshotEntity.TABLE_NAME}
            WHERE ${ScreenshotEntity.GAME_ID} = :gameId
        """,
    )
    public suspend fun deleteByGameId(gameId: Int)
}
