package io.github.onreg.core.db.series.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.series.entity.SeriesGameEntity

@Dao
public interface SeriesDao {
    @Transaction
    @androidx.room.Query(
        """
            SELECT games.*
            FROM games
            INNER JOIN series_games
                ON games.id = series_games.gameId
            WHERE series_games.parentGameId = :parentGameId
            ORDER BY series_games.insertionOrder
        """,
    )
    public fun pagingSource(parentGameId: Int): PagingSource<Int, GameWithPlatforms>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertAll(items: List<SeriesGameEntity>)

    @androidx.room.Query(
        """
            DELETE FROM series_games
            WHERE parentGameId = :parentGameId
        """,
    )
    public suspend fun clearForParent(parentGameId: Int)
}
