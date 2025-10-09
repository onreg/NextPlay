package io.github.onreg.core.ui.components.card

public data class GameCardUI(
    val id: String,
    val title: String,
    val imageUrl: String,
    val releaseDate: String,
    val genres: List<String>,
    val platforms: List<Platform>,
    val rating: String,
    val isBookmarked: Boolean
)

public data class Platform(
    val name: String,
    val iconRes: Int
)
