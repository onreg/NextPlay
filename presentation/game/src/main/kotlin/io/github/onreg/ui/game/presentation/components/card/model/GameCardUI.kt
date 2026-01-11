package io.github.onreg.ui.game.presentation.components.card.model

import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.ui.platform.model.PlatformUI

public data class GameCardUI(
    val id: String,
    val title: String,
    val imageUrl: String,
    val releaseDate: String,
    val platforms: Set<PlatformUI>,
    val rating: ChipUI,
    val isBookmarked: Boolean
)