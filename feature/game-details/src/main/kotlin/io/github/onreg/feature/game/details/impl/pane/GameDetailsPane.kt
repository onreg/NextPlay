package io.github.onreg.feature.game.details.impl.pane

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.onreg.core.ui.animation.shimmer
import io.github.onreg.core.ui.components.chip.Chip
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.components.content.error.ContentError
import io.github.onreg.core.ui.components.content.error.ContentErrorUI
import io.github.onreg.core.ui.components.image.DynamicAsyncImage
import io.github.onreg.core.ui.runtime.collectWithLifecycle
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

private const val BANNER_ASPECT_RATIO: Float = 2f
private const val PLACEHOLDER_COUNT: Int = 3
private const val TITLE_PLACEHOLDER_WIDTH: Float = 0.6f
private const val SUBTITLE_PLACEHOLDER_WIDTH: Float = 0.7f
private const val LINK_PLACEHOLDER_WIDTH: Float = 0.5f

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

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            GameDetailsEvent.GoBack -> onGoBack()
            is GameDetailsEvent.OpenGameDetails -> onOpenGameDetails(event.gameId)
            is GameDetailsEvent.OpenImage -> openUrl(context, event.url)
            is GameDetailsEvent.OpenUrl -> openUrl(context, event.url)
            is GameDetailsEvent.OpenVideo -> openUrl(context, event.url)
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
            GameDetailsScreen(
                modifier = modifier,
                state = currentState,
                details = details,
                screenshots = screenshots,
                movies = movies,
                series = series,
                onBackClicked = viewModel::onBackClicked,
                onBannerClicked = { viewModel.onImageClicked(details.imageUrl) },
                onWebsiteClicked = viewModel::onWebsiteClicked,
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
internal fun LoadingContent(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(GameDetailsTestTags.LOADING),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shimmer(320.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(BANNER_ASPECT_RATIO)
                .shimmer(320.dp),
        )
        LoadingMetadataPlaceholder()
        repeat(PLACEHOLDER_COUNT) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .shimmer(320.dp),
            )
        }
    }
}

@Composable
private fun LoadingMetadataPlaceholder() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(TITLE_PLACEHOLDER_WIDTH)
                    .height(16.dp)
                    .shimmer(180.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(SUBTITLE_PLACEHOLDER_WIDTH)
                    .height(16.dp)
                    .shimmer(200.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(LINK_PLACEHOLDER_WIDTH)
                    .height(16.dp)
                    .shimmer(160.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .shimmer(48.dp),
        )
    }
}

@Composable
internal fun ErrorContent(
    modifier: Modifier,
    onRetry: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(GameDetailsTestTags.ERROR),
        contentAlignment = Alignment.Center,
    ) {
        ContentError(
            contentErrorUI = ContentErrorUI(
                iconRes = CoreUiR.drawable.ic_controller_off_24,
                titleResId = CoreUiR.string.error_message,
                descriptionResId = CoreUiR.string.error_network_message,
                actionLabelResId = CoreUiR.string.retry_text,
            ),
            onActionClick = onRetry,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun GameDetailsScreen(
    modifier: Modifier = Modifier,
    state: GameDetailsState,
    details: GameDetailsUi,
    screenshots: LazyPagingItems<Screenshot>,
    movies: LazyPagingItems<Movie>,
    series: LazyPagingItems<Game>,
    onBackClicked: () -> Unit = {},
    onBannerClicked: () -> Unit = {},
    onWebsiteClicked: () -> Unit = {},
    onBookmarkClicked: () -> Unit = {},
    onScreenshotClicked: (String) -> Unit = { _ -> },
    onMovieClicked: (String) -> Unit = { _ -> },
    onSeriesClicked: (Int) -> Unit = { _ -> },
    mapPlatforms: (Set<GamePlatform>) -> Set<PlatformUI> = { emptySet() },
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = details.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            painter = painterResource(CoreUiR.drawable.ic_back_24),
                            contentDescription = stringResource(CoreUiR.string.back_text),
                        )
                    }
                },
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
    onBookmarkClicked: () -> Unit,
    onScreenshotClicked: (String) -> Unit,
    onMovieClicked: (String) -> Unit,
    onSeriesClicked: (Int) -> Unit,
    mapPlatforms: (Set<GamePlatform>) -> Set<PlatformUI>,
) {
    var isDescriptionExpanded by rememberSaveable(details.gameId) { mutableStateOf(false) }
    var hasDescriptionOverflow by rememberSaveable(details.gameId) { mutableStateOf(false) }
    val screenshotsVisibility = sectionVisibility(screenshots)
    val moviesVisibility = sectionVisibility(movies)
    val seriesVisibility = sectionVisibility(series)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .testTag(GameDetailsTestTags.CONTENT),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        primarySections(
            state = state,
            details = details,
            onBannerClicked = onBannerClicked,
            onWebsiteClicked = onWebsiteClicked,
            onBookmarkClicked = onBookmarkClicked,
            isDescriptionExpanded = isDescriptionExpanded,
            hasDescriptionOverflow = hasDescriptionOverflow,
            onDescriptionOverflowChanged = { hasVisualOverflow ->
                hasDescriptionOverflow = hasVisualOverflow
            },
            onToggleDescription = {
                isDescriptionExpanded = !isDescriptionExpanded
            },
        )
        mediaSections(
            screenshots = screenshots,
            screenshotsVisibility = screenshotsVisibility,
            onScreenshotClicked = onScreenshotClicked,
            movies = movies,
            moviesVisibility = moviesVisibility,
            onMovieClicked = onMovieClicked,
            series = series,
            seriesVisibility = seriesVisibility,
            onSeriesClicked = onSeriesClicked,
            mapPlatforms = mapPlatforms,
        )
    }
}

