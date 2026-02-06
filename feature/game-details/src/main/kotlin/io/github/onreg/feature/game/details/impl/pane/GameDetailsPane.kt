package io.github.onreg.feature.game.details.impl.pane

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.onreg.core.ui.runtime.collectWithLifecycle
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.feature.game.details.impl.GameDetailsViewModel
import io.github.onreg.feature.game.details.impl.R
import io.github.onreg.feature.game.details.impl.model.GameDetailsEvent
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestTags
import io.github.onreg.ui.details.presentation.components.DetailsSectionHeader
import io.github.onreg.ui.details.presentation.components.GameDetailsDescription
import io.github.onreg.ui.details.presentation.components.GameDetailsHeader
import io.github.onreg.ui.details.presentation.model.GameDetailsUi
import io.github.onreg.ui.details.presentation.model.MovieUi
import io.github.onreg.ui.details.presentation.model.ScreenshotUi
import io.github.onreg.ui.game.presentation.components.card.model.GameCardUI
import kotlinx.coroutines.flow.Flow

@Composable
public fun GameDetailsPane(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val viewModel = hiltViewModel<GameDetailsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    viewModel.events.collectWithLifecycle { event ->
        handleEvent(
            event = event,
            navController = navController,
            context = context,
        )
    }

    GameDetailsScreen(
        modifier = modifier.fillMaxSize(),
        detailsUi = state.detailsUi,
        isInitialLoading = state.isInitialLoading,
        initialError = state.initialError?.message,
        isDescriptionExpanded = state.isDescriptionExpanded,
        screenshots = state.screenshots,
        movies = state.movies,
        series = state.series,
        onBackClick = viewModel::onBackClicked,
        onRetryClick = viewModel::onRetryClicked,
        onWebsiteClick = viewModel::onWebsiteClicked,
        onScreenshotClick = viewModel::onScreenshotClicked,
        onMovieClick = viewModel::onMovieClicked,
        onSeriesClick = viewModel::onSeriesGameClicked,
        onToggleDescription = viewModel::onToggleDescription,
        onBookmarkClick = viewModel::onBookmarkClicked,
    )
}

@Composable
internal fun GameDetailsScreen(
    modifier: Modifier = Modifier,
    detailsUi: GameDetailsUi?,
    isInitialLoading: Boolean,
    initialError: String?,
    isDescriptionExpanded: Boolean,
    screenshots: Flow<androidx.paging.PagingData<ScreenshotUi>>,
    movies: Flow<androidx.paging.PagingData<MovieUi>>,
    series: Flow<androidx.paging.PagingData<GameCardUI>>,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onWebsiteClick: (String) -> Unit,
    onScreenshotClick: (String) -> Unit,
    onMovieClick: (String) -> Unit,
    onSeriesClick: (Int) -> Unit,
    onToggleDescription: () -> Unit,
    onBookmarkClick: (String) -> Unit,
) {
    when {
        detailsUi == null && isInitialLoading -> {
            LoadingState(modifier)
        }

        detailsUi == null && initialError != null -> {
            ErrorState(
                modifier = modifier,
                message = initialError,
                onRetryClick = onRetryClick,
            )
        }

        else -> {
            GameDetailsContent(
                modifier = modifier,
                detailsUi = detailsUi,
                isDescriptionExpanded = isDescriptionExpanded,
                screenshots = screenshots,
                movies = movies,
                series = series,
                onBackClick = onBackClick,
                onWebsiteClick = onWebsiteClick,
                onScreenshotClick = onScreenshotClick,
                onMovieClick = onMovieClick,
                onSeriesClick = onSeriesClick,
                onToggleDescription = onToggleDescription,
                onBookmarkClick = onBookmarkClick,
            )
        }
    }
}

private fun handleEvent(
    event: GameDetailsEvent,
    navController: NavHostController,
    context: Context,
) {
    when (event) {
        GameDetailsEvent.GoBack -> {
            navController.popBackStack()
        }

        is GameDetailsEvent.OpenUrl -> {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, event.url.toUri()),
            )
        }

        is GameDetailsEvent.OpenImage -> {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, event.url.toUri()),
            )
        }

        is GameDetailsEvent.OpenVideo -> {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, event.url.toUri()),
            )
        }

        is GameDetailsEvent.OpenGameDetails -> {
            val route = detailsRoute(event.gameId)
            navController.navigate(route)
        }
    }
}

private fun detailsRoute(gameId: Int): String = "GameDetails/$gameId"

@Composable
private fun LoadingState(modifier: Modifier) {
    Box(
        modifier = modifier.testTag(GameDetailsTestTags.LOADING),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    modifier: Modifier,
    message: String,
    onRetryClick: () -> Unit,
) {
    Box(
        modifier = modifier.testTag(GameDetailsTestTags.ERROR),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Button(onClick = onRetryClick) {
                Text(text = stringResource(R.string.details_retry))
            }
        }
    }
}

@Composable
private fun GameDetailsContent(
    modifier: Modifier,
    detailsUi: GameDetailsUi?,
    isDescriptionExpanded: Boolean,
    screenshots: Flow<androidx.paging.PagingData<ScreenshotUi>>,
    movies: Flow<androidx.paging.PagingData<MovieUi>>,
    series: Flow<androidx.paging.PagingData<GameCardUI>>,
    onBackClick: () -> Unit,
    onWebsiteClick: (String) -> Unit,
    onScreenshotClick: (String) -> Unit,
    onMovieClick: (String) -> Unit,
    onSeriesClick: (Int) -> Unit,
    onToggleDescription: () -> Unit,
    onBookmarkClick: (String) -> Unit,
) {
    val screenshotItems = screenshots.collectAsLazyPagingItems()
    val movieItems = movies.collectAsLazyPagingItems()
    val seriesItems = series.collectAsLazyPagingItems()

    LazyColumn(
        modifier = modifier.testTag(GameDetailsTestTags.CONTENT),
        contentPadding = PaddingValues(bottom = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        item {
            GameDetailsHeader(
                title = detailsUi?.title.orEmpty(),
                onBackClick = onBackClick,
            )
        }
        bannerItem(detailsUi)
        item {
            DetailsCard(
                detailsUi = detailsUi,
                onWebsiteClick = onWebsiteClick,
                onBookmarkClick = onBookmarkClick,
            )
        }
        descriptionSection(
            detailsUi = detailsUi,
            isDescriptionExpanded = isDescriptionExpanded,
            onToggleDescription = onToggleDescription,
        )
        developersSection(detailsUi)
        screenshotsSection(
            items = screenshotItems,
            onScreenshotClick = onScreenshotClick,
        )
        moviesSection(
            items = movieItems,
            onMovieClick = onMovieClick,
        )
        seriesSection(
            items = seriesItems,
            onSeriesClick = onSeriesClick,
            onBookmarkClick = onBookmarkClick,
        )
    }
}
