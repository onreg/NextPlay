package io.github.onreg.core.db.game.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = GameListRemoteKeysEntity.TABLE_NAME,
    primaryKeys = [GameListRemoteKeysEntity.LIST_KEY, GameListRemoteKeysEntity.GAME_ID],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = [GameEntity.ID],
            childColumns = [GameListRemoteKeysEntity.GAME_ID],
            onDelete = CASCADE,
        ),
    ],
    indices = [
        Index(GameListRemoteKeysEntity.GAME_ID),
        Index(GameListRemoteKeysEntity.LIST_KEY),
    ],
)
public data class GameListRemoteKeysEntity(
    @ColumnInfo(name = LIST_KEY)
    val listKey: String,
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = PREV_KEY)
    val prevKey: Int?,
    @ColumnInfo(name = NEXT_KEY)
    val nextKey: Int?,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_list_remote_keys"
        const val LIST_KEY: String = "listKey"
        const val GAME_ID: String = "gameId"
        const val PREV_KEY: String = "prevKey"
        const val NEXT_KEY: String = "nextKey"
    }
}
