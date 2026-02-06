package io.github.onreg.core.db.screenshots.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = ScreenshotEntity.TABLE_NAME,
    primaryKeys = [ScreenshotEntity.GAME_ID, ScreenshotEntity.ID],
    indices = [Index(ScreenshotEntity.GAME_ID)],
)
public data class ScreenshotEntity(
    @ColumnInfo(name = ID)
    val id: Int,
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = IMAGE_URL)
    val imageUrl: String,
    @ColumnInfo(name = WIDTH)
    val width: Int?,
    @ColumnInfo(name = HEIGHT)
    val height: Int?,
    @ColumnInfo(name = INSERTION_ORDER)
    val insertionOrder: Long,
) {
    internal companion object {
        const val TABLE_NAME: String = "screenshots"
        const val ID: String = "id"
        const val GAME_ID: String = "gameId"
        const val IMAGE_URL: String = "imageUrl"
        const val WIDTH: String = "width"
        const val HEIGHT: String = "height"
        const val INSERTION_ORDER: String = "insertionOrder"
    }
}
