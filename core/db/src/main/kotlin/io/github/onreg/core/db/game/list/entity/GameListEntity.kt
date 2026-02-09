package io.github.onreg.core.db.game.list.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import io.github.onreg.core.db.game.entity.GameEntity

@Entity(
    tableName = GameListEntity.TABLE_NAME,
    primaryKeys = [GameListEntity.GAME_ID],
    indices = [Index(GameListEntity.GAME_ID)],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = [GameEntity.ID],
            childColumns = [GameListEntity.GAME_ID],
            onDelete = CASCADE,
        ),
    ],
)
public data class GameListEntity(
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = POSITION)
    val position: Long,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_list_items"
        const val GAME_ID: String = "gameId"
        const val POSITION: String = "position"
    }
}
