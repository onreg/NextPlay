package io.github.onreg.core.ui.components.card

import io.github.onreg.core.ui.components.chip.ChipUI

public data class GameCardUI(
    val id: String,
    val title: String,
    val imageUrl: String,
    val releaseDate: String,
    val platforms: Set<PlatformUI>,
    val rating: ChipUI,
    val isBookmarked: Boolean
) {
    public data class PlatformUI(
        val name: String,
        val iconRes: Int
    )
}