package io.github.onreg.feature.game.details.impl.pane

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.onreg.core.ui.components.content.error.ContentError
import io.github.onreg.core.ui.components.content.error.ContentErrorUI
import io.github.onreg.core.ui.components.header.AppHeader
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.runtime.flow.collectWithLifecycle
import io.github.onreg.core.ui.runtime.paging.PagedListState
import io.github.onreg.core.ui.runtime.paging.resolveState
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.feature.game.details.impl.GameDetailsViewModel
import io.github.onreg.feature.game.details.impl.model.GameDetailsEvent
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.model.GameDetailsUi
import io.github.onreg.feature.game.details.impl.model.MovieUI
import io.github.onreg.feature.game.details.impl.model.ScreenshotUI
import io.github.onreg.feature.game.details.impl.pane.component.BannerComponent
import io.github.onreg.feature.game.details.impl.pane.component.CompanyComponent
import io.github.onreg.feature.game.details.impl.pane.component.DescriptionComponent
import io.github.onreg.feature.game.details.impl.pane.component.DetailsComponent
import io.github.onreg.feature.game.details.impl.pane.component.LoadingComponent
import io.github.onreg.feature.game.details.impl.pane.component.MoviesComponent
import io.github.onreg.feature.game.details.impl.pane.component.ScreenshotsComponent
import io.github.onreg.feature.game.details.impl.pane.component.SeriesComponent
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestTags
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import kotlinx.coroutines.flow.Flow
import io.github.onreg.core.ui.R as CoreUiR

@Composable
public fun GameDetailsPane(
    modifier: Modifier = Modifier,
    isLargeScreen: Boolean = false,
    gameId: Int,
    goBack: () -> Unit,
    openGameDetails: (Int) -> Unit,
) {
    val viewModel = hiltViewModel<GameDetailsViewModel, GameDetailsViewModel.Factory>(
        creationCallback = { factory ->
            factory.create(gameId)
        },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    GameDetailsPaneScreen(
        modifier = modifier.fillMaxSize(),
        isLargeScreen = isLargeScreen,
        gameDetailsState = state,
        onBackClicked = viewModel::onBackClicked,
        screenshots = viewModel.screenshots,
        movies = viewModel.movies,
        series = viewModel.series,
        onRetry = viewModel::refresh,
        onWebsiteClicked = viewModel::onWebsiteClicked,
        onBookmarkClicked = viewModel::onBookmarkClicked,
        onDescriptionOverflowChanged = viewModel::onDescriptionOverflowChanged,
        onDescriptionToggleClicked = viewModel::onDescriptionToggleClicked,
        onScreenshotClicked = viewModel::onScreenshotClicked,
        onMovieClicked = viewModel::onMovieClicked,
        onSeriesClicked = viewModel::onSeriesClicked,
    )

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            GameDetailsEvent.GoBack -> goBack()
            is GameDetailsEvent.GoGameDetails -> openGameDetails(event.gameId)
        }
    }
}

@Composable
@Suppress("LongMethod")
internal fun GameDetailsPaneScreen(
    modifier: Modifier = Modifier,
    isLargeScreen: Boolean = false,
    gameDetailsState: GameDetailsState,
    screenshots: Flow<PagingData<ScreenshotUI>>,
    movies: Flow<PagingData<MovieUI>>,
    series: Flow<PagingData<GameCardUI>>,
    onBackClicked: () -> Unit = {},
    onRetry: () -> Unit = {},
    onWebsiteClicked: () -> Unit = {},
    onBookmarkClicked: () -> Unit = {},
    onDescriptionOverflowChanged: (Boolean) -> Unit = {},
    onDescriptionToggleClicked: () -> Unit = {},
    onScreenshotClicked: (String) -> Unit = {},
    onMovieClicked: (String) -> Unit = {},
    onSeriesClicked: (Int) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppHeader(
                appHeaderUI = gameDetailsState.headerUi,
                onNavigationClicked = onBackClicked,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            when (gameDetailsState) {
                is GameDetailsState.Error -> {
                    ErrorContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(GameDetailsTestTags.ERROR),
                        onRetry = onRetry,
                    )
                }

                is GameDetailsState.Loading -> {
                    LoadingComponent(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(GameDetailsTestTags.LOADING),
                    )
                }

                is GameDetailsState.Ready -> {
                    val moviesItems = movies.collectAsLazyPagingItems()
                    val screenshotsItems = screenshots.collectAsLazyPagingItems()
                    val seriesItems = series.collectAsLazyPagingItems()

                    val screenshotsState = screenshotsItems.resolveState()
                    val moviesState = moviesItems.resolveState()
                    val seriesState = seriesItems.resolveState()

                    GameDetailsContent(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(bottom = Spacing.lg)
                            .testTag(GameDetailsTestTags.CONTENT),
                        isLargeScreen = isLargeScreen,
                        details = gameDetailsState.details,
                        screenshots = screenshotsItems,
                        screenshotsState = screenshotsState,
                        movies = moviesItems,
                        moviesState = moviesState,
                        series = seriesItems,
                        seriesState = seriesState,
                        onWebsiteClicked = onWebsiteClicked,
                        onBookmarkClicked = onBookmarkClicked,
                        onDescriptionOverflowChanged = onDescriptionOverflowChanged,
                        onDescriptionToggleClicked = onDescriptionToggleClicked,
                        onScreenshotClicked = onScreenshotClicked,
                        onMovieClicked = onMovieClicked,
                        onSeriesClicked = onSeriesClicked,
                    )
                }
            }
        }
    }
}

