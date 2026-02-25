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
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.onreg.core.ui.components.content.error.ContentError
import io.github.onreg.core.ui.components.content.error.ContentErrorUI
import io.github.onreg.core.ui.components.header.AppHeader
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.runtime.collectWithLifecycle
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
import io.github.onreg.feature.game.details.impl.pane.component.ScreenshotsComponent
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestTags
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import kotlinx.coroutines.flow.Flow
import io.github.onreg.core.ui.R as CoreUiR

@Composable
public fun GameDetailsPane(
    modifier: Modifier = Modifier,
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

    GameDetailsScreen(
        modifier = modifier.fillMaxSize(),
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
private fun GameDetailsScreen(
    modifier: Modifier,
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
                    val movies = movies.collectAsLazyPagingItems()
                    val screenshots = screenshots.collectAsLazyPagingItems()
                    val series = series.collectAsLazyPagingItems()

                    GameDetailsContent(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(bottom = Spacing.lg)
                            .testTag(GameDetailsTestTags.CONTENT),
                        details = gameDetailsState.details,
                        screenshots = screenshots,
                        movies = movies,
                        series = series,
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
private fun GameDetailsContent(
    modifier: Modifier = Modifier,
    details: GameDetailsUi,
    screenshots: LazyPagingItems<ScreenshotUI>,
    movies: LazyPagingItems<MovieUI>,
    series: LazyPagingItems<GameCardUI>,
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
            modifier = Modifier
                .padding(top = Spacing.lg),
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

        if (screenshots.itemCount > 0 || screenshots.loadState.refresh is LoadState.Loading) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

            ScreenshotsComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg),
                screenshots = screenshots,
                onScreenshotClicked = onScreenshotClicked,
            )
        }
        if (movies.itemCount > 0 || movies.loadState.refresh is LoadState.Loading) {
            MoviesSection(
                movies = movies,
                isLoading = movies.loadState.refresh is LoadState.Loading,
                onMovieClicked = onMovieClicked,
            )
        }
        if (series.itemCount > 0 || series.loadState.refresh is LoadState.Loading) {
            SeriesSection(
                series = series,
                isLoading = series.loadState.refresh is LoadState.Loading,
                onSeriesClicked = onSeriesClicked,
            )
        }
    }
}


@Composable
@ThemePreview
private fun FilledPreview() {
    GameDetailsContentPreview(
        state = GameDetailsTestData.readyState,
        screenshots = GameDetailsTestData.screenshots,
        movies = GameDetailsTestData.movies,
        series = GameDetailsTestData.seriesState,
    )
}

@Composable
private fun GameDetailsContentPreview(
    state: GameDetailsState.Ready,
    screenshots: Flow<PagingData<ScreenshotUI>>,
    movies: Flow<PagingData<MovieUI>>,
    series: Flow<PagingData<GameCardUI>>,
) {
    NextPlayTheme {
        val screenshotsItems = screenshots.collectAsLazyPagingItems()
        val moviesItems = movies.collectAsLazyPagingItems()
        val seriesItems = series.collectAsLazyPagingItems()

        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                GameDetailsContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = Spacing.lg),
                    details = state.details,
                    screenshots = screenshotsItems,
                    movies = moviesItems,
                    series = seriesItems,
                    onWebsiteClicked = {},
                    onBookmarkClicked = {},
                    onDescriptionOverflowChanged = {},
                    onDescriptionToggleClicked = {},
                    onScreenshotClicked = {},
                    onMovieClicked = {},
                    onSeriesClicked = {},
                )
            }
        }
    }
}
