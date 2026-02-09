package io.github.onreg.core.db.movies.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.movies.entity.MovieEntity

@Dao
public interface GameMoviesDao {
    @Query(
        """
            SELECT * FROM ${MovieEntity.TABLE_NAME}
            WHERE ${MovieEntity.GAME_ID} = :gameId
            ORDER BY ${MovieEntity.POSITION}
        """,
    )
    public fun pagingSource(gameId: Int): PagingSource<Int, MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertMovies(entities: List<MovieEntity>)

    @Query(
        """
            DELETE FROM ${MovieEntity.TABLE_NAME}
            WHERE ${MovieEntity.GAME_ID} = :gameId
        """,
    )
    public suspend fun deleteByGameId(gameId: Int)
}
