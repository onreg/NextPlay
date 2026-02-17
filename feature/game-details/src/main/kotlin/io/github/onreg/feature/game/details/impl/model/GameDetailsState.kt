package io.github.onreg.feature.game.details.impl.model

internal sealed interface GameDetailsState {
    data object Loading : GameDetailsState

    data object Error : GameDetailsState

    data class Ready(val details: GameDetailsUi) : GameDetailsState
}
