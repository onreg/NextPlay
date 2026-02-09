package io.github.onreg.core.db.details.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import io.github.onreg.core.db.platform.entity.PlatformEntity

@Entity(
    tableName = GameDetailsPlatformCrossRef.TABLE_NAME,
    primaryKeys = [
        GameDetailsPlatformCrossRef.GAME_ID,
        GameDetailsPlatformCrossRef.PLATFORM_ID,
    ],
    indices = [
        Index(GameDetailsPlatformCrossRef.GAME_ID),
        Index(GameDetailsPlatformCrossRef.PLATFORM_ID),
    ],
    foreignKeys = [
        ForeignKey(
            entity = GameDetailsEntity::class,
            parentColumns = [GameDetailsEntity.GAME_ID],
            childColumns = [GameDetailsPlatformCrossRef.GAME_ID],
            onDelete = CASCADE,
        ),
        ForeignKey(
            entity = PlatformEntity::class,
            parentColumns = [PlatformEntity.ID],
            childColumns = [GameDetailsPlatformCrossRef.PLATFORM_ID],
            onDelete = CASCADE,
        ),
    ],
)
public data class GameDetailsPlatformCrossRef(
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = PLATFORM_ID)
    val platformId: Int,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_details_platforms"
        const val GAME_ID: String = "gameId"
        const val PLATFORM_ID: String = "platformId"
    }
}
