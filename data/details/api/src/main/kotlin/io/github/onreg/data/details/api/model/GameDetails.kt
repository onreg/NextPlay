package io.github.onreg.data.details.api.model

import io.github.onreg.data.game.api.model.GamePlatform
import java.time.Instant

public data class GameDetails(
    val gameId: Int,
    val title: String,
    val imageUrl: String,
    val releaseDate: Instant?,
    val platforms: Set<GamePlatform>,
    val website: String?,
    val rating: Double,
    val description: String,
    val companies: List<GameCompany>,
)
