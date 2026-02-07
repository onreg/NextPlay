package io.github.onreg.core.db.movies.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.movies.entity.GameMovieRemoteKeysEntity

@Dao
public interface GameMovieRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insert(items: List<GameMovieRemoteKeysEntity>)

    @Query(
        """
            SELECT * FROM ${GameMovieRemoteKeysEntity.TABLE_NAME}
            WHERE ${GameMovieRemoteKeysEntity.GAME_ID} = :gameId
            AND ${GameMovieRemoteKeysEntity.MOVIE_ID} = :movieId
        """,
    )
    public suspend fun get(
        gameId: Int,
        movieId: Int,
    ): GameMovieRemoteKeysEntity?

    @Query(
        """
            DELETE FROM ${GameMovieRemoteKeysEntity.TABLE_NAME}
            WHERE ${GameMovieRemoteKeysEntity.GAME_ID} = :gameId
        """,
    )
    public suspend fun clearByGameId(gameId: Int)
}
