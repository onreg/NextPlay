package io.github.onreg.core.db.screenshots.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import io.github.onreg.core.db.game.entity.GameEntity

@Entity(
    tableName = GameScreenshotCrossRef.TABLE_NAME,
    primaryKeys = [GameScreenshotCrossRef.GAME_ID, GameScreenshotCrossRef.SCREENSHOT_ID],
    indices = [Index(GameScreenshotCrossRef.GAME_ID), Index(GameScreenshotCrossRef.SCREENSHOT_ID)],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = [GameEntity.ID],
            childColumns = [GameScreenshotCrossRef.GAME_ID],
            onDelete = CASCADE,
        ),
        ForeignKey(
            entity = ScreenshotEntity::class,
            parentColumns = [ScreenshotEntity.ID],
            childColumns = [GameScreenshotCrossRef.SCREENSHOT_ID],
            onDelete = CASCADE,
        ),
    ],
)
public data class GameScreenshotCrossRef(
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = SCREENSHOT_ID)
    val screenshotId: Int,
    @ColumnInfo(name = POSITION)
    val position: Long,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_screenshot_items"
        const val GAME_ID: String = "gameId"
        const val SCREENSHOT_ID: String = "screenshotId"
        const val POSITION: String = "position"
    }
}
