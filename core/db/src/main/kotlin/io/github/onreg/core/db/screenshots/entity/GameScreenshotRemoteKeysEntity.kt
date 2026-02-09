package io.github.onreg.core.db.screenshots.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import io.github.onreg.core.db.game.entity.GameEntity

@Entity(
    tableName = GameScreenshotRemoteKeysEntity.TABLE_NAME,
    primaryKeys = [GameScreenshotRemoteKeysEntity.GAME_ID],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = [GameEntity.ID],
            childColumns = [GameScreenshotRemoteKeysEntity.GAME_ID],
            onDelete = CASCADE,
        ),
    ],
    indices = [Index(GameScreenshotRemoteKeysEntity.GAME_ID)],
)
public data class GameScreenshotRemoteKeysEntity(
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = PREV_KEY)
    val prevKey: Int?,
    @ColumnInfo(name = NEXT_KEY)
    val nextKey: Int?,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_screenshot_remote_keys"
        const val GAME_ID: String = "gameId"
        const val PREV_KEY: String = "prevKey"
        const val NEXT_KEY: String = "nextKey"
    }
}
