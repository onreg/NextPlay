package io.github.onreg.feature.game.details.impl.model

internal sealed interface GameDetailsEvent {
    data object GoBack : GameDetailsEvent

    data class GoToGameDetails(val gameId: Int) : GameDetailsEvent
}
