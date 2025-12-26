package io.github.onreg.feature.game.impl.pane

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.onreg.core.ui.components.card.GameCard
import io.github.onreg.core.ui.components.card.GameCardLoading
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.core.ui.components.state.ContentError
import io.github.onreg.core.ui.components.state.ContentErrorUI
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.feature.game.impl.GamesPaneViewModel
import io.github.onreg.feature.game.impl.GamesViewModel
import io.github.onreg.feature.game.impl.R
import io.github.onreg.feature.game.impl.model.Event
import io.github.onreg.feature.game.impl.model.GamePaneState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
public fun GamesPane(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    GamesPane(
        modifier = modifier.fillMaxSize(),
        navController = navController,
        viewModel = hiltViewModel<GamesViewModel>()
    )
}

@Composable
private fun GamesPane(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: GamesPaneViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (state) {
        is GamePaneState.Ready -> Content(
            modifier = modifier,
            viewModel = viewModel,
            navigate = navController::navigate
        )

        is GamePaneState.Error -> Error(
            modifier = modifier,
            onRetry = viewModel::onRetryClicked
        )
    }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    viewModel: GamesPaneViewModel,
    navigate: (String) -> Unit
) {
    val lazyPagingItems = viewModel.dataState.collectAsLazyPagingItems()

    val hasData = lazyPagingItems.itemCount > 0

    val isLoading = lazyPagingItems.loadState.refresh is LoadState.Loading
    val isSourceLoading = lazyPagingItems.loadState.source.refresh is LoadState.Loading
    val isError = lazyPagingItems.loadState.refresh is LoadState.Error

    val showFullScreenLoading = !hasData && (isLoading || isSourceLoading)
    val showFullScreenError = !hasData && isError

    when {
        showFullScreenLoading -> LoadingGrid(modifier = modifier)
        showFullScreenError -> Error(
            modifier = modifier,
            onRetry = viewModel::onRetryClicked
        )

        hasData -> {
            GamesGrid(
                modifier = modifier,
                lazyPagingItems = lazyPagingItems,
                onRefresh = viewModel::onRefreshClicked,
                onRetry = viewModel::onPageRetryClicked,
                onBookmarkClicked = viewModel::onBookMarkClicked,
                onCardClicked = viewModel::onCardClicked
            )
        }
    }

    ObserveEvents(
        event = viewModel.events,
        navigate,
        onRetryClicked = { lazyPagingItems.retry() },
        onRefreshClicked = { lazyPagingItems.refresh() }
    )
}

@Composable
private fun ObserveEvents(
    event: Flow<Event>,
    navigate: (String) -> Unit,
    onRetryClicked: () -> Unit,
    onRefreshClicked: () -> Unit,
) {
    event.collectWithLifecycle { event ->
        when (event) {
            is Event.GoToDetails -> navigate(GamesRoute.detailsRoute(event.gameId))
            Event.ListEvent.Retry -> onRetryClicked()
            Event.ListEvent.Refresh -> onRefreshClicked()
        }
    }
}

@Composable
private fun GamesGrid(
    modifier: Modifier,
    lazyPagingItems: LazyPagingItems<GameCardUI>,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onBookmarkClicked: (String) -> Unit,
    onCardClicked: (String) -> Unit
) {
    val isNextPageError = lazyPagingItems.loadState.append is LoadState.Error
    val isNextPageLoading = lazyPagingItems.loadState.append is LoadState.Loading
    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading
    val loadingItemsCount = if (isNextPageLoading) 20 else 0

    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = modifier,
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier
                    .align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                state = pullToRefreshState
            )
        }
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(1),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            items(
                count = lazyPagingItems.itemCount,
                key = lazyPagingItems.itemKey { item -> item.id }
            ) { index ->
                val item = lazyPagingItems[index]
                if (item != null) {
                    GameCard(
                        gameData = item,
                        onBookmarkClick = { onBookmarkClicked(item.id) },
                        onCardClicked = { onCardClicked(item.id) }
                    )
                }
            }
            if (isNextPageLoading) {
                items(count = loadingItemsCount) {
                    GameCardLoading()
                }
            }
            if (isNextPageError) {
                item {
                    Error(onRetry = onRetry)
                }
            }
        }
    }
}

@Composable
private fun LoadingGrid(
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(1),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        items(20) {
            GameCardLoading()
        }
    }
}

@Composable
private fun Error(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        ContentError(
            contentErrorUI = ContentErrorUI(
                iconRes = io.github.onreg.core.ui.R.drawable.ic_controller_off_24,
                titleResId = R.string.games_error_title,
                descriptionResId = R.string.games_error_description,
                actionLabelResId = R.string.games_error_retry,
            ),
            onActionClick = onRetry
        )
    }
}

public object GamesRoute {
    public const val games: String = "GamesPane"
    public const val details: String = "GameDetails/{gameId}"
    public fun detailsRoute(gameId: String): String = "GameDetails/$gameId"
}

/**
 * Collects values from a [Flow] inside a [Composable], automatically starting and stopping
 * with the provided [lifecycleOwner] and [minActiveState]. The collector suspends when the
 * lifecycle is below the given state and resumes when active again, always invoking the latest [action].
 */
@SuppressLint("ComposableNaming")
@Composable
internal fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
) {
    val currentAction by rememberUpdatedState(action)
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            this@collectWithLifecycle.collectLatest { value ->
                currentAction(value)
            }
        }
    }
}
