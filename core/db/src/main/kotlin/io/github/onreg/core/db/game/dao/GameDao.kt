package io.github.onreg.core.db.game.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.game.entity.GameEntity

@Dao
public interface GameDao {
    @Query("SELECT * FROM games")
    public fun pagingSource(): PagingSource<Int, GameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertAll(games: List<GameEntity>)

    @Query("DELETE FROM games")
    public suspend fun clearGames()
}
