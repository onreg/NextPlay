package io.github.onreg.feature.game.impl.model

public data object GamePaneState

internal sealed interface GamesPaneEvent {
    data class GoToDetails(val gameId: String) : GamesPaneEvent
}

internal sealed interface GamesPaneListEvent {
    data object Retry : GamesPaneListEvent

    data object Refresh : GamesPaneListEvent
}
