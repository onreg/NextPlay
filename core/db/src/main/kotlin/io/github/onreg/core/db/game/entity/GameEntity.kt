package io.github.onreg.core.db.game.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = GameEntity.TABLE_NAME)
public data class GameEntity(
    @PrimaryKey
    @ColumnInfo(name = ID)
    val id: Int,
    @ColumnInfo(name = TITLE)
    val title: String,
    @ColumnInfo(name = IMAGE_URL)
    val imageUrl: String,
    @ColumnInfo(name = RELEASE_DATE)
    val releaseDate: Instant?,
    @ColumnInfo(name = RATING)
    val rating: Double,
) {
    internal companion object {
        const val TABLE_NAME: String = "games"
        const val ID: String = "id"
        const val TITLE: String = "title"
        const val IMAGE_URL: String = "imageUrl"
        const val RELEASE_DATE: String = "releaseDate"
        const val RATING: String = "rating"
    }
}
