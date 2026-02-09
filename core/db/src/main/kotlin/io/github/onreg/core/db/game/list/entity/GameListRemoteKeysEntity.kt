package io.github.onreg.core.db.game.list.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = GameListRemoteKeysEntity.TABLE_NAME,
    primaryKeys = [GameListRemoteKeysEntity.GAME_ID],
    foreignKeys = [
        ForeignKey(
            entity = GameListEntity::class,
            parentColumns = [GameListEntity.GAME_ID],
            childColumns = [GameListRemoteKeysEntity.GAME_ID],
            onDelete = CASCADE,
        ),
    ],
    indices = [Index(GameListRemoteKeysEntity.GAME_ID)],
)
public data class GameListRemoteKeysEntity(
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = PREV_KEY)
    val prevKey: Int?,
    @ColumnInfo(name = NEXT_KEY)
    val nextKey: Int?,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_remote_keys"
        const val GAME_ID: String = "gameId"
        const val PREV_KEY: String = "prevKey"
        const val NEXT_KEY: String = "nextKey"
    }
}
