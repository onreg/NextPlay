package io.github.onreg.feature.game.details.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.onreg.core.ui.components.header.AppHeaderUi
import io.github.onreg.core.util.android.intent.UrlOpener
import io.github.onreg.core.util.android.lifecycle.ViewModelDelegateImpl
import io.github.onreg.data.details.api.GameDetailsRepository
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.movies.api.GameMoviesRepository
import io.github.onreg.data.screenshots.api.GameScreenshotsRepository
import io.github.onreg.feature.game.details.impl.mapper.GameDetailsStateMapper
import io.github.onreg.feature.game.details.impl.mapper.MovieUiMapper
import io.github.onreg.feature.game.details.impl.mapper.ScreenshotUiMapper
import io.github.onreg.feature.game.details.impl.model.ContentState
import io.github.onreg.feature.game.details.impl.model.GameDetailsEvent
import io.github.onreg.feature.game.details.impl.model.GameDetailsInternalState
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.model.MovieUI
import io.github.onreg.feature.game.details.impl.model.ScreenshotUI
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.game.list.presentation.mapper.GameCardUiMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = GameDetailsViewModel.Factory::class)
public class GameDetailsViewModel
    @AssistedInject
    internal constructor(
        @Assisted private val gameId: Int,
        private val detailsRepository: GameDetailsRepository,
        screenshotsRepository: GameScreenshotsRepository,
        moviesRepository: GameMoviesRepository,
        seriesRepository: GameRepository,
        private val stateMapper: GameDetailsStateMapper,
        private val screenshotUiMapper: ScreenshotUiMapper,
        private val movieUiMapper: MovieUiMapper,
        private val gameCardUiMapper: GameCardUiMapper,
        private val urlOpener: UrlOpener,
    ) : ViewModel() {
        private val delegate = ViewModelDelegateImpl<GameDetailsInternalState, GameDetailsEvent>(
            GameDetailsInternalState(
                isBookmarked = false,
                contentState = ContentState.Loading,
                isDescriptionExpanded = false,
                isReadMoreVisible = false,
            ),
        )

        internal val events: Flow<GameDetailsEvent> = delegate.events
        internal val state: StateFlow<GameDetailsState> = with(delegate) {
            viewModelScope.mergedState(
                remote = detailsRepository
                    .observeGameDetails(gameId)
                    .onStart { refresh() },
                merge = { localState, gameDetails ->
                    stateMapper.map(
                        gameDetails = gameDetails,
                        localState = localState,
                    )
                },
                initial = GameDetailsState.Loading(AppHeaderUi()) as GameDetailsState,
            )
        }

        internal val screenshots: Flow<PagingData<ScreenshotUI>> = screenshotsRepository
            .getScreenshots(gameId)
            .map { pagingData -> pagingData.map(screenshotUiMapper::map) }
            .cachedIn(viewModelScope)

        internal val movies: Flow<PagingData<MovieUI>> = moviesRepository
            .getMovies(gameId)
            .map { pagingData -> pagingData.map(movieUiMapper::map) }
            .cachedIn(viewModelScope)

        internal val series: Flow<PagingData<GameCardUI>> = seriesRepository
            .getSeries(gameId)
            .map { pagingData ->
                pagingData.map { game ->
                    gameCardUiMapper.map(game = game, isBookmarked = false)
                }
            }.cachedIn(viewModelScope)

        internal fun refresh() {
            delegate.reduce { it.copy(contentState = ContentState.Loading) }
            viewModelScope.launch {
                detailsRepository
                    .refreshGameDetails(gameId)
                    .onSuccess {
                        delegate.reduce { it.copy(contentState = ContentState.Idle) }
                    }.onFailure {
                        delegate.reduce { it.copy(contentState = ContentState.Error) }
                    }
            }
        }

        internal fun onBackClicked() {
            with(delegate) { viewModelScope.sendEvent(GameDetailsEvent.GoBack) }
        }

        internal fun onWebsiteClicked() {
            val details = (state.value as? GameDetailsState.Ready)?.details ?: return
            details.website?.takeIf { it.isNotBlank() }?.let { website ->
                urlOpener.open(website)
            }
        }

        internal fun onBannerClicked(url: String) {
            url.takeIf { it.isNotBlank() }?.let { imageUrl ->
                urlOpener.open(imageUrl)
            }
        }

        internal fun onScreenshotClicked(url: String) {
            url.takeIf { it.isNotBlank() }?.let { imageUrl ->
                urlOpener.open(imageUrl)
            }
        }

        internal fun onMovieClicked(url: String) {
            url.takeIf { it.isNotBlank() }?.let { videoUrl ->
                urlOpener.open(videoUrl)
            }
        }

        internal fun onSeriesClicked(gameId: Int) {
            with(delegate) { viewModelScope.sendEvent(GameDetailsEvent.GoGameDetails(gameId)) }
        }

        internal fun onBookmarkClicked() {
            val current = state.value as? GameDetailsState.Ready ?: return
            delegate.reduce {
                it.copy(isBookmarked = !current.details.isBookmarked)
            }
        }

        internal fun onDescriptionOverflowChanged(isOverflowed: Boolean) {
            delegate.reduce {
                it.copy(isReadMoreVisible = isOverflowed)
            }
        }

        internal fun onDescriptionToggleClicked() {
            val current = state.value as? GameDetailsState.Ready ?: return
            delegate.reduce {
                it.copy(isDescriptionExpanded = !current.details.gameDescriptionUi.isExpanded)
            }
        }

        @AssistedFactory
        public interface Factory {
            public fun create(gameId: Int): GameDetailsViewModel
        }
    }
