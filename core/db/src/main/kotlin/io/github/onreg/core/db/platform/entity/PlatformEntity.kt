package io.github.onreg.core.db.platform.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "platforms")
public data class PlatformEntity(
    @PrimaryKey val id: Int
)
