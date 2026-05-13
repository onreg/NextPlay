package io.github.onreg.core.db.movies.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import io.github.onreg.core.db.game.entity.GameEntity

@Entity(
    tableName = GameMovieRemoteKeysEntity.TABLE_NAME,
    primaryKeys = [GameMovieRemoteKeysEntity.GAME_ID],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = [GameEntity.ID],
            childColumns = [GameMovieRemoteKeysEntity.GAME_ID],
            onDelete = CASCADE,
        ),
    ],
    indices = [Index(GameMovieRemoteKeysEntity.GAME_ID)],
)
public data class GameMovieRemoteKeysEntity(
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = PREV_KEY)
    val prevKey: Int?,
    @ColumnInfo(name = NEXT_KEY)
    val nextKey: Int?,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_movie_remote_keys"
        const val GAME_ID: String = "gameId"
        const val PREV_KEY: String = "prevKey"
        const val NEXT_KEY: String = "nextKey"
    }
}
