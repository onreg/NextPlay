package io.github.onreg.core.db.movies.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.movies.entity.GameMovieCrossRef
import io.github.onreg.core.db.movies.entity.MovieEntity

@Dao
public interface GameMoviesDao {
    @Query(
        """
            SELECT m.* FROM ${MovieEntity.TABLE_NAME} m
            INNER JOIN ${GameMovieCrossRef.TABLE_NAME} c
            ON m.${MovieEntity.ID} = c.${GameMovieCrossRef.MOVIE_ID}
            WHERE c.${GameMovieCrossRef.GAME_ID} = :gameId
            ORDER BY c.${GameMovieCrossRef.POSITION}
        """,
    )
    public fun pagingSource(gameId: Int): PagingSource<Int, MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertMovies(items: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertCrossRefs(items: List<GameMovieCrossRef>)

    @Query(
        """
            DELETE FROM ${GameMovieCrossRef.TABLE_NAME}
            WHERE ${GameMovieCrossRef.GAME_ID} = :gameId
        """,
    )
    public suspend fun clearByGameId(gameId: Int)
}
