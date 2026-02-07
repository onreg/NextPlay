package io.github.onreg.core.db.movies.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = GameMovieRemoteKeysEntity.TABLE_NAME,
    primaryKeys = [GameMovieRemoteKeysEntity.GAME_ID, GameMovieRemoteKeysEntity.MOVIE_ID],
    indices = [Index(GameMovieRemoteKeysEntity.GAME_ID), Index(GameMovieRemoteKeysEntity.MOVIE_ID)],
)
public data class GameMovieRemoteKeysEntity(
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = MOVIE_ID)
    val movieId: Int,
    @ColumnInfo(name = PREV_KEY)
    val prevKey: Int?,
    @ColumnInfo(name = NEXT_KEY)
    val nextKey: Int?,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_movie_remote_keys"
        const val GAME_ID: String = "gameId"
        const val MOVIE_ID: String = "movieId"
        const val PREV_KEY: String = "prevKey"
        const val NEXT_KEY: String = "nextKey"
    }
}
