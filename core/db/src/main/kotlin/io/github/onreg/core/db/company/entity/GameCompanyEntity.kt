package io.github.onreg.core.db.company.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = GameCompanyEntity.TABLE_NAME)
public data class GameCompanyEntity(
    @PrimaryKey
    @ColumnInfo(name = ID)
    val id: String,
    @ColumnInfo(name = NAME)
    val name: String,
    @ColumnInfo(name = LOGO_URL)
    val logoUrl: String?,
    @ColumnInfo(name = ROLE)
    val role: GameCompanyRoleEntity,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_companies"
        const val ID: String = "id"
        const val NAME: String = "name"
        const val LOGO_URL: String = "logoUrl"
        const val ROLE: String = "role"
    }
}
