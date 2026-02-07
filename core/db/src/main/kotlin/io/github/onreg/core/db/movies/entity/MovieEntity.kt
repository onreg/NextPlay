package io.github.onreg.core.db.movies.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = MovieEntity.TABLE_NAME)
public data class MovieEntity(
    @PrimaryKey
    @ColumnInfo(name = ID)
    val id: Int,
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
        const val NAME: String = "name"
        const val PREVIEW_URL: String = "previewUrl"
        const val VIDEO_URL: String = "videoUrl"
    }
}
