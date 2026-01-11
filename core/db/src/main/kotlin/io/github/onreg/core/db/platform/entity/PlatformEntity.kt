package io.github.onreg.core.db.platform.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = PlatformEntity.TABLE_NAME)
public data class PlatformEntity(
    @PrimaryKey
    @ColumnInfo(name = ID)
    val id: Int
) {
    internal companion object {
        const val TABLE_NAME: String = "platforms"
        const val ID: String = "id"
    }
}
