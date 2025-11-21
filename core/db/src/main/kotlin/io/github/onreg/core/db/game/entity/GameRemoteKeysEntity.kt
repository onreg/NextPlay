package io.github.onreg.core.db.game.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_remote_keys",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("gameId")]
)
public data class GameRemoteKeysEntity(
    @PrimaryKey
    val gameId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)
