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
    @ColumnInfo(name = NAME)
    val name: String,
    @ColumnInfo(name = BANNER_IMAGE_URL)
    val bannerImageUrl: String?,
    @ColumnInfo(name = RELEASE_DATE)
    val releaseDate: Instant?,
    @ColumnInfo(name = WEBSITE_URL)
    val websiteUrl: String?,
    @ColumnInfo(name = RATING)
    val rating: Double?,
    @ColumnInfo(name = DESCRIPTION_HTML)
    val descriptionHtml: String?,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_details"
        const val GAME_ID: String = "gameId"
        const val NAME: String = "name"
        const val BANNER_IMAGE_URL: String = "bannerImageUrl"
        const val RELEASE_DATE: String = "releaseDate"
        const val WEBSITE_URL: String = "websiteUrl"
        const val RATING: String = "rating"
        const val DESCRIPTION_HTML: String = "descriptionHtml"
    }
}
