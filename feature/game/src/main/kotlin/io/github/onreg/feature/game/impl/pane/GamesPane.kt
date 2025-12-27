package io.github.onreg.feature.game.impl.pane

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.onreg.core.ui.R
import io.github.onreg.core.ui.components.list.GameList
import io.github.onreg.core.ui.components.content.error.ContentError
import io.github.onreg.core.ui.components.content.error.ContentErrorUI
import io.github.onreg.core.ui.runtime.collectWithLifecycle
import io.github.onreg.feature.game.impl.GamesPaneViewModel
import io.github.onreg.feature.game.impl.GamesViewModel
import io.github.onreg.feature.game.impl.model.Event
import io.github.onreg.feature.game.impl.model.GamePaneState

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
                iconRes = R.drawable.ic_controller_off_24,
                titleResId = R.string.games_error_title,
                descriptionResId = R.string.games_error_description,
                actionLabelResId = R.string.retry,
            ),
            onActionClick = onRetry
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
    GameList(
        modifier = modifier,
        lazyPagingItems = lazyPagingItems,
        columns = 1,
        onRefresh = viewModel::onRefreshClicked,
        onRetry = viewModel::onPageRetryClicked,
        onBookmarkClicked = viewModel::onBookMarkClicked,
        onCardClicked = viewModel::onCardClicked,
    )

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            is Event.GoToDetails -> navigate(GamesRoute.detailsRoute(event.gameId))
            Event.ListEvent.Retry -> {
                lazyPagingItems.retry()
            }

            Event.ListEvent.Refresh -> {
                lazyPagingItems.refresh()
            }
        }
    }
}

public object GamesRoute {
    public const val games: String = "GamesPane"
    public const val details: String = "GameDetails/{gameId}"
    public fun detailsRoute(gameId: String): String = "GameDetails/$gameId"
}
