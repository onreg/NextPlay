package io.github.onreg.core.db.screenshots.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = GameScreenshotRemoteKeysEntity.TABLE_NAME,
    primaryKeys = [
        GameScreenshotRemoteKeysEntity.GAME_ID,
        GameScreenshotRemoteKeysEntity.SCREENSHOT_ID,
    ],
    indices = [
        Index(
            GameScreenshotRemoteKeysEntity.GAME_ID,
        ), Index(GameScreenshotRemoteKeysEntity.SCREENSHOT_ID),
    ],
)
public data class GameScreenshotRemoteKeysEntity(
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = SCREENSHOT_ID)
    val screenshotId: Int,
    @ColumnInfo(name = PREV_KEY)
    val prevKey: Int?,
    @ColumnInfo(name = NEXT_KEY)
    val nextKey: Int?,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_screenshot_remote_keys"
        const val GAME_ID: String = "gameId"
        const val SCREENSHOT_ID: String = "screenshotId"
        const val PREV_KEY: String = "prevKey"
        const val NEXT_KEY: String = "nextKey"
    }
}
