package io.github.onreg.core.db.platform.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.onreg.core.db.platform.entity.PlatformEntity

@Dao
public interface PlatformDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public suspend fun insertPlatforms(platforms: List<PlatformEntity>)
}
