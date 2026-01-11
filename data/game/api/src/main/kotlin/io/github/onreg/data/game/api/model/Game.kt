package io.github.onreg.data.game.api.model

import java.time.Instant

public data class Game(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val releaseDate: Instant?,
    val rating: Double,
    val platforms: Set<GamePlatform>
)
