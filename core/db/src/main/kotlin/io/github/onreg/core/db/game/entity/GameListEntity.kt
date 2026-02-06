package io.github.onreg.core.db.game.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = GameListEntity.TABLE_NAME,
    primaryKeys = [GameListEntity.LIST_KEY, GameListEntity.GAME_ID],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = [GameEntity.ID],
            childColumns = [GameListEntity.GAME_ID],
            onDelete = CASCADE,
        ),
    ],
    indices = [
        Index(GameListEntity.GAME_ID),
        Index(GameListEntity.LIST_KEY),
    ],
)
public data class GameListEntity(
    @ColumnInfo(name = LIST_KEY)
    val listKey: String,
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = INSERTION_ORDER)
    val insertionOrder: Long,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_list"
        const val LIST_KEY: String = "listKey"
        const val GAME_ID: String = "gameId"
        const val INSERTION_ORDER: String = "insertionOrder"
    }
}
