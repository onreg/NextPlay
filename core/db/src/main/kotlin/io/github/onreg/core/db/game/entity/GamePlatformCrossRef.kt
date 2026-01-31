package io.github.onreg.core.db.game.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import io.github.onreg.core.db.platform.entity.PlatformEntity

@Entity(
    tableName = GamePlatformCrossRef.TABLE_NAME,
    primaryKeys = [
        GamePlatformCrossRef.GAME_ID,
        GamePlatformCrossRef.PLATFORM_ID,
    ],
    indices = [
        Index(GamePlatformCrossRef.GAME_ID),
        Index(GamePlatformCrossRef.PLATFORM_ID),
    ],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = [GameEntity.ID],
            childColumns = [GamePlatformCrossRef.GAME_ID],
            onDelete = CASCADE,
        ),
        ForeignKey(
            entity = PlatformEntity::class,
            parentColumns = [PlatformEntity.ID],
            childColumns = [GamePlatformCrossRef.PLATFORM_ID],
            onDelete = CASCADE,
        ),
    ],
)
public data class GamePlatformCrossRef(
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = PLATFORM_ID)
    val platformId: Int,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_platforms"
        const val GAME_ID: String = "gameId"
        const val PLATFORM_ID: String = "platformId"
    }
}
