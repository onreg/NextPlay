package io.github.onreg.feature.game.details.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.onreg.data.details.api.GameDetailsRepository
import io.github.onreg.data.game.list.api.model.Game
import io.github.onreg.data.game.list.api.model.GamePlatform
import io.github.onreg.data.movies.api.GameMoviesRepository
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.screenshots.api.GameScreenshotsRepository
import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.data.series.api.GameSeriesRepository
import io.github.onreg.feature.game.details.impl.model.GameDetailsEvent
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.ui.mapper.GameDetailsUiMapper
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import io.github.onreg.ui.platform.model.PlatformUI
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class GameDetailsViewModel
    @Inject
    constructor(
        private val detailsRepository: GameDetailsRepository,
        private val screenshotsRepository: GameScreenshotsRepository,
        private val moviesRepository: GameMoviesRepository,
        private val seriesRepository: GameSeriesRepository,
        private val uiMapper: GameDetailsUiMapper,
        private val platformUiMapper: PlatformUiMapper,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow(GameDetailsState())
        private val mutableEvents = MutableSharedFlow<GameDetailsEvent>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
        private val gameIdFlow = MutableStateFlow<Int?>(null)
        private var detailsObservationJob: Job? = null

        val state: StateFlow<GameDetailsState> = mutableState
        val events: Flow<GameDetailsEvent> = mutableEvents

        val screenshots: Flow<PagingData<Screenshot>> = gameIdFlow
            .filterNotNull()
            .flatMapLatest(screenshotsRepository::getScreenshots)
            .cachedIn(viewModelScope)

        val movies: Flow<PagingData<Movie>> = gameIdFlow
            .filterNotNull()
            .flatMapLatest(moviesRepository::getMovies)
            .cachedIn(viewModelScope)

        val series: Flow<PagingData<Game>> = gameIdFlow
            .filterNotNull()
            .flatMapLatest(seriesRepository::getSeries)
            .cachedIn(viewModelScope)

        fun initialize(gameId: Int) {
            if (state.value.gameId == gameId) return

            gameIdFlow.value = gameId
            mutableState.update {
                it.copy(
                    gameId = gameId,
                    isInitialLoading = true,
                    isInitialError = false,
                )
            }

            detailsObservationJob?.cancel()
            detailsObservationJob = viewModelScope.launch {
                detailsRepository
                    .observeGameDetails(gameId)
                    .collect { details ->
                        mutableState.update { current ->
                            current.copy(
                                details = details?.let(uiMapper::map),
                                isInitialLoading = details == null && current.isInitialLoading,
                            )
                        }
                    }
            }

            refresh()
        }

        fun refresh() {
            val id = state.value.gameId ?: return
            viewModelScope.launch {
                runCatching { detailsRepository.refreshGameDetails(id) }
                    .onSuccess {
                        mutableState.update {
                            it.copy(
                                isInitialLoading = false,
                                isInitialError = false,
                            )
                        }
                    }.onFailure {
                        mutableState.update { current ->
                            current.copy(
                                isInitialLoading = false,
                                isInitialError = current.details == null,
                            )
                        }
                    }
            }
        }

        fun onBackClicked() {
            mutableEvents.tryEmit(GameDetailsEvent.GoBack)
        }

        fun onWebsiteClicked() {
            state.value.details?.website?.takeIf { it.isNotBlank() }?.let {
                mutableEvents.tryEmit(GameDetailsEvent.OpenUrl(it))
            }
        }

        fun onImageClicked(url: String) {
            mutableEvents.tryEmit(GameDetailsEvent.OpenImage(url))
        }

        fun onVideoClicked(url: String) {
            mutableEvents.tryEmit(GameDetailsEvent.OpenVideo(url))
        }

        fun onSeriesGameClicked(gameId: Int) {
            mutableEvents.tryEmit(GameDetailsEvent.OpenGameDetails(gameId))
        }

        fun onBookmarkClicked() {
            mutableState.update { it.copy(isBookmarked = !it.isBookmarked) }
        }

        fun mapPlatforms(platforms: Set<GamePlatform>): Set<PlatformUI> =
            platformUiMapper.mapPlatform(platforms)
    }
