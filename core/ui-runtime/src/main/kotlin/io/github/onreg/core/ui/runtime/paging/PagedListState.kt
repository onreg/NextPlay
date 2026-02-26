package io.github.onreg.core.ui.runtime.paging

import androidx.paging.compose.LazyPagingItems

public sealed interface PagedListState<out T : Any> {
    public data object Loading : PagedListState<Nothing>

    public data class Error(val type: ErrorType) : PagedListState<Nothing>

    public data object Empty : PagedListState<Nothing>

    public data class Loaded<T : Any>(
        public val items: LazyPagingItems<T>,
        public val appendState: AppendState,
        public val isRefreshing: Boolean,
    ) : PagedListState<T>
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
