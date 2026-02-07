package io.github.onreg.core.db.details.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface GameDetailsDao {
    @Query(
        """
            SELECT * FROM ${GameDetailsEntity.TABLE_NAME}
            WHERE ${GameDetailsEntity.GAME_ID} = :gameId
        """,
    )
    public fun observe(gameId: Int): Flow<GameDetailsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun upsert(entity: GameDetailsEntity)
}
