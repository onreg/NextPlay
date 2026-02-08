package io.github.onreg.feature.game.details.impl.ui.model

import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.ui.platform.model.PlatformUI

internal enum class GameCompanyRoleUi(val label: String) {
    Developer("Developer"),
    Publisher("Publisher"),
}

internal data class GameCompanyUi(
    val name: String,
    val logoUrl: String?,
    val role: GameCompanyRoleUi,
)

internal data class GameDetailsUi(
    val gameId: Int,
    val title: String,
    val imageUrl: String,
    val ratingChip: ChipUI,
    val releaseDate: String?,
    val platforms: Set<PlatformUI>,
    val website: String?,
    val isWebsiteVisible: Boolean,
    val description: String,
    val companies: List<GameCompanyUi>,
)
