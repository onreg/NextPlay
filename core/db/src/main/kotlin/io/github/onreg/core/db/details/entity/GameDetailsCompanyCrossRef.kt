package io.github.onreg.core.db.details.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import io.github.onreg.core.db.company.entity.GameCompanyEntity

@Entity(
    tableName = GameDetailsCompanyCrossRef.TABLE_NAME,
    primaryKeys = [
        GameDetailsCompanyCrossRef.GAME_ID,
        GameDetailsCompanyCrossRef.COMPANY_ID,
    ],
    indices = [
        Index(GameDetailsCompanyCrossRef.GAME_ID),
        Index(GameDetailsCompanyCrossRef.COMPANY_ID),
    ],
    foreignKeys = [
        ForeignKey(
            entity = GameDetailsEntity::class,
            parentColumns = [GameDetailsEntity.GAME_ID],
            childColumns = [GameDetailsCompanyCrossRef.GAME_ID],
            onDelete = CASCADE,
        ),
        ForeignKey(
            entity = GameCompanyEntity::class,
            parentColumns = [GameCompanyEntity.ID],
            childColumns = [GameDetailsCompanyCrossRef.COMPANY_ID],
            onDelete = CASCADE,
        ),
    ],
)
public data class GameDetailsCompanyCrossRef(
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = COMPANY_ID)
    val companyId: String,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_details_companies"
        const val GAME_ID: String = "gameId"
        const val COMPANY_ID: String = "companyId"
    }
}
