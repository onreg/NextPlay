package io.github.onreg.feature.game.details.impl.model

internal data class GameDetailsInternalState(
    val isBookmarked: Boolean,
    val contentState: ContentState,
)

internal sealed interface ContentState {
    data object Idle : ContentState

    data object Loading : ContentState

    data object Error : ContentState
}
