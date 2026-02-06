package io.github.onreg.core.db.screenshots.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity

@Dao
public interface ScreenshotDao {
    @androidx.room.Query(
        """
            SELECT * FROM screenshots
            WHERE gameId = :gameId
            ORDER BY insertionOrder
        """,
    )
    public fun pagingSource(gameId: Int): PagingSource<Int, ScreenshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun upsertAll(items: List<ScreenshotEntity>)

    @androidx.room.Query(
        """
            DELETE FROM screenshots
            WHERE gameId = :gameId
        """,
    )
    public suspend fun clearForGame(gameId: Int)
}
