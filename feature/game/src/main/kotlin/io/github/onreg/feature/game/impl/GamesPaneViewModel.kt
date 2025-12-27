package io.github.onreg.feature.game.impl

import androidx.paging.PagingData
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.feature.game.impl.model.Event
import io.github.onreg.feature.game.impl.model.GamePaneState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal interface GamesPaneViewModel {
    val state: StateFlow<GamePaneState>
    val pagingState: StateFlow<PagingData<GameCardUI>>
    val events: Flow<Event>

    fun onCardClicked(gameId: String)
    fun onBookMarkClicked(gameId: String)
    fun onRetryClicked()
    fun onRefreshClicked()
    fun onPageRetryClicked()
}
