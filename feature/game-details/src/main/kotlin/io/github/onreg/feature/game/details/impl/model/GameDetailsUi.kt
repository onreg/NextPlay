package io.github.onreg.feature.game.details.impl.model

import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.ui.platform.model.PlatformUI

internal data class GameCompanyUi(
    val name: String,
    val logoUrl: String?,
    val role: String,
)

internal data class GameDetailsUi(
    val title: String,
    val imageUrl: String,
    val ratingChip: ChipUI,
    val releaseDate: String?,
    val platforms: Set<PlatformUI>,
    val website: String?,
    val isWebsiteVisible: Boolean,
    val description: String,
    val companies: List<GameCompanyUi>,
    val isBookmarked: Boolean,
)
