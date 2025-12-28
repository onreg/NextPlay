package io.github.onreg.core.ui.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.onreg.core.ui.components.card.GameCard
import io.github.onreg.core.ui.components.card.GameCardError
import io.github.onreg.core.ui.components.card.GameCardLoading
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.core.ui.preview.TabletThemePreview
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import kotlinx.coroutines.flow.Flow

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
    onError: @Composable () -> Unit = {},
    onEmpty: @Composable () -> Unit = {}
) {
    val hasData = lazyPagingItems.itemCount > 0

    val isLoading = lazyPagingItems.loadState.refresh is LoadState.Loading
    val isSourceLoading = lazyPagingItems.loadState.source.refresh is LoadState.Loading
    val isError = lazyPagingItems.loadState.refresh is LoadState.Error

    val showFullScreenLoading = !hasData && (isLoading || isSourceLoading)
    val showFullScreenError = !hasData && isError
    val showEmptyState = !hasData && !isLoading && !isSourceLoading && !isError

    when {
        showFullScreenLoading -> LoadingGrid(columns = columns)
        showFullScreenError -> onError()
        showEmptyState -> onEmpty()

        else -> {
            GamesGrid(
                modifier = modifier,
                lazyPagingItems = lazyPagingItems,
                columns = columns,
                onRefresh = onRefresh,
                onRetry = onRetry,
                onBookmarkClicked = onBookmarkClicked,
                onCardClicked = onCardClicked
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
    onCardClicked: (String) -> Unit
) {
    val isNextPageError = lazyPagingItems.loadState.append is LoadState.Error
    val isNextPageLoading = lazyPagingItems.loadState.append is LoadState.Loading
    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading

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
            columns = GridCells.Fixed(columns),
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
                items(LOADING_ITEMS_COUNT) {
                    GameCardLoading()
                }
            }
            if (isNextPageError) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    GameCardError(onRetry = onRetry)
                }
            }
        }
    }
}

@Composable
private fun LoadingGrid(
    modifier: Modifier = Modifier,
    columns: Int = 1
) {
    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
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
        pagingState = GameListTestData.loadingState
    )
}

@Composable
@ThemePreview
private fun OneColumnLoadedPreview() {
    GameListPreview(
        columns = 1,
        pagingState = GameListTestData.loadedState
    )
}

@Composable
@ThemePreview
private fun OneColumnNextPageLoadingPreview() {
    GameListPreview(
        columns = 1,
        pagingState = GameListTestData.nextPageLoadingState
    )
}

@Composable
@ThemePreview
private fun OneColumnNextPageErrorPreview() {
    GameListPreview(
        columns = 1,
        pagingState = GameListTestData.nextPageErrorState
    )
}

@Composable
@ThemePreview
private fun OneColumnRefreshingPreview() {
    GameListPreview(
        columns = 1,
        pagingState = GameListTestData.refreshingState
    )
}

@Composable
@TabletThemePreview
private fun FourColumnLoadingPreview() {
    GameListPreview(
        columns = 4,
        pagingState = GameListTestData.loadingState
    )
}

@Composable
@TabletThemePreview
private fun FourColumnLoadedPreview() {
    GameListPreview(
        columns = 4,
        pagingState = GameListTestData.loadedState
    )
}

@Composable
@TabletThemePreview
private fun FourColumnNextPageLoadingPreview() {
    GameListPreview(
        columns = 4,
        pagingState = GameListTestData.nextPageLoadingLargeState
    )
}

@Composable
@TabletThemePreview
private fun FourColumnNextPageErrorPreview() {
    GameListPreview(
        columns = 4,
        pagingState = GameListTestData.nextPageErrorLargeState
    )
}

@Composable
@TabletThemePreview
private fun FourColumnRefreshingPreview() {
    GameListPreview(
        columns = 4,
        pagingState = GameListTestData.refreshingState
    )
}

@Composable
private fun GameListPreview(
    columns: Int,
    pagingState: Flow<PagingData<GameCardUI>>
) {
    NextPlayTheme {
        val lazyPagingItems = pagingState.collectAsLazyPagingItems()
        Scaffold {
            GameList(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                lazyPagingItems = lazyPagingItems,
                columns = columns
            )
        }
    }
}