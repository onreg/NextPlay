package io.github.onreg.core.db.details.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.company.dao.GameCompanyDao
import io.github.onreg.core.db.details.entity.GameDetailsCompanyCrossRef
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.details.entity.GameDetailsPlatformCrossRef
import io.github.onreg.core.db.details.model.GameDetailsInsertionBundle
import io.github.onreg.core.db.details.model.GameDetailsWithPlatformsAndCompanies
import io.github.onreg.core.db.platform.dao.PlatformDao
import kotlinx.coroutines.flow.Flow

@Dao
public abstract class GameDetailsDao internal constructor(
    private val platformDao: PlatformDao,
    private val gameCompanyDao: GameCompanyDao,
) {
    public constructor(database: NextPlayDatabase) : this(
        platformDao = database.platformDao(),
        gameCompanyDao = database.gameCompanyDao(),
    )

    @Transaction
    @Query(
        """
            SELECT * FROM ${GameDetailsEntity.TABLE_NAME}
            WHERE ${GameDetailsEntity.GAME_ID} = :id
        """,
    )
    public abstract fun observeGame(id: Int): Flow<GameDetailsWithPlatformsAndCompanies?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertDetails(entity: GameDetailsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertPlatformCrossRefs(
        entities: List<GameDetailsPlatformCrossRef>,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertCompanyCrossRefs(
        entities: List<GameDetailsCompanyCrossRef>,
    )

    @Transaction
    public open suspend fun insertGameDetails(bundle: GameDetailsInsertionBundle) {
        insertDetails(bundle.details)
        platformDao.insertPlatforms(bundle.platforms)
        insertPlatformCrossRefs(bundle.platformCrossRefs)
        gameCompanyDao.insertCompanies(bundle.companies)
        insertCompanyCrossRefs(bundle.companyCrossRefs)
    }
}
