package io.github.onreg.core.db.series.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import io.github.onreg.core.db.game.entity.GameEntity

@Entity(
    tableName = SeriesEntity.TABLE_NAME,
    primaryKeys = [SeriesEntity.GAME_ID],
    indices = [Index(SeriesEntity.GAME_ID)],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = [GameEntity.ID],
            childColumns = [SeriesEntity.GAME_ID],
            onDelete = CASCADE,
        ),
    ],
)
public data class SeriesEntity(
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = POSITION)
    val position: Long,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_series_items"
        const val GAME_ID: String = "gameId"
        const val POSITION: String = "position"
    }
}
