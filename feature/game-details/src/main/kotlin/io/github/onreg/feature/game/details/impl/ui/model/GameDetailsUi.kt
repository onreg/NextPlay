package io.github.onreg.feature.game.details.impl.ui.model

import io.github.onreg.ui.platform.model.PlatformUI

internal data class GameDetailsUi(
    val gameId: Int,
    val title: String,
    val imageUrl: String,
    val releaseDate: String,
    val platforms: Set<PlatformUI>,
    val website: String?,
    val isWebsiteVisible: Boolean,
    val rating: String,
    val description: String,
    val developers: List<String>,
)
