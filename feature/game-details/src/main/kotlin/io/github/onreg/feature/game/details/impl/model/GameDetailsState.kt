package io.github.onreg.feature.game.details.impl.model

import io.github.onreg.feature.game.details.impl.ui.model.GameDetailsUi

internal data class GameDetailsState(
    val gameId: Int? = null,
    val isBookmarked: Boolean = false,
    val details: GameDetailsUi? = null,
    val isInitialLoading: Boolean = true,
    val isInitialError: Boolean = false,
)