private fun LazyListScope.primarySections(
    state: GameDetailsState,
    details: GameDetailsUi,
    onBannerClicked: () -> Unit,
    onWebsiteClicked: () -> Unit,
    onBookmarkClicked: () -> Unit,
    isDescriptionExpanded: Boolean,
    hasDescriptionOverflow: Boolean,
    onDescriptionOverflowChanged: (Boolean) -> Unit,
    onToggleDescription: () -> Unit,
) {
    item {
        BannerSection(
            ratingChip = details.ratingChip,
            imageUrl = details.imageUrl,
            onClick = onBannerClicked,
        )
    }
    item {
        DetailsSection(
            state = state,
            details = details,
            onWebsiteClicked = onWebsiteClicked,
            onBookmarkClicked = onBookmarkClicked,
        )
    }
    item {
        DescriptionSection(
            description = details.description,
            isExpanded = isDescriptionExpanded,
            hasOverflow = hasDescriptionOverflow,
            onDescriptionTextLayout = onDescriptionOverflowChanged,
            onToggleDescription = onToggleDescription,
        )
    }
    if (details.companies.isNotEmpty()) {
        item {
            DevelopersAndPublishersSection(companies = details.companies)
        }
    }
}

private fun LazyListScope.mediaSections(
    screenshots: LazyPagingItems<Screenshot>,
    screenshotsVisibility: SectionVisibility,
    onScreenshotClicked: (String) -> Unit,
    movies: LazyPagingItems<Movie>,
    moviesVisibility: SectionVisibility,
    onMovieClicked: (String) -> Unit,
    series: LazyPagingItems<Game>,
    seriesVisibility: SectionVisibility,
    onSeriesClicked: (Int) -> Unit,
    mapPlatforms: (Set<GamePlatform>) -> Set<PlatformUI>,
) {
    if (screenshotsVisibility != SectionVisibility.Hide) {
        item {
            ScreenshotsSection(
                screenshots = screenshots,
                isLoading = screenshotsVisibility == SectionVisibility.ShowLoading,
                onScreenshotClicked = onScreenshotClicked,
            )
        }
    }
    if (moviesVisibility != SectionVisibility.Hide) {
        item {
            MoviesSection(
                movies = movies,
                isLoading = moviesVisibility == SectionVisibility.ShowLoading,
                onMovieClicked = onMovieClicked,
            )
        }
    }
    if (seriesVisibility != SectionVisibility.Hide) {
        item {
            SeriesSection(
                series = series,
                isLoading = seriesVisibility == SectionVisibility.ShowLoading,
                onSeriesClicked = onSeriesClicked,
                mapPlatforms = mapPlatforms,
            )
        }
    }
}

@Composable
private fun BannerSection(
    ratingChip: ChipUI,
    imageUrl: String,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.padding(bottom = 16.dp)) {
        Box {
            DynamicAsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(BANNER_ASPECT_RATIO)
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onClick),
                imageUrl = imageUrl,
            )
            Chip(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp),
                chipUI = ratingChip,
            )
        }
    }
}
