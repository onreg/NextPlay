package io.github.onreg.core.db.movies.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.onreg.core.db.movies.entity.MovieEntity

@Dao
public interface MovieDao {
    @androidx.room.Query(
        """
            SELECT * FROM movies
            WHERE gameId = :gameId
            ORDER BY insertionOrder
        """,
    )
    public fun pagingSource(gameId: Int): PagingSource<Int, MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun upsertAll(items: List<MovieEntity>)

    @androidx.room.Query(
        """
            DELETE FROM movies
            WHERE gameId = :gameId
        """,
    )
    public suspend fun clearForGame(gameId: Int)
}
