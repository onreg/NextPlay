package io.github.onreg.ui.game.list.presentation.components.list

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
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.onreg.core.ui.preview.TabletThemePreview
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.runtime.paging.AppendState
import io.github.onreg.core.ui.runtime.paging.ErrorType
import io.github.onreg.core.ui.runtime.paging.PagedListState
import io.github.onreg.core.ui.runtime.paging.resolveState
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.ui.game.list.presentation.components.card.GameCard
import io.github.onreg.ui.game.list.presentation.components.card.GameCardError
import io.github.onreg.ui.game.list.presentation.components.card.GameCardLoading
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.game.list.presentation.components.list.test.GameListTestData
import io.github.onreg.ui.game.list.presentation.components.list.test.GameListTestTags
import kotlinx.coroutines.flow.Flow

private const val LOADING_ITEMS_COUNT = 20

@Composable
public fun GameList(
    modifier: Modifier = Modifier,
    lazyPagingItems: LazyPagingItems<GameCardUI>,
    columns: Int = 1,
    onRefresh: () -> Unit = {},
    onRetry: () -> Unit = {},
    onBookmarkClicked: (Int) -> Unit = {},
    onCardClicked: (Int) -> Unit = {},
    onError: @Composable (error: ErrorType) -> Unit = {},
    onEmpty: @Composable () -> Unit = {},
) {
    when (val pagingState = lazyPagingItems.resolveState()) {
        PagedListState.Loading -> {
            LoadingGrid(
                modifier = modifier.testTag(GameListTestTags.GAME_LIST_FULL_SCREEN_LOADING),
                columns = columns,
            )
        }

        is PagedListState.Error -> {
            onError(pagingState.type)
        }

        PagedListState.Empty -> {
            onEmpty()
        }

        is PagedListState.Loaded -> {
            GamesGrid(
                modifier = modifier.testTag(GameListTestTags.GAME_LIST),
                pagingState = pagingState,
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
    pagingState: PagedListState.Loaded<GameCardUI>,
    columns: Int = 1,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onBookmarkClicked: (Int) -> Unit,
    onCardClicked: (Int) -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = modifier,
        state = pullToRefreshState,
        isRefreshing = pagingState.isRefreshing,
        onRefresh = onRefresh,
        indicator = {
            GameListPullToRefreshIndicator(
                isRefreshing = pagingState.isRefreshing,
                state = pullToRefreshState,
            )
        },
    ) {
        GamesGridContent(
            lazyPagingItems = pagingState.items,
            columns = columns,
            appendState = pagingState.appendState,
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
    appendState: AppendState,
    onRetry: () -> Unit,
    onBookmarkClicked: (Int) -> Unit,
    onCardClicked: (Int) -> Unit,
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
        if (appendState is AppendState.Loading) {
            items(LOADING_ITEMS_COUNT) {
                GameCardLoading(
                    modifier = Modifier.testTag(GameListTestTags.GAME_LIST_APPEND_LOADING),
                )
            }
        }
        if (appendState is AppendState.Error) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                GameCardError(
                    modifier = Modifier.testTag(GameListTestTags.GAME_LIST_APPEND_ERROR),
                    errorType = appendState.type,
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
