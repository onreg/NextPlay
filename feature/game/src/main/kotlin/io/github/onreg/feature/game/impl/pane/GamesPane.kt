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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.onreg.core.ui.R
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.core.ui.components.content.error.ContentError
import io.github.onreg.core.ui.components.content.error.ContentErrorUI
import io.github.onreg.core.ui.components.content.info.ContentInfo
import io.github.onreg.core.ui.components.content.info.ContentInfoUI
import io.github.onreg.core.ui.components.list.GameList
import io.github.onreg.core.ui.runtime.collectWithLifecycle
import io.github.onreg.feature.game.impl.GamesViewModel
import io.github.onreg.feature.game.impl.model.Event
import io.github.onreg.feature.game.impl.model.GamePaneState

@Composable
public fun GamesPane(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val viewModel = hiltViewModel<GamesViewModel>()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagingState = viewModel.pagingState.collectAsLazyPagingItems()

    GamesPane(
        modifier = modifier.fillMaxSize(),
        gamePaneState = state,
        pagingState = pagingState,
        onRetry = viewModel::onRetryClicked,
        onRefreshClicked = viewModel::onRefreshClicked,
        onPageRetryClicked = viewModel::onPageRetryClicked,
        onBookMarkClicked = viewModel::onBookMarkClicked,
        onCardClicked = viewModel::onCardClicked
    )

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            is Event.GoToDetails -> navController.navigate(GamesRoute.detailsRoute(event.gameId))
            Event.ListEvent.Retry -> pagingState.retry()
            Event.ListEvent.Refresh -> pagingState.refresh()
        }
    }
}

@Composable
private fun GamesPane(
    modifier: Modifier = Modifier,
    gamePaneState: GamePaneState,
    pagingState: LazyPagingItems<GameCardUI>,
    onRetry: () -> Unit,
    onRefreshClicked: () -> Unit,
    onPageRetryClicked: () -> Unit,
    onBookMarkClicked: (String) -> Unit,
    onCardClicked: (String) -> Unit
) {
    when (gamePaneState) {
        is GamePaneState.Ready -> ContentComponent(
            modifier = modifier,
            pagingState = pagingState,
            onRefreshClicked = onRefreshClicked,
            onPageRetryClicked = onPageRetryClicked,
            onBookMarkClicked = onBookMarkClicked,
            onCardClicked = onCardClicked
        )

        is GamePaneState.Error -> ErrorComponent(
            modifier = modifier,
            onRetry = onRetry
        )
    }
}

@Composable
private fun ContentComponent(
    modifier: Modifier = Modifier,
    pagingState: LazyPagingItems<GameCardUI>,
    onRefreshClicked: () -> Unit,
    onPageRetryClicked: () -> Unit,
    onBookMarkClicked: (String) -> Unit,
    onCardClicked: (String) -> Unit
) {
    GameList(
        modifier = modifier,
        lazyPagingItems = pagingState,
        columns = 1,
        onRefresh = onRefreshClicked,
        onRetry = onPageRetryClicked,
        onBookmarkClicked = onBookMarkClicked,
        onCardClicked = onCardClicked,
        onError = {
            ErrorComponent(
                modifier = modifier,
                onRetry = onPageRetryClicked
            )
        },
        onEmpty = { EmptyComponent(modifier) }
    )
}

@Composable
private fun ErrorComponent(
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
private fun EmptyComponent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        ContentInfo(
            contentInfoUI = ContentInfoUI(
                iconRes = R.drawable.ic_controller_24,
                titleResId = R.string.games_empty_title,
                descriptionResId = R.string.games_empty_description
            )
        )
    }
}

public object GamesRoute {
    public const val games: String = "GamesPane"
    public const val details: String = "GameDetails/{gameId}"
    public fun detailsRoute(gameId: String): String = "GameDetails/$gameId"
}
