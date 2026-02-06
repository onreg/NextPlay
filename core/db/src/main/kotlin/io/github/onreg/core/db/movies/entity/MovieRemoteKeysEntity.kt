package io.github.onreg.core.db.movies.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = MovieRemoteKeysEntity.TABLE_NAME,
    primaryKeys = [MovieRemoteKeysEntity.GAME_ID, MovieRemoteKeysEntity.MOVIE_ID],
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = [MovieEntity.GAME_ID, MovieEntity.ID],
            childColumns = [MovieRemoteKeysEntity.GAME_ID, MovieRemoteKeysEntity.MOVIE_ID],
            onDelete = CASCADE,
        ),
    ],
    indices = [
        Index(MovieRemoteKeysEntity.GAME_ID),
        Index(MovieRemoteKeysEntity.MOVIE_ID),
    ],
)
public data class MovieRemoteKeysEntity(
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
        const val TABLE_NAME: String = "movie_remote_keys"
        const val GAME_ID: String = "gameId"
        const val MOVIE_ID: String = "movieId"
        const val PREV_KEY: String = "prevKey"
        const val NEXT_KEY: String = "nextKey"
    }
}
