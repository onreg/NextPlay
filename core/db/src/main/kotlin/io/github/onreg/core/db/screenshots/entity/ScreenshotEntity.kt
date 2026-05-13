package io.github.onreg.core.db.screenshots.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.onreg.core.db.game.entity.GameEntity

@Entity(
    tableName = ScreenshotEntity.TABLE_NAME,
    indices = [Index(ScreenshotEntity.GAME_ID)],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = [GameEntity.ID],
            childColumns = [ScreenshotEntity.GAME_ID],
            onDelete = CASCADE,
        ),
    ],
)
public data class ScreenshotEntity(
    @PrimaryKey
    @ColumnInfo(name = ID)
    val id: Int,
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = POSITION)
    val position: Long,
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
        const val GAME_ID: String = "gameId"
        const val POSITION: String = "position"
        const val IMAGE_URL: String = "imageUrl"
        const val WIDTH: String = "width"
        const val HEIGHT: String = "height"
    }
}
