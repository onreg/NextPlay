package io.github.onreg.feature.game.impl.model

import androidx.paging.PagingData
import io.github.onreg.core.ui.components.card.GameCardUI

internal sealed interface GamePaneState {
    data object Error : GamePaneState
    data class Ready(val gameCardsUI: PagingData<GameCardUI>) : GamePaneState
}

internal sealed interface Event {
    data class GoToDetails(val gameId: String) : Event
}
