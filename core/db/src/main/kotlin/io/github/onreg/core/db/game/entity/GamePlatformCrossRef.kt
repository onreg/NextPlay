package io.github.onreg.core.db.game.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import io.github.onreg.core.db.platform.entity.PlatformEntity

@Entity(
    tableName = "game_platforms",
    primaryKeys = ["gameId", "platformId"],
    indices = [
        Index("gameId"),
        Index("platformId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = PlatformEntity::class,
            parentColumns = ["id"],
            childColumns = ["platformId"],
            onDelete = CASCADE
        )
    ]
)
public data class GamePlatformCrossRef(
    val gameId: Int,
    val platformId: Int
)