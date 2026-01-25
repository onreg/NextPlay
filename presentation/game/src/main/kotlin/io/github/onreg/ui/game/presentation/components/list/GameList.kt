package io.github.onreg.ui.game.presentation.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.onreg.core.ui.preview.TabletThemePreview
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.ui.game.presentation.components.card.GameCard
import io.github.onreg.ui.game.presentation.components.card.GameCardError
import io.github.onreg.ui.game.presentation.components.card.GameCardLoading
import io.github.onreg.ui.game.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.game.presentation.components.card.model.GameListErrorType
import io.github.onreg.ui.game.presentation.components.list.test.GameListTestData
import io.github.onreg.ui.game.presentation.components.list.test.GameListTestTags
import kotlinx.coroutines.flow.Flow
import java.io.IOException

private const val LOADING_ITEMS_COUNT = 20

@Composable
public fun GameList(
    modifier: Modifier = Modifier,
    lazyPagingItems: LazyPagingItems<GameCardUI>,
    columns: Int = 1,
    onRefresh: () -> Unit = {},
    onRetry: () -> Unit = {},
    onBookmarkClicked: (String) -> Unit = {},
    onCardClicked: (String) -> Unit = {},
    onError: @Composable (error: GameListErrorType) -> Unit = {},
    onEmpty: @Composable () -> Unit = {},
) {
    val hasData = lazyPagingItems.itemCount > 0

    val srcRefresh = lazyPagingItems.loadState.source.refresh
    val medRefresh = lazyPagingItems.loadState.mediator?.refresh

    val isLoading = srcRefresh is LoadState.Loading || medRefresh is LoadState.Loading
    val refreshErrorState = (medRefresh as? LoadState.Error) ?: (srcRefresh as? LoadState.Error)
    val refreshError = refreshErrorState?.toGameListErrorType()

    val endReached = lazyPagingItems.loadState.append.endOfPaginationReached

    val showFullScreenError = !hasData && !isLoading && refreshError != null
    val showEmptyState = !hasData && !isLoading && endReached
    val showFullScreenLoading = !hasData && !showFullScreenError && !showEmptyState

    when {
        showFullScreenLoading -> {
            LoadingGrid(
                modifier = modifier.testTag(GameListTestTags.GAME_LIST_FULL_SCREEN_LOADING),
                columns = columns,
            )
        }

        showFullScreenError -> {
            onError(refreshError)
        }

        showEmptyState -> {
            onEmpty()
        }

        else -> {
            GamesGrid(
                modifier = modifier.testTag(GameListTestTags.GAME_LIST),
                lazyPagingItems = lazyPagingItems,
                columns = columns,
                onRefresh = onRefresh,
                onRetry = onRetry,
                onBookmarkClicked = onBookmarkClicked,
                onCardClicked = onCardClicked,
            )
        }
    }
}

@Composable
private fun GamesGrid(
    modifier: Modifier,
    lazyPagingItems: LazyPagingItems<GameCardUI>,
    columns: Int = 1,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onBookmarkClicked: (String) -> Unit,
    onCardClicked: (String) -> Unit,
) {
    val nextPageError = (lazyPagingItems.loadState.append as? LoadState.Error)
        ?.toGameListErrorType()
    val isNextPageLoading = lazyPagingItems.loadState.append is LoadState.Loading
    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading

    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = modifier,
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        indicator = {
            GameListPullToRefreshIndicator(
                isRefreshing = isRefreshing,
                state = pullToRefreshState,
            )
        },
    ) {
        GamesGridContent(
            lazyPagingItems = lazyPagingItems,
            columns = columns,
            isNextPageLoading = isNextPageLoading,
            nextPageError = nextPageError,
            onRetry = onRetry,
            onBookmarkClicked = onBookmarkClicked,
            onCardClicked = onCardClicked,
        )
    }
}

@Composable
private fun BoxScope.GameListPullToRefreshIndicator(
    isRefreshing: Boolean,
    state: PullToRefreshState,
) {
    PullToRefreshDefaults.Indicator(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .testTag(GameListTestTags.GAME_LIST_PULL_TO_REFRESH_INDICATOR),
        isRefreshing = isRefreshing,
        state = state,
    )
}

