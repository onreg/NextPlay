package io.github.onreg.feature.game.impl.pane

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.onreg.core.ui.R as CoreUiR
import io.github.onreg.ui.game.presentation.R
import io.github.onreg.core.ui.components.content.error.ContentError
import io.github.onreg.core.ui.components.content.error.ContentErrorUI
import io.github.onreg.core.ui.components.content.info.ContentInfo
import io.github.onreg.core.ui.components.content.info.ContentInfoUI
import io.github.onreg.ui.game.presentation.components.list.GameList
import io.github.onreg.ui.game.presentation.components.list.test.GameListTestData
import io.github.onreg.core.ui.preview.TabletThemePreview
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.runtime.collectWithLifecycle
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.feature.game.impl.GamesViewModel
import io.github.onreg.feature.game.impl.model.Event
import io.github.onreg.feature.game.impl.model.GamePaneState
import io.github.onreg.feature.game.impl.model.ListEvent
import io.github.onreg.feature.game.impl.test.GamesPaneTestTags
import io.github.onreg.ui.game.presentation.components.card.model.GameCardUI
import kotlinx.coroutines.flow.Flow

@Composable
public fun GamesPane(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    isLargeScreen: Boolean = false
) {
    val viewModel = hiltViewModel<GamesViewModel>()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagingState = viewModel.pagingState.collectAsLazyPagingItems()

    GamesPaneScreen(
        modifier = modifier.fillMaxSize(),
        isLargeScreen = isLargeScreen,
        gamePaneState = state,
        pagingState = pagingState,
        onRefreshClicked = viewModel::onRefreshClicked,
        onRetryClicked = viewModel::onRetryClicked,
        onBookMarkClicked = viewModel::onBookMarkClicked,
        onCardClicked = viewModel::onCardClicked
    )

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            is Event.GoToDetails -> navController.navigate(GamesRoute.detailsRoute(event.gameId))
        }
    }

    viewModel.pagingEvents.collectWithLifecycle { event ->
        when (event) {
            ListEvent.Retry -> pagingState.retry()
            ListEvent.Refresh -> pagingState.refresh()
        }
    }
}

@Composable
internal fun GamesPaneScreen(
    modifier: Modifier = Modifier,
    isLargeScreen: Boolean = false,
    gamePaneState: GamePaneState,
    pagingState: LazyPagingItems<GameCardUI>,
    onRefreshClicked: () -> Unit = {},
    onRetryClicked: () -> Unit = {},
    onBookMarkClicked: (String) -> Unit = {},
    onCardClicked: (String) -> Unit = {}
) {
    ContentComponent(
        modifier = modifier,
        isLargeScreen = isLargeScreen,
        pagingState = pagingState,
        onRefreshClicked = onRefreshClicked,
        onPageRetryClicked = onRetryClicked,
        onBookMarkClicked = onBookMarkClicked,
        onCardClicked = onCardClicked
    )
}

@Composable
private fun ContentComponent(
    modifier: Modifier = Modifier,
    isLargeScreen: Boolean = false,
    pagingState: LazyPagingItems<GameCardUI>,
    onRefreshClicked: () -> Unit,
    onPageRetryClicked: () -> Unit,
    onBookMarkClicked: (String) -> Unit,
    onCardClicked: (String) -> Unit
) {
    GameList(
        modifier = modifier,
        lazyPagingItems = pagingState,
        columns = if (isLargeScreen) 4 else 1,
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
        modifier = modifier.testTag(GamesPaneTestTags.TAG_COMPONENT_ERROR),
        contentAlignment = Alignment.Center
    ) {
        ContentError(
            contentErrorUI = ContentErrorUI(
                iconRes = CoreUiR.drawable.ic_controller_off_24,
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
        modifier = modifier.testTag(GamesPaneTestTags.TAG_COMPONENT_EMPTY),
        contentAlignment = Alignment.Center
    ) {
        ContentInfo(
            contentInfoUI = ContentInfoUI(
                iconRes = CoreUiR.drawable.ic_controller_24,
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

@Composable
@ThemePreview
private fun ErrorPreview() {
    GamesPanePreview(
        gamePaneState = GamePaneState,
        pagingState = GameListTestData.emptyState
    )
}

@Composable
@ThemePreview
private fun EmptyPreview() {
    GamesPanePreview(
        gamePaneState = GamePaneState,
        pagingState = GameListTestData.emptyState
    )
}

@Composable
@ThemePreview
private fun LoadingPreview() {
    GamesPanePreview(
        gamePaneState = GamePaneState,
        pagingState = GameListTestData.loadingState
    )
}

@Composable
@ThemePreview
private fun LoadedPreview() {
    GamesPanePreview(
        gamePaneState = GamePaneState,
        pagingState = GameListTestData.loadedState
    )
}

@Composable
@ThemePreview
private fun PagingErrorPreview() {
    GamesPanePreview(
        gamePaneState = GamePaneState,
        pagingState = GameListTestData.pagingErrorState
    )
}

@Composable
@TabletThemePreview
private fun ErrorTabletPreview() {
    GamesPanePreview(
        isLargeScreen = true,
        gamePaneState = GamePaneState,
        pagingState = GameListTestData.emptyState
    )
}

@Composable
@TabletThemePreview
private fun EmptyTabletPreview() {
    GamesPanePreview(
        isLargeScreen = true,
        gamePaneState = GamePaneState,
        pagingState = GameListTestData.emptyState
    )
}

@Composable
@TabletThemePreview
private fun LoadingTabletPreview() {
    GamesPanePreview(
        isLargeScreen = true,
        gamePaneState = GamePaneState,
        pagingState = GameListTestData.loadingState
    )
}

@Composable
@TabletThemePreview
private fun PagingErrorTabletPreview() {
    GamesPanePreview(
        isLargeScreen = true,
        gamePaneState = GamePaneState,
        pagingState = GameListTestData.pagingErrorState
    )
}

@Composable
@TabletThemePreview
private fun LoadedTabletPreview() {
    GamesPanePreview(
        isLargeScreen = true,
        gamePaneState = GamePaneState,
        pagingState = GameListTestData.loadedState
    )
}

@Composable
private fun GamesPanePreview(
    isLargeScreen: Boolean = false,
    gamePaneState: GamePaneState,
    pagingState: Flow<PagingData<GameCardUI>>
) {
    NextPlayTheme {
        val lazyPagingItems = pagingState.collectAsLazyPagingItems()
        Scaffold { paddingValues ->
            GamesPaneScreen(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                isLargeScreen = isLargeScreen,
                gamePaneState = gamePaneState,
                pagingState = lazyPagingItems
            )
        }
    }
}
