package io.github.onreg.core.db.company.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.github.onreg.core.db.company.entity.GameCompanyEntity

@Dao
public interface GameCompanyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertCompanies(entities: List<GameCompanyEntity>)
}
