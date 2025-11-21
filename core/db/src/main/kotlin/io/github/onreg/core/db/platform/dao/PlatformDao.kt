package io.github.onreg.core.db.platform.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.onreg.core.db.platform.entity.PlatformEntity

internal interface PlatformDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlatforms(platforms: List<PlatformEntity>)
}