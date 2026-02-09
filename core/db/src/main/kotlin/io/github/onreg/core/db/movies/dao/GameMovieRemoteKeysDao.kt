package io.github.onreg.core.db.movies.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.movies.entity.GameMovieRemoteKeysEntity

@Dao
public interface GameMovieRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertRemoteKeys(entities: List<GameMovieRemoteKeysEntity>)

    @Query(
        """
            SELECT * FROM ${GameMovieRemoteKeysEntity.TABLE_NAME}
            WHERE ${GameMovieRemoteKeysEntity.GAME_ID} = :gameId
        """,
    )
    public suspend fun getByGameId(gameId: Int): GameMovieRemoteKeysEntity?

    @Query(
        """
            DELETE FROM ${GameMovieRemoteKeysEntity.TABLE_NAME}
            WHERE ${GameMovieRemoteKeysEntity.GAME_ID} = :gameId
        """,
    )
    public suspend fun deleteByGameId(gameId: Int)
}
