package io.github.onreg.core.db.details.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = DeveloperEntity.TABLE_NAME)
public data class DeveloperEntity(
    @PrimaryKey
    @ColumnInfo(name = ID)
    val id: Int,
    @ColumnInfo(name = NAME)
    val name: String,
) {
    internal companion object {
        const val TABLE_NAME: String = "developers"
        const val ID: String = "id"
        const val NAME: String = "name"
    }
}
