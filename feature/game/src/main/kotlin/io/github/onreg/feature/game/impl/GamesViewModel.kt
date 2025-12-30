package io.github.onreg.feature.game.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.onreg.core.util.android.lifecycle.ViewModelDelegateImpl
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.feature.game.impl.model.Event
import io.github.onreg.feature.game.impl.model.GamePaneState
import io.github.onreg.feature.game.impl.model.ListEvent
import io.github.onreg.ui.game.presentation.mapper.GameUiMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class GamesViewModel @Inject constructor(
    repository: GameRepository,
    gameUiMapper: GameUiMapper
) : ViewModel() {

    private val gamePaneStateDelegate = ViewModelDelegateImpl<GamePaneState, Event>(GamePaneState)
    private val pagingStateDelegate = ViewModelDelegateImpl<Set<String>, ListEvent>(emptySet())

    val events: Flow<Event> = gamePaneStateDelegate.events
    val state = gamePaneStateDelegate.state()

    val pagingState = with(pagingStateDelegate) {
        viewModelScope.mergedState(
            remote = repository.getGames(),
            merge = { state, pagingData ->
                gameUiMapper.map(pagingData, state)
            },
            initial = PagingData.empty()
        )
    }
    val pagingEvents = pagingStateDelegate.events

    fun onCardClicked(gameId: String) {
        with(gamePaneStateDelegate) { viewModelScope.sendEvent(Event.GoToDetails(gameId)) }
    }

    fun onBookMarkClicked(gameId: String) {
        pagingStateDelegate.reduce { current ->
            if (gameId in current) current - gameId else current + gameId
        }
    }

    fun onRefreshClicked() {
        with(pagingStateDelegate) { viewModelScope.sendEvent(ListEvent.Refresh) }
    }

    fun onRetryClicked() {
        with(pagingStateDelegate) { viewModelScope.sendEvent(ListEvent.Retry) }
    }
}
