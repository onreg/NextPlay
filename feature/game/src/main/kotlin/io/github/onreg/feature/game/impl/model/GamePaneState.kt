package io.github.onreg.feature.game.impl.model

internal sealed interface GamePaneState {
    data object Error : GamePaneState
    data object Ready : GamePaneState
}

internal sealed interface Event {
    data class GoToDetails(val gameId: String) : Event
    sealed interface ListEvent : Event {
        data object Retry : ListEvent
        data object Refresh : ListEvent
    }
}