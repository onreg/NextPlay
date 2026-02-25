package io.github.onreg.feature.game.list.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.onreg.core.util.android.lifecycle.ViewModelDelegateImpl
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.feature.game.list.impl.model.GamePaneState
import io.github.onreg.feature.game.list.impl.model.GamesPaneEvent
import io.github.onreg.feature.game.list.impl.model.GamesPaneListEvent
import io.github.onreg.ui.game.list.presentation.mapper.GameCardUiMapper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
internal class GamesPaneViewModel
    @Inject
    constructor(
        repository: GameRepository,
        gameCardUiMapper: GameCardUiMapper,
    ) : ViewModel() {
        private val gamePaneStateDelegate =
            ViewModelDelegateImpl<GamePaneState, GamesPaneEvent>(GamePaneState)
        private val pagingStateDelegate =
            ViewModelDelegateImpl<Set<Int>, GamesPaneListEvent>(emptySet())

        val events: Flow<GamesPaneEvent> = gamePaneStateDelegate.events
        val state = gamePaneStateDelegate.state()

        val pagingState = with(pagingStateDelegate) {
            viewModelScope.mergedState(
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
        }
        val pagingEvents = pagingStateDelegate.events

        fun onCardClicked(gameId: Int) {
            with(
                gamePaneStateDelegate,
            ) { viewModelScope.sendEvent(GamesPaneEvent.GoToDetails(gameId)) }
        }

        fun onBookMarkClicked(gameId: Int) {
            pagingStateDelegate.reduce { current ->
                if (gameId in current) current - gameId else current + gameId
            }
        }

        fun onRefreshClicked() {
            with(pagingStateDelegate) { viewModelScope.sendEvent(GamesPaneListEvent.Refresh) }
        }

        fun onRetryClicked() {
            with(pagingStateDelegate) { viewModelScope.sendEvent(GamesPaneListEvent.Retry) }
        }
    }
