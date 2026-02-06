package io.github.onreg.core.db.details.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = GameDeveloperCrossRef.TABLE_NAME,
    primaryKeys = [GameDeveloperCrossRef.GAME_ID, GameDeveloperCrossRef.DEVELOPER_ID],
    foreignKeys = [
        ForeignKey(
            entity = GameDetailsEntity::class,
            parentColumns = [GameDetailsEntity.GAME_ID],
            childColumns = [GameDeveloperCrossRef.GAME_ID],
            onDelete = CASCADE,
        ),
        ForeignKey(
            entity = DeveloperEntity::class,
            parentColumns = [DeveloperEntity.ID],
            childColumns = [GameDeveloperCrossRef.DEVELOPER_ID],
            onDelete = CASCADE,
        ),
    ],
    indices = [
        Index(GameDeveloperCrossRef.GAME_ID),
        Index(GameDeveloperCrossRef.DEVELOPER_ID),
    ],
)
public data class GameDeveloperCrossRef(
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = DEVELOPER_ID)
    val developerId: Int,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_developer_cross_ref"
        const val GAME_ID: String = "gameId"
        const val DEVELOPER_ID: String = "developerId"
    }
}