@Composable
internal fun ErrorContent(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        ContentError(
            contentErrorUI = ContentErrorUI(
                iconRes = CoreUiR.drawable.ic_controller_off_24,
                titleResId = CoreUiR.string.error_message,
                descriptionResId = CoreUiR.string.error_network_message,
                actionLabelResId = CoreUiR.string.error_message,
            ),
            onActionClick = onRetry,
        )
    }
}

@Composable
@Suppress("LongMethod")
private fun GameDetailsContent(
    modifier: Modifier = Modifier,
    isLargeScreen: Boolean,
    details: GameDetailsUi,
    screenshots: LazyPagingItems<ScreenshotUI>,
    screenshotsState: PagedListState<ScreenshotUI>,
    movies: LazyPagingItems<MovieUI>,
    moviesState: PagedListState<MovieUI>,
    series: LazyPagingItems<GameCardUI>,
    seriesState: PagedListState<GameCardUI>,
    onWebsiteClicked: () -> Unit,
    onBookmarkClicked: () -> Unit,
    onDescriptionOverflowChanged: (Boolean) -> Unit,
    onDescriptionToggleClicked: () -> Unit,
    onScreenshotClicked: (String) -> Unit,
    onMovieClicked: (String) -> Unit,
    onSeriesClicked: (Int) -> Unit,
) {
    Column(modifier = modifier) {
        BannerComponent(
            modifier = Modifier.fillMaxWidth(),
            rating = details.rating,
            image = details.image,
        )
        DetailsComponent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.lg),
            releaseDate = details.releaseDate,
            platforms = details.platforms,
            isBookmarked = details.isBookmarked,
            onWebsiteClicked = onWebsiteClicked,
            onBookmarkClicked = onBookmarkClicked,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

        DescriptionComponent(
            modifier = Modifier.padding(top = Spacing.lg),
            descriptionUi = details.gameDescriptionUi,
            onTextOverflow = onDescriptionOverflowChanged,
            onToggleClicked = onDescriptionToggleClicked,
        )

        if (details.companies.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

            CompanyComponent(
                modifier = Modifier.padding(Spacing.lg),
                companies = details.companies,
            )
        }

        if (
            screenshotsState is PagedListState.Loading ||
            screenshotsState is PagedListState.Loaded
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
            ScreenshotsComponent(
                modifier = Modifier
                    .padding(vertical = Spacing.lg)
                    .fillMaxWidth()
                    .testTag(GameDetailsTestTags.SCREENSHOTS_SECTION),
                pagingState = screenshotsState,
                onScreenshotClicked = onScreenshotClicked,
            )
        }

        if (
            moviesState is PagedListState.Loading ||
            moviesState is PagedListState.Loaded
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
            MoviesComponent(
                modifier = Modifier
                    .padding(vertical = Spacing.lg)
                    .fillMaxWidth()
                    .testTag(GameDetailsTestTags.MOVIES_SECTION),
                pagingState = moviesState,
                onMovieClicked = onMovieClicked,
            )
        }

        if (
            seriesState is PagedListState.Loading ||
            seriesState is PagedListState.Loaded
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
            SeriesComponent(
                modifier = Modifier
                    .padding(vertical = Spacing.lg)
                    .fillMaxWidth()
                    .testTag(GameDetailsTestTags.SERIES_SECTION),
                pagingState = seriesState,
                onSeriesClicked = onSeriesClicked,
            )
        }
    }
}

@Composable
@ThemePreview
private fun LoadingPreview() {
    GameDetailsPanePreview(
        state = GameDetailsTestData.loadingState,
        screenshots = GameDetailsTestData.emptyScreenshots,
        movies = GameDetailsTestData.emptyMovies,
        series = GameDetailsTestData.emptySeries,
    )
}

@Composable
@ThemePreview
private fun FilledDetailsWithLoadingMediaPreview() {
    GameDetailsPanePreview(
        state = GameDetailsTestData.readyState,
        screenshots = GameDetailsTestData.loadingScreenshots,
        movies = GameDetailsTestData.loadingMovies,
        series = GameDetailsTestData.loadingSeries,
    )
}

@Composable
@ThemePreview
private fun FilledPreview() {
    GameDetailsPanePreview(
        state = GameDetailsTestData.readyState,
        screenshots = GameDetailsTestData.screenshots,
        movies = GameDetailsTestData.movies,
        series = GameDetailsTestData.seriesState,
    )
}

@Composable
private fun GameDetailsPanePreview(
    state: GameDetailsState,
    screenshots: Flow<PagingData<ScreenshotUI>>,
    movies: Flow<PagingData<MovieUI>>,
    series: Flow<PagingData<GameCardUI>>,
) {
    NextPlayTheme {
        Surface {
            GameDetailsPaneScreen(
                modifier = Modifier.fillMaxSize(),
                gameDetailsState = state,
                screenshots = screenshots,
                movies = movies,
                series = series,
            )
        }
    }
}
