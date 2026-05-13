package io.github.onreg.core.db.details.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = GameDetailsEntity.TABLE_NAME)
public data class GameDetailsEntity(
    @PrimaryKey
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = TITLE)
    val title: String,
    @ColumnInfo(name = IMAGE_URL)
    val imageUrl: String,
    @ColumnInfo(name = RELEASE_DATE)
    val releaseDate: Instant?,
    @ColumnInfo(name = WEBSITE)
    val website: String?,
    @ColumnInfo(name = RATING)
    val rating: Double,
    @ColumnInfo(name = DESCRIPTION)
    val description: String,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_details"
        const val GAME_ID: String = "gameId"
        const val TITLE: String = "title"
        const val IMAGE_URL: String = "imageUrl"
        const val RELEASE_DATE: String = "releaseDate"
        const val WEBSITE: String = "website"
        const val RATING: String = "rating"
        const val DESCRIPTION: String = "description"
    }
}
