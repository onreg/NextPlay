package io.github.onreg.core.db.movies.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.onreg.core.db.game.entity.GameEntity

@Entity(
    tableName = MovieEntity.TABLE_NAME,
    indices = [Index(MovieEntity.GAME_ID)],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = [GameEntity.ID],
            childColumns = [MovieEntity.GAME_ID],
            onDelete = CASCADE,
        ),
    ],
)
public data class MovieEntity(
    @PrimaryKey
    @ColumnInfo(name = ID)
    val id: Int,
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = POSITION)
    val position: Long,
    @ColumnInfo(name = NAME)
    val name: String?,
    @ColumnInfo(name = PREVIEW_URL)
    val previewUrl: String?,
    @ColumnInfo(name = VIDEO_URL)
    val videoUrl: String,
) {
    internal companion object {
        const val TABLE_NAME: String = "movies"
        const val ID: String = "id"
        const val GAME_ID: String = "gameId"
        const val POSITION: String = "position"
        const val NAME: String = "name"
        const val PREVIEW_URL: String = "previewUrl"
        const val VIDEO_URL: String = "videoUrl"
    }
}
