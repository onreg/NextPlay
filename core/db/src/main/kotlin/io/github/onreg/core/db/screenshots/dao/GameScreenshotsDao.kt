package io.github.onreg.core.db.screenshots.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.screenshots.entity.GameScreenshotCrossRef
import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity

@Dao
public interface GameScreenshotsDao {
    @Query(
        """
            SELECT s.* FROM ${ScreenshotEntity.TABLE_NAME} s
            INNER JOIN ${GameScreenshotCrossRef.TABLE_NAME} c
            ON s.${ScreenshotEntity.ID} = c.${GameScreenshotCrossRef.SCREENSHOT_ID}
            WHERE c.${GameScreenshotCrossRef.GAME_ID} = :gameId
            ORDER BY c.${GameScreenshotCrossRef.POSITION}
        """,
    )
    public fun pagingSource(gameId: Int): PagingSource<Int, ScreenshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertScreenshots(items: List<ScreenshotEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertCrossRefs(items: List<GameScreenshotCrossRef>)

    @Query(
        """
            DELETE FROM ${GameScreenshotCrossRef.TABLE_NAME}
            WHERE ${GameScreenshotCrossRef.GAME_ID} = :gameId
        """,
    )
    public suspend fun clearByGameId(gameId: Int)
}
