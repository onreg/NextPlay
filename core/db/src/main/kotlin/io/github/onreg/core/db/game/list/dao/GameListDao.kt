package io.github.onreg.core.db.game.list.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.list.entity.GameListEntity
import io.github.onreg.core.db.game.model.GameWithPlatforms

@Dao
public interface GameListDao {
    @Transaction
    @Query(
        """
            SELECT ${GameEntity.TABLE_NAME}.*
            FROM ${GameEntity.TABLE_NAME}
            INNER JOIN ${GameListEntity.TABLE_NAME} l
            ON ${GameEntity.TABLE_NAME}.${GameEntity.ID} = l.${GameListEntity.GAME_ID}
            ORDER BY l.${GameListEntity.POSITION}
        """,
    )
    public fun pagingSource(): PagingSource<Int, GameWithPlatforms>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertGameListEntries(entities: List<GameListEntity>)

    @Query(
        """
            DELETE FROM ${GameListEntity.TABLE_NAME}
        """,
    )
    public suspend fun deleteAll()
}
