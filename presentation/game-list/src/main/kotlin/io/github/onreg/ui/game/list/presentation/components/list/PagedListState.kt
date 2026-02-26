package io.github.onreg.ui.game.list.presentation.components.list

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import java.io.IOException

public sealed interface PagedListState {
    public data object Loading : PagedListState

    public data class Error(val type: ErrorType) : PagedListState

    public data object Empty : PagedListState

    public data class Loaded(
        public val items: LazyPagingItems<GameCardUI>,
        public val appendState: AppendState,
        public val isRefreshing: Boolean
    ) : PagedListState
}

public sealed interface AppendState {
    public data object Idle : AppendState
    public data object Loading : AppendState
    public data class Error(val type: ErrorType) : AppendState
}

public enum class ErrorType {
    NETWORK,
    OTHER,
}

public fun LazyPagingItems<GameCardUI>.resolveState(): PagedListState {
    val hasData = itemCount > 0

    val srcRefresh = loadState.source.refresh
    val medRefresh = loadState.mediator?.refresh

    val isLoading = srcRefresh is LoadState.Loading || medRefresh is LoadState.Loading
    val refreshErrorState = (medRefresh as? LoadState.Error) ?: (srcRefresh as? LoadState.Error)
    val refreshError = refreshErrorState?.toErrorType()

    val endReached = loadState.append.endOfPaginationReached

    val showFullScreenError = !hasData && !isLoading && refreshError != null
    val showEmptyState = !hasData && !isLoading && endReached
    val showFullScreenLoading = !hasData && !showFullScreenError && !showEmptyState

    return when {
        showFullScreenLoading -> PagedListState.Loading
        showFullScreenError -> PagedListState.Error(refreshError)
        showEmptyState -> PagedListState.Empty
        else -> {
            val nextPageError = (loadState.append as? LoadState.Error)?.toErrorType()
            val isNextPageLoading = loadState.append is LoadState.Loading
            val isRefreshing = loadState.refresh is LoadState.Loading

            PagedListState.Loaded(
                appendState = when {
                    nextPageError != null -> AppendState.Error(nextPageError)
                    isNextPageLoading -> AppendState.Loading
                    else -> AppendState.Idle
                },
                isRefreshing = isRefreshing,
                items = this
            )
        }
    }
}

private fun LoadState.Error.toErrorType(): ErrorType =
    if (error is IOException) ErrorType.NETWORK else ErrorType.OTHER
