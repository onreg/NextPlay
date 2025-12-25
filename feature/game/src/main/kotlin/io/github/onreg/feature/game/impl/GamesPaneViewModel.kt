package io.github.onreg.feature.game.impl

import io.github.onreg.feature.game.impl.model.Event
import io.github.onreg.feature.game.impl.model.GamePaneState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal interface GamesPaneViewModel {
    val state: StateFlow<GamePaneState>
    val events: Flow<Event>

    fun onCardClicked(gameId: String)
    fun onBookMarkClicked(gameId: String)
    fun onRetryClicked()
    fun onRefreshClicked()
}
