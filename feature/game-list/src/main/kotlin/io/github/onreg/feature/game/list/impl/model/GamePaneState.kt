package io.github.onreg.feature.game.list.impl.model

public data object GamePaneState

internal sealed interface GamesPaneEvent {
    data class GoToDetails(val gameId: Int) : GamesPaneEvent
    data object Retry : GamesPaneEvent
    data object Refresh : GamesPaneEvent
}
