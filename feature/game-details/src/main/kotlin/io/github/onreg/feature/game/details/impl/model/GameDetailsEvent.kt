package io.github.onreg.feature.game.details.impl.model

internal sealed interface GameDetailsEvent {
    data object GoBack : GameDetailsEvent

    data class OpenUrl(val url: String) : GameDetailsEvent

    data class OpenImage(val url: String) : GameDetailsEvent

    data class OpenVideo(val url: String) : GameDetailsEvent

    data class OpenGameDetails(val gameId: Int) : GameDetailsEvent
}
