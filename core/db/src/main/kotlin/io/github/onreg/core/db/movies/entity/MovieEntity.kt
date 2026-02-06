package io.github.onreg.core.db.movies.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = MovieEntity.TABLE_NAME,
    primaryKeys = [MovieEntity.GAME_ID, MovieEntity.ID],
    indices = [Index(MovieEntity.GAME_ID)],
)
public data class MovieEntity(
    @ColumnInfo(name = ID)
    val id: Int,
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = NAME)
    val name: String?,
    @ColumnInfo(name = PREVIEW_URL)
    val previewUrl: String?,
    @ColumnInfo(name = BEST_VIDEO_URL)
    val bestVideoUrl: String,
    @ColumnInfo(name = INSERTION_ORDER)
    val insertionOrder: Long,
) {
    internal companion object {
        const val TABLE_NAME: String = "movies"
        const val ID: String = "id"
        const val GAME_ID: String = "gameId"
        const val NAME: String = "name"
        const val PREVIEW_URL: String = "previewUrl"
        const val BEST_VIDEO_URL: String = "bestVideoUrl"
        const val INSERTION_ORDER: String = "insertionOrder"
    }
}
