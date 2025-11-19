package io.github.onreg.core.db.game.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
public data class GameEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val backgroundImage: String,
    val released: String,
    val rating: Double,
    val platforms: List<String>
)