@Composable
private fun GamesGridContent(
    lazyPagingItems: LazyPagingItems<GameCardUI>,
    columns: Int,
    isNextPageLoading: Boolean,
    nextPageError: GameListErrorType?,
    onRetry: () -> Unit,
    onBookmarkClicked: (String) -> Unit,
    onCardClicked: (String) -> Unit,
) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        items(
            count = lazyPagingItems.itemCount,
            key = lazyPagingItems.itemKey { item -> item.id },
        ) { index ->
            val item = lazyPagingItems[index]
            if (item != null) {
                GameCard(
                    modifier = Modifier.testTag(
                        GameListTestTags.GAME_LIST_CARD_PREFIX.plus(item.id),
                    ),
                    gameData = item,
                    onBookmarkClick = { onBookmarkClicked(item.id) },
                    onCardClicked = { onCardClicked(item.id) },
                )
            }
        }
        if (isNextPageLoading) {
            items(LOADING_ITEMS_COUNT) {
                GameCardLoading(
                    modifier = Modifier.testTag(GameListTestTags.GAME_LIST_APPEND_LOADING),
                )
            }
        }
        if (nextPageError != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                GameCardError(
                    modifier = Modifier.testTag(GameListTestTags.GAME_LIST_APPEND_ERROR),
                    errorType = nextPageError,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun LoadingGrid(
    modifier: Modifier = Modifier,
    columns: Int = 1,
) {
    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        items(LOADING_ITEMS_COUNT) {
            GameCardLoading()
        }
    }
}

private fun LoadState.Error.toGameListErrorType(): GameListErrorType =
    if (error is IOException) GameListErrorType.NETWORK else GameListErrorType.OTHER

@Composable
@ThemePreview
private fun OneColumnLoadingPreview() {
    GameListPreview(
        columns = 1,
        pagingState = GameListTestData.loadingState,
    )
}

@Composable
@ThemePreview
private fun OneColumnLoadedPreview() {
    GameListPreview(
        columns = 1,
        pagingState = GameListTestData.loadedState,
    )
}

@Composable
@ThemePreview
private fun OneColumnNextPageLoadingPreview() {
    GameListPreview(
        columns = 1,
        pagingState = GameListTestData.nextPageLoadingState,
    )
}

@Composable
@ThemePreview
private fun OneColumnNextPageErrorPreview() {
    GameListPreview(
        columns = 1,
        pagingState = GameListTestData.nextPageErrorState,
    )
}

@Composable
@ThemePreview
private fun OneColumnNextPageNetworkErrorPreview() {
    GameListPreview(
        columns = 1,
        pagingState = GameListTestData.nextPageNetworkErrorState,
    )
}

@Composable
@ThemePreview
private fun OneColumnRefreshingPreview() {
    GameListPreview(
        columns = 1,
        pagingState = GameListTestData.refreshingState,
    )
}

@Composable
@TabletThemePreview
private fun FourColumnLoadingPreview() {
    GameListPreview(
        columns = 4,
        pagingState = GameListTestData.loadingState,
    )
}

@Composable
@TabletThemePreview
private fun FourColumnLoadedPreview() {
    GameListPreview(
        columns = 4,
        pagingState = GameListTestData.loadedState,
    )
}

@Composable
@TabletThemePreview
private fun FourColumnNextPageLoadingPreview() {
    GameListPreview(
        columns = 4,
        pagingState = GameListTestData.nextPageLoadingLargeState,
    )
}

@Composable
@TabletThemePreview
private fun FourColumnNextPageErrorPreview() {
    GameListPreview(
        columns = 4,
        pagingState = GameListTestData.nextPageErrorLargeState,
    )
}

@Composable
@TabletThemePreview
private fun FourColumnNextPageNetworkErrorPreview() {
    GameListPreview(
        columns = 4,
        pagingState = GameListTestData.nextPageNetworkErrorLargeState,
    )
}

@Composable
@TabletThemePreview
private fun FourColumnRefreshingPreview() {
    GameListPreview(
        columns = 4,
        pagingState = GameListTestData.refreshingState,
    )
}

@Composable
private fun GameListPreview(
    columns: Int,
    pagingState: Flow<PagingData<GameCardUI>>,
) {
    NextPlayTheme {
        val lazyPagingItems = pagingState.collectAsLazyPagingItems()
        Scaffold {
            GameList(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                lazyPagingItems = lazyPagingItems,
                columns = columns,
            )
        }
    }
}
