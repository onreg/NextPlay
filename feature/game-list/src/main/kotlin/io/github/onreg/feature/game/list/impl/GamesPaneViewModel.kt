package io.github.onreg.feature.game.list.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.onreg.core.util.flow.deriveState
import io.github.onreg.core.util.flow.reduce
import io.github.onreg.core.util.flow.sendEvent
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.feature.game.list.impl.model.GamePaneState
import io.github.onreg.feature.game.list.impl.model.GamesPaneEvent
import io.github.onreg.ui.game.list.presentation.mapper.GameCardUiMapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
internal class GamesPaneViewModel
    @Inject
    constructor(
        repository: GameRepository,
        gameCardUiMapper: GameCardUiMapper,
    ) : ViewModel() {
        private val screenState = MutableStateFlow(GamePaneState)
        private val screenEvents = Channel<GamesPaneEvent>()
        private val bookmarkedIds = MutableStateFlow<Set<Int>>(emptySet())

        val events: Flow<GamesPaneEvent> = screenEvents.receiveAsFlow()
        val state = screenState.asStateFlow()

        val pagingState = bookmarkedIds.deriveState(
            scope = viewModelScope,
            remote = repository.getGames().cachedIn(viewModelScope),
            merge = { state, pagingData ->
                pagingData.map { game ->
                    gameCardUiMapper.map(
                        game = game,
                        isBookmarked = game.id in state,
                    )
                }
            },
            initial = PagingData.empty(),
        )

        fun onCardClicked(gameId: Int) {
            viewModelScope.sendEvent(screenEvents, GamesPaneEvent.GoToDetails(gameId))
        }

        fun onBookMarkClicked(gameId: Int) {
            bookmarkedIds.reduce { current ->
                if (gameId in current) current - gameId else current + gameId
            }
        }

        fun onRefreshClicked() {
            viewModelScope.sendEvent(screenEvents, GamesPaneEvent.Refresh)
        }

        fun onRetryClicked() {
            viewModelScope.sendEvent(screenEvents, GamesPaneEvent.Retry)
        }
    }
