package io.github.onreg.feature.game.details.impl.pane

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.onreg.core.ui.components.image.DynamicAsyncImage
import io.github.onreg.data.game.list.api.model.Game
import io.github.onreg.data.game.list.api.model.GamePlatform
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.feature.game.details.impl.GameDetailsViewModel
import io.github.onreg.feature.game.details.impl.model.GameDetailsEvent
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestTags
import io.github.onreg.feature.game.details.impl.ui.model.GameDetailsUi
import io.github.onreg.ui.platform.model.PlatformUI
import io.github.onreg.core.ui.R as CoreUiR

@Composable
public fun GameDetailsPane(
    gameId: Int,
    modifier: Modifier = Modifier,
    onGoBack: () -> Unit,
    onOpenGameDetails: (Int) -> Unit,
) {
    val viewModel = hiltViewModel<GameDetailsViewModel>()
    val currentState by viewModel.state.collectAsStateWithLifecycle()
    val screenshots = viewModel.screenshots.collectAsLazyPagingItems()
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val series = viewModel.series.collectAsLazyPagingItems()
    val context = LocalContext.current

    LaunchedEffect(gameId) {
        viewModel.initialize(gameId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                GameDetailsEvent.GoBack -> onGoBack()
                is GameDetailsEvent.OpenGameDetails -> onOpenGameDetails(event.gameId)
                is GameDetailsEvent.OpenImage -> openUrl(context, event.url)
                is GameDetailsEvent.OpenUrl -> openUrl(context, event.url)
                is GameDetailsEvent.OpenVideo -> openUrl(context, event.url)
            }
        }
    }

    when {
        currentState.isInitialLoading && currentState.details == null -> {
            LoadingContent(modifier)
        }

        currentState.isInitialError && currentState.details == null -> {
            ErrorContent(modifier = modifier, onRetry = viewModel::refresh)
        }

        else -> {
            val details = currentState.details ?: return
            GameDetailsContent(
                modifier = modifier,
                state = currentState,
                details = details,
                screenshots = screenshots,
                movies = movies,
                series = series,
                onBackClicked = viewModel::onBackClicked,
                onBannerClicked = { viewModel.onImageClicked(details.imageUrl) },
                onWebsiteClicked = viewModel::onWebsiteClicked,
                onToggleDescription = viewModel::onToggleDescription,
                onBookmarkClicked = viewModel::onBookmarkClicked,
                onScreenshotClicked = { viewModel.onImageClicked(it) },
                onMovieClicked = { viewModel.onVideoClicked(it) },
                onSeriesClicked = { viewModel.onSeriesGameClicked(it) },
                mapPlatforms = viewModel::mapPlatforms,
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(GameDetailsTestTags.LOADING),
    )
}

@Composable
private fun ErrorContent(
    modifier: Modifier,
    onRetry: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(GameDetailsTestTags.ERROR),
    ) {
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameDetailsContent(
    modifier: Modifier,
    state: GameDetailsState,
    details: GameDetailsUi,
    screenshots: LazyPagingItems<Screenshot>,
    movies: LazyPagingItems<Movie>,
    series: LazyPagingItems<Game>,
    onBackClicked: () -> Unit,
    onBannerClicked: () -> Unit,
    onWebsiteClicked: () -> Unit,
    onToggleDescription: () -> Unit,
    onBookmarkClicked: () -> Unit,
    onScreenshotClicked: (String) -> Unit,
    onMovieClicked: (String) -> Unit,
    onSeriesClicked: (Int) -> Unit,
    mapPlatforms: (Set<GamePlatform>) -> Set<PlatformUI>,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = details.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            painter = painterResource(CoreUiR.drawable.ic_back_24),
                            contentDescription = "Back",
                        )
                    }
                },
                actions = { Box(modifier = Modifier.size(48.dp)) },
            )
        },
    ) { paddingValues ->
        GameDetailsItems(
            paddingValues = paddingValues,
            state = state,
            details = details,
            screenshots = screenshots,
            movies = movies,
            series = series,
            onBannerClicked = onBannerClicked,
            onWebsiteClicked = onWebsiteClicked,
            onToggleDescription = onToggleDescription,
            onBookmarkClicked = onBookmarkClicked,
            onScreenshotClicked = onScreenshotClicked,
            onMovieClicked = onMovieClicked,
            onSeriesClicked = onSeriesClicked,
            mapPlatforms = mapPlatforms,
        )
    }
}

@Composable
private fun GameDetailsItems(
    paddingValues: PaddingValues,
    state: GameDetailsState,
    details: GameDetailsUi,
    screenshots: LazyPagingItems<Screenshot>,
    movies: LazyPagingItems<Movie>,
    series: LazyPagingItems<Game>,
    onBannerClicked: () -> Unit,
    onWebsiteClicked: () -> Unit,
    onToggleDescription: () -> Unit,
    onBookmarkClicked: () -> Unit,
    onScreenshotClicked: (String) -> Unit,
    onMovieClicked: (String) -> Unit,
    onSeriesClicked: (Int) -> Unit,
    mapPlatforms: (Set<GamePlatform>) -> Set<PlatformUI>,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .testTag(GameDetailsTestTags.CONTENT),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item { BannerSection(imageUrl = details.imageUrl, onClick = onBannerClicked) }
        item {
            DetailsSection(
                state = state,
                details = details,
                onWebsiteClicked = onWebsiteClicked,
                onToggleDescription = onToggleDescription,
                onBookmarkClicked = onBookmarkClicked,
            )
        }
        if (shouldShowSection(screenshots)) {
            item {
                ScreenshotsSection(
                    screenshots = screenshots,
                    onScreenshotClicked = onScreenshotClicked,
                )
            }
        }
        if (shouldShowSection(movies)) {
            item {
                MoviesSection(
                    movies = movies,
                    onMovieClicked = onMovieClicked,
                )
            }
        }
        if (shouldShowSection(series)) {
            item {
                SeriesSection(
                    series = series,
                    onSeriesClicked = onSeriesClicked,
                    mapPlatforms = mapPlatforms,
                )
            }
        }
    }
}

@Composable
private fun BannerSection(
    imageUrl: String,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        DynamicAsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(198.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onClick),
            imageUrl = imageUrl,
        )
    }
}
