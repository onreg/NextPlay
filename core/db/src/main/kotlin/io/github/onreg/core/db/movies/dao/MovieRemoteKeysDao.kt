package io.github.onreg.core.db.movies.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.onreg.core.db.movies.entity.MovieRemoteKeysEntity

@Dao
public interface MovieRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertRemoteKeys(items: List<MovieRemoteKeysEntity>)

    @androidx.room.Query(
        """
            SELECT * FROM movie_remote_keys
            WHERE gameId = :gameId
            AND movieId = :movieId
        """,
    )
    public suspend fun getRemoteKey(
        gameId: Int,
        movieId: Int,
    ): MovieRemoteKeysEntity?

    @androidx.room.Query(
        """
            DELETE FROM movie_remote_keys
            WHERE gameId = :gameId
        """,
    )
    public suspend fun clearForGame(gameId: Int)
}
