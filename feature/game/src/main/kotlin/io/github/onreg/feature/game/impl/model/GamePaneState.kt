package io.github.onreg.feature.game.impl.model

public data object GamePaneState

internal sealed interface Event {
    data class GoToDetails(val gameId: String) : Event
}

internal sealed interface ListEvent {
    data object Retry : ListEvent
    data object Refresh : ListEvent
}