package io.github.onreg.core.db.series.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import io.github.onreg.core.db.game.entity.GameEntity

@Entity(
    tableName = SeriesGameEntity.TABLE_NAME,
    primaryKeys = [SeriesGameEntity.PARENT_GAME_ID, SeriesGameEntity.GAME_ID],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = [GameEntity.ID],
            childColumns = [SeriesGameEntity.GAME_ID],
            onDelete = CASCADE,
        ),
    ],
    indices = [
        Index(SeriesGameEntity.PARENT_GAME_ID),
        Index(SeriesGameEntity.GAME_ID),
    ],
)
public data class SeriesGameEntity(
    @ColumnInfo(name = PARENT_GAME_ID)
    val parentGameId: Int,
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = INSERTION_ORDER)
    val insertionOrder: Long,
) {
    internal companion object {
        const val TABLE_NAME: String = "series_games"
        const val PARENT_GAME_ID: String = "parentGameId"
        const val GAME_ID: String = "gameId"
        const val INSERTION_ORDER: String = "insertionOrder"
    }
}
