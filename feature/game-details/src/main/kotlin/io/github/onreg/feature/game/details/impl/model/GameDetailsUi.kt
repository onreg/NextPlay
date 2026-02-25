package io.github.onreg.feature.game.details.impl.model

import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.ui.platform.model.PlatformUI

internal data class GameDetailsUi(
    val image: String,
    val rating: ChipUI,
    val releaseDate: String,
    val platforms: Set<PlatformUI>,
    val website: String?,
    val gameDescriptionUi: GameDescriptionUi,
    val companies: List<GameCompanyUi>,
    val isBookmarked: Boolean,
)

internal data class GameDescriptionUi(
    val description: String,
    val isExpanded: Boolean,
    val descriptionToggleUi: DescriptionToggleUi,
)

internal data class GameCompanyUi(
    val name: String,
    val logoUrl: String?,
    val role: String,
)

internal data class DescriptionToggleUi(
    val text: String,
    val isVisible: Boolean,
)
