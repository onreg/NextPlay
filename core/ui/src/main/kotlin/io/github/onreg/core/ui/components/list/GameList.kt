package io.github.onreg.core.ui.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import io.github.onreg.core.ui.R
import io.github.onreg.core.ui.components.card.GameCard
import io.github.onreg.core.ui.components.card.GameCardLoading
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.core.ui.components.state.ContentError
import io.github.onreg.core.ui.components.state.ContentErrorUI
import io.github.onreg.core.ui.theme.Spacing

private const val LOADING_ITEMS_COUNT = 20

@Composable
public fun GameList(
    modifier: Modifier = Modifier,
    lazyPagingItems: LazyPagingItems<GameCardUI>,
    columns: Int = 1,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onBookmarkClicked: (String) -> Unit,
    onCardClicked: (String) -> Unit
) {
    val hasData = lazyPagingItems.itemCount > 0

    val isLoading = lazyPagingItems.loadState.refresh is LoadState.Loading
    val isSourceLoading = lazyPagingItems.loadState.source.refresh is LoadState.Loading
    val isError = lazyPagingItems.loadState.refresh is LoadState.Error

    val showFullScreenLoading = !hasData && (isLoading || isSourceLoading)
    val showFullScreenError = !hasData && isError

    when {
        showFullScreenLoading -> LoadingGrid()
        showFullScreenError -> Error(
            modifier = modifier,
            onRetry = onRetry
        )

        hasData -> {
            GamesGrid(
                modifier = modifier,
                lazyPagingItems = lazyPagingItems,
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
                    Error(onRetry = onRetry)
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
