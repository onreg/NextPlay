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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class GamesViewModel @Inject constructor(
    repository: GameRepository,
    gameUiMapper: GameUiMapper
) : ViewModel() {

    private val _events = Channel<Event>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: Flow<Event> = _events.receiveAsFlow()

    private val _state = MutableStateFlow<GamePaneState>(GamePaneState.Ready)
    val state = _state.asStateFlow()

    private val _retryEvent = MutableSharedFlow<Unit>()
    private val bookMarks = MutableStateFlow(emptySet<String>())
    val pagingState = _retryEvent
        .onStart { emit(Unit) }
        .flatMapLatest {
            combine(repository.getGames(), bookMarks, gameUiMapper::map)
                .catch { _state.value = GamePaneState.Error }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PagingData.empty()
        )

    fun onCardClicked(gameId: String) {
        _events.trySend(Event.GoToDetails(gameId))
    }

    fun onBookMarkClicked(gameId: String) {
        bookMarks.update { current ->
            if (gameId in current) current - gameId else current + gameId
        }
    }

    fun onRefreshClicked() {
        _events.trySend(Event.ListEvent.Refresh)
    }

    fun onRetryClicked() {
        _state.update { GamePaneState.Ready }
        _retryEvent.tryEmit(Unit)
    }

    fun onPageRetryClicked() {
        _events.trySend(Event.ListEvent.Retry)
    }
}
