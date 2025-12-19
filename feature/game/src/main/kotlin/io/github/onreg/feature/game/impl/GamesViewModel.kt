package io.github.onreg.feature.game.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.feature.game.impl.mapper.GameUiMapper
import io.github.onreg.feature.game.impl.model.Event
import io.github.onreg.feature.game.impl.model.GamePaneState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class GamesViewModel @Inject constructor(
    repository: GameRepository,
    gameUiMapper: GameUiMapper
) : ViewModel() {

    val events = Channel<Event>()
    private val bookMarks = MutableStateFlow(emptySet<String>())
    val state: StateFlow<GamePaneState> = combine(
        repository.getGames(),
        bookMarks
    ) { games, marks ->
        gameUiMapper.map(games, marks)
    }
        .map<PagingData<GameCardUI>, GamePaneState>(GamePaneState::Ready)
        .catch { emit(GamePaneState.Error) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GamePaneState.Ready(PagingData.empty())
        )


    fun onCardClicked(gameId: String) {
        viewModelScope.launch {
            events.send(Event.GoToDetails(gameId))
        }
    }

    fun onBookMarkClicked(gameId: String) {
        bookMarks.update { current ->
            if (gameId in current) current - gameId else current + gameId
        }
    }
}