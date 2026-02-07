package io.github.onreg.core.db.screenshots.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.screenshots.entity.GameScreenshotRemoteKeysEntity

@Dao
public interface GameScreenshotRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insert(items: List<GameScreenshotRemoteKeysEntity>)

    @Query(
        """
            SELECT * FROM ${GameScreenshotRemoteKeysEntity.TABLE_NAME}
            WHERE ${GameScreenshotRemoteKeysEntity.GAME_ID} = :gameId
            AND ${GameScreenshotRemoteKeysEntity.SCREENSHOT_ID} = :screenshotId
        """,
    )
    public suspend fun get(
        gameId: Int,
        screenshotId: Int,
    ): GameScreenshotRemoteKeysEntity?

    @Query(
        """
            DELETE FROM ${GameScreenshotRemoteKeysEntity.TABLE_NAME}
            WHERE ${GameScreenshotRemoteKeysEntity.GAME_ID} = :gameId
        """,
    )
    public suspend fun clearByGameId(gameId: Int)
}
