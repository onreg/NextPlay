package io.github.onreg.feature.game.details.impl.model

import androidx.paging.PagingData
import io.github.onreg.ui.details.presentation.model.GameDetailsUi
import io.github.onreg.ui.details.presentation.model.MovieUi
import io.github.onreg.ui.details.presentation.model.ScreenshotUi
import io.github.onreg.ui.game.presentation.components.card.model.GameCardUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal data class GameDetailsState(
    val detailsUi: GameDetailsUi?,
    val isInitialLoading: Boolean,
    val initialError: ErrorUi?,
    val isDescriptionExpanded: Boolean,
    val bookmarks: Set<String>,
    val screenshots: Flow<PagingData<ScreenshotUi>>,
    val movies: Flow<PagingData<MovieUi>>,
    val series: Flow<PagingData<GameCardUI>>,
)

internal data class ErrorUi(val message: String)

internal fun initialGameDetailsState(): GameDetailsState = GameDetailsState(
    detailsUi = null,
    isInitialLoading = true,
    initialError = null,
    isDescriptionExpanded = false,
    bookmarks = emptySet(),
    screenshots = flowOf(PagingData.empty()),
    movies = flowOf(PagingData.empty()),
    series = flowOf(PagingData.empty()),
)
