package io.github.onreg.core.db.game.list.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import io.github.onreg.core.db.game.entity.GameEntity

@Entity(
    tableName = GameListEntity.TABLE_NAME,
    primaryKeys = [GameListEntity.LIST_KEY, GameListEntity.GAME_ID],
    indices = [Index(GameListEntity.GAME_ID), Index(GameListEntity.LIST_KEY)],
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
    @ColumnInfo(name = LIST_KEY)
    val listKey: String,
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = POSITION)
    val position: Long,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_list_items"
        const val LIST_KEY: String = "listKey"
        const val GAME_ID: String = "gameId"
        const val POSITION: String = "position"
    }
}
