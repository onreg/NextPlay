package io.github.onreg.core.db.movies.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import io.github.onreg.core.db.game.entity.GameEntity

@Entity(
    tableName = GameMovieCrossRef.TABLE_NAME,
    primaryKeys = [GameMovieCrossRef.GAME_ID, GameMovieCrossRef.MOVIE_ID],
    indices = [Index(GameMovieCrossRef.GAME_ID), Index(GameMovieCrossRef.MOVIE_ID)],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = [GameEntity.ID],
            childColumns = [GameMovieCrossRef.GAME_ID],
            onDelete = CASCADE,
        ),
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = [MovieEntity.ID],
            childColumns = [GameMovieCrossRef.MOVIE_ID],
            onDelete = CASCADE,
        ),
    ],
)
public data class GameMovieCrossRef(
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = MOVIE_ID)
    val movieId: Int,
    @ColumnInfo(name = POSITION)
    val position: Long,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_movie_items"
        const val GAME_ID: String = "gameId"
        const val MOVIE_ID: String = "movieId"
        const val POSITION: String = "position"
    }
}
