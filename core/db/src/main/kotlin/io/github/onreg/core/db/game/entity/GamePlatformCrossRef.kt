package io.github.onreg.core.db.game.entity

import androidx.room.Entity

@Entity(
    tableName = "game_platforms",
    primaryKeys = ["gameId", "platformId"]
)
public data class GamePlatformCrossRef(
    val gameId: Int,
    val platformId: Int
)