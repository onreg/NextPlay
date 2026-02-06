package io.github.onreg.feature.game.details.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.onreg.core.util.android.lifecycle.ViewModelDelegateImpl
import io.github.onreg.data.details.api.GameDetailsRepository
import io.github.onreg.data.details.api.RefreshResult
import io.github.onreg.data.movies.api.GameMoviesRepository
import io.github.onreg.data.screenshots.api.GameScreenshotsRepository
import io.github.onreg.data.series.api.GameSeriesRepository
import io.github.onreg.feature.game.details.impl.model.ErrorUi
import io.github.onreg.feature.game.details.impl.model.GameDetailsEvent
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.model.initialGameDetailsState
import io.github.onreg.ui.details.presentation.mapper.GameDetailsUiMapper
import io.github.onreg.ui.details.presentation.model.MovieUi
import io.github.onreg.ui.details.presentation.model.ScreenshotUi
import io.github.onreg.ui.game.presentation.mapper.GameUiMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class GameDetailsViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val gameDetailsRepository: GameDetailsRepository,
        private val screenshotsRepository: GameScreenshotsRepository,
        private val moviesRepository: GameMoviesRepository,
        private val seriesRepository: GameSeriesRepository,
        private val gameDetailsUiMapper: GameDetailsUiMapper,
        private val gameUiMapper: GameUiMapper,
    ) : ViewModel() {
        private val gameId: Int = savedStateHandle.get<String>(GAME_ID_KEY)?.toIntOrNull() ?: 0
        private val stateDelegate =
            ViewModelDelegateImpl<GameDetailsState, GameDetailsEvent>(initialGameDetailsState())

        val events: Flow<GameDetailsEvent> = stateDelegate.events
        val state = stateDelegate.state()

        init {
            val screenshots = screenshotsRepository
                .getScreenshots(gameId)
                .cachedIn(viewModelScope)
                .map { pagingData ->
                    pagingData.map { item ->
                        ScreenshotUi(
                            id = item.id.toString(),
                            imageUrl = item.imageUrl,
                        )
                    }
                }
            val movies = moviesRepository
                .getMovies(gameId)
                .cachedIn(viewModelScope)
                .map { pagingData ->
                    pagingData.map { item ->
                        MovieUi(
                            id = item.id.toString(),
                            name = item.name,
                            previewUrl = item.previewUrl,
                            videoUrl = item.videoUrl,
                        )
                    }
                }
            val series = with(stateDelegate) {
                viewModelScope.mergedState(
                    remote = seriesRepository.getSeries(gameId).cachedIn(viewModelScope),
                    merge = { currentState, pagingData ->
                        gameUiMapper.map(pagingData, currentState.bookmarks)
                    },
                    initial = PagingData.empty(),
                )
            }

            stateDelegate.reduce { current ->
                current.copy(
                    screenshots = screenshots,
                    movies = movies,
                    series = series,
                )
            }

            viewModelScope.launch {
                gameDetailsRepository.observeGameDetails(gameId).collect { details ->
                    val ui = details?.let(gameDetailsUiMapper::map)
                    stateDelegate.reduce { current ->
                        current.copy(
                            detailsUi = ui,
                            isInitialLoading = current.isInitialLoading && ui == null,
                            initialError = if (ui != null) null else current.initialError,
                        )
                    }
                }
            }

            refreshDetails()
        }

        fun onBackClicked() {
            with(stateDelegate) { viewModelScope.sendEvent(GameDetailsEvent.GoBack) }
        }

        fun onWebsiteClicked(url: String) {
            with(stateDelegate) { viewModelScope.sendEvent(GameDetailsEvent.OpenUrl(url)) }
        }

        fun onScreenshotClicked(url: String) {
            with(stateDelegate) { viewModelScope.sendEvent(GameDetailsEvent.OpenImage(url)) }
        }

        fun onMovieClicked(url: String) {
            with(stateDelegate) { viewModelScope.sendEvent(GameDetailsEvent.OpenVideo(url)) }
        }

        fun onSeriesGameClicked(gameId: Int) {
            with(stateDelegate) {
                viewModelScope.sendEvent(GameDetailsEvent.OpenGameDetails(gameId))
            }
        }

        fun onToggleDescription() {
            stateDelegate.reduce { current ->
                current.copy(isDescriptionExpanded = !current.isDescriptionExpanded)
            }
        }

        fun onBookmarkClicked(gameId: String) {
            stateDelegate.reduce { current ->
                val bookmarks = if (gameId in current.bookmarks) {
                    current.bookmarks - gameId
                } else {
                    current.bookmarks + gameId
                }
                current.copy(bookmarks = bookmarks)
            }
        }

        fun onRetryClicked() {
            refreshDetails()
        }

        private fun refreshDetails() {
            if (gameId == 0) {
                stateDelegate.reduce { current ->
                    current.copy(
                        isInitialLoading = false,
                        initialError = ErrorUi("Invalid game id"),
                    )
                }
                return
            }
            stateDelegate.reduce { current ->
                current.copy(
                    isInitialLoading = current.detailsUi == null,
                    initialError = null,
                )
            }
            viewModelScope.launch {
                when (val result = gameDetailsRepository.refreshGameDetails(gameId)) {
                    is RefreshResult.Success -> {
                        stateDelegate.reduce { current ->
                            current.copy(
                                isInitialLoading = false,
                                initialError = null,
                            )
                        }
                    }

                    is RefreshResult.Failure -> {
                        stateDelegate.reduce { current ->
                            if (current.detailsUi == null) {
                                current.copy(
                                    isInitialLoading = false,
                                    initialError = ErrorUi(result.throwable.message.orEmpty()),
                                )
                            } else {
                                current.copy(isInitialLoading = false)
                            }
                        }
                    }
                }
            }
        }

        internal companion object {
            const val GAME_ID_KEY: String = "gameId"
        }
    }
