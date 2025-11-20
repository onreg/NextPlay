package io.github.onreg.core.db.game.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "games")
public data class GameEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val imageUrl: String,
    val releaseDate: Instant?,
    val rating: Double
)
