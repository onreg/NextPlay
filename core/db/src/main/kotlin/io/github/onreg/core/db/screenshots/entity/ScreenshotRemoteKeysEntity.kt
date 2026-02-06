package io.github.onreg.core.db.screenshots.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = ScreenshotRemoteKeysEntity.TABLE_NAME,
    primaryKeys = [ScreenshotRemoteKeysEntity.GAME_ID, ScreenshotRemoteKeysEntity.SCREENSHOT_ID],
    foreignKeys = [
        ForeignKey(
            entity = ScreenshotEntity::class,
            parentColumns = [ScreenshotEntity.GAME_ID, ScreenshotEntity.ID],
            childColumns = [
                ScreenshotRemoteKeysEntity.GAME_ID,
                ScreenshotRemoteKeysEntity.SCREENSHOT_ID,
            ],
            onDelete = CASCADE,
        ),
    ],
    indices = [
        Index(ScreenshotRemoteKeysEntity.GAME_ID),
        Index(ScreenshotRemoteKeysEntity.SCREENSHOT_ID),
    ],
)
public data class ScreenshotRemoteKeysEntity(
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
        const val TABLE_NAME: String = "screenshot_remote_keys"
        const val GAME_ID: String = "gameId"
        const val SCREENSHOT_ID: String = "screenshotId"
        const val PREV_KEY: String = "prevKey"
        const val NEXT_KEY: String = "nextKey"
    }
}
