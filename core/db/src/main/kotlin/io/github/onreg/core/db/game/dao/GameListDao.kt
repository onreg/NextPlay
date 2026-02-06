package io.github.onreg.core.db.game.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GameListEntity
import io.github.onreg.core.db.game.model.GameWithPlatforms

@Dao
public interface GameListDao {
    @Transaction
    @androidx.room.Query(
        """
            SELECT games.*
            FROM games
            INNER JOIN game_list
                ON games.id = game_list.gameId
            WHERE game_list.listKey = :listKey
            ORDER BY game_list.insertionOrder
        """,
    )
    public fun pagingSource(listKey: String): PagingSource<Int, GameWithPlatforms>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertAll(items: List<GameListEntity>)

    @androidx.room.Query(
        """
            DELETE FROM game_list
            WHERE listKey = :listKey
        """,
    )
    public suspend fun clearList(listKey: String)
}
