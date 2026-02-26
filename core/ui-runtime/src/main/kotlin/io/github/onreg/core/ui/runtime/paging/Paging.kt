package io.github.onreg.core.ui.runtime.paging

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import java.io.IOException

public fun <T : Any> LazyPagingItems<T>.resolveState(): PagedListState<T> {
    val hasData = itemCount > 0

    val sourceRefreshState = loadState.source.refresh
    val mediatorRefreshState = loadState.mediator?.refresh

    val isLoading =
        sourceRefreshState is LoadState.Loading || mediatorRefreshState is LoadState.Loading
    val refreshError =
        ((mediatorRefreshState as? LoadState.Error) ?: (sourceRefreshState as? LoadState.Error))
            ?.toErrorType()

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
                items = this,
            )
        }
    }
}

private fun LoadState.Error.toErrorType(): ErrorType {
    return if (error is IOException) ErrorType.NETWORK else ErrorType.OTHER
}
