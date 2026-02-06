package io.github.onreg.core.db.details.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import io.github.onreg.core.db.details.entity.DeveloperEntity
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.details.entity.GameDeveloperCrossRef
import io.github.onreg.core.db.details.model.GameDetailsWithDevelopers
import kotlinx.coroutines.flow.Flow

@Dao
public abstract class GameDetailsDao {
    @Transaction
    @androidx.room.Query(
        """
            SELECT * FROM game_details
            WHERE gameId = :gameId
        """,
    )
    public abstract fun observeGameDetails(gameId: Int): Flow<GameDetailsWithDevelopers?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract suspend fun upsertDetails(details: GameDetailsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertDevelopers(developers: List<DeveloperEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertCrossRefs(crossRefs: List<GameDeveloperCrossRef>)

    @androidx.room.Query(
        """
            DELETE FROM game_developer_cross_ref
            WHERE gameId = :gameId
        """,
    )
    internal abstract suspend fun clearDevelopersForGame(gameId: Int)

    @Transaction
    public open suspend fun replaceDevelopers(
        gameId: Int,
        developers: List<DeveloperEntity>,
    ) {
        clearDevelopersForGame(gameId)
        if (developers.isEmpty()) {
            return
        }
        insertDevelopers(developers)
        insertCrossRefs(
            developers.map { developer ->
                GameDeveloperCrossRef(
                    gameId = gameId,
                    developerId = developer.id,
                )
            },
        )
    }
}
