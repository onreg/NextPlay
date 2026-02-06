package io.github.onreg.core.db.screenshots.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.onreg.core.db.screenshots.entity.ScreenshotRemoteKeysEntity

@Dao
public interface ScreenshotRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertRemoteKeys(items: List<ScreenshotRemoteKeysEntity>)

    @androidx.room.Query(
        """
            SELECT * FROM screenshot_remote_keys
            WHERE gameId = :gameId
            AND screenshotId = :screenshotId
        """,
    )
    public suspend fun getRemoteKey(
        gameId: Int,
        screenshotId: Int,
    ): ScreenshotRemoteKeysEntity?

    @androidx.room.Query(
        """
            DELETE FROM screenshot_remote_keys
            WHERE gameId = :gameId
        """,
    )
    public suspend fun clearForGame(gameId: Int)
}
