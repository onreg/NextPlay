package io.github.onreg.core.db.screenshots.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = ScreenshotEntity.TABLE_NAME)
public data class ScreenshotEntity(
    @PrimaryKey
    @ColumnInfo(name = ID)
    val id: Int,
    @ColumnInfo(name = IMAGE_URL)
    val imageUrl: String,
    @ColumnInfo(name = WIDTH)
    val width: Int?,
    @ColumnInfo(name = HEIGHT)
    val height: Int?,
) {
    internal companion object {
        const val TABLE_NAME: String = "screenshots"
        const val ID: String = "id"
        const val IMAGE_URL: String = "imageUrl"
        const val WIDTH: String = "width"
        const val HEIGHT: String = "height"
    }
}
