package io.github.onreg.core.db.platform.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.onreg.core.db.platform.entity.PlatformEntity

public interface PlatformDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public suspend fun insertPlatforms(platforms: List<PlatformEntity>)

    @Query("DELETE FROM game_platforms")
    public suspend fun clearGamePlatformRelations()
}