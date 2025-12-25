package io.github.onreg.feature.game.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.feature.game.impl.model.Event
import io.github.onreg.feature.game.impl.model.GamePaneState
import io.github.onreg.ui.game.presentation.mapper.GameUiMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class GamesViewModel @Inject constructor(
    repository: GameRepository,
    gameUiMapper: GameUiMapper
) : ViewModel(), GamesPaneViewModel {

    private val _events = Channel<Event>()
    override val events: Flow<Event> = _events.receiveAsFlow()

    private val _state = MutableStateFlow<GamePaneState>(GamePaneState.Ready)
    override val state: StateFlow<GamePaneState> = _state

    private val _retryEvent = MutableSharedFlow<Any>()
    private val bookMarks = MutableStateFlow(emptySet<String>())
    override val dataState = _retryEvent
        .onStart { emit(Any()) }
        .flatMapLatest {
            combine(repository.getGames(), bookMarks, gameUiMapper::map)
        }
        .catch { _state.value = GamePaneState.Error }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PagingData.empty()
        )

    override fun onCardClicked(gameId: String) {
        viewModelScope.launch {
            _events.send(Event.GoToDetails(gameId))
        }
    }

    override fun onBookMarkClicked(gameId: String) {
        bookMarks.update { current ->
            if (gameId in current) current - gameId else current + gameId
        }
    }

    override fun onRefreshClicked() {
        viewModelScope.launch {
            _events.send(Event.ListEvent.Refresh)
        }
    }

    override fun onRetryClicked() {
        viewModelScope.launch {
            _retryEvent.emit(Any())
        }
    }

    override fun onPageRetryClicked() {
        viewModelScope.launch {
            _events.send(Event.ListEvent.Retry)
        }
    }
}