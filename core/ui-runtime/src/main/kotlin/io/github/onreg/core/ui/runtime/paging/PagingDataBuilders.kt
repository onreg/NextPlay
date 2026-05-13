package io.github.onreg.core.ui.runtime.paging

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public fun <T : Any> emptyPagingFlow(): Flow<PagingData<T>> = createPagingFlow(
    items = emptyList(),
    refresh = LoadState.NotLoading(endOfPaginationReached = false),
    append = LoadState.NotLoading(endOfPaginationReached = true),
)

public fun <T : Any> empyPagingFlow(): Flow<PagingData<T>> = emptyPagingFlow()

public fun <T : Any> loadingPagingFlow(
    items: List<T> = emptyList(),
): Flow<PagingData<T>> = createPagingFlow(
    items = items,
    refresh = LoadState.Loading,
    append = LoadState.NotLoading(endOfPaginationReached = false),
)

public fun <T : Any> errorPagingFlow(
    throwable: Throwable,
    items: List<T> = emptyList(),
): Flow<PagingData<T>> = createPagingFlow(
    items = items,
    refresh = LoadState.Error(throwable),
    append = LoadState.NotLoading(endOfPaginationReached = false),
)

public fun <T : Any> loadedPagingFlow(
    items: List<T>,
    endOfPaginationReached: Boolean = false,
): Flow<PagingData<T>> = createPagingFlow(
    items = items,
    refresh = LoadState.NotLoading(endOfPaginationReached = false),
    append = LoadState.NotLoading(endOfPaginationReached = endOfPaginationReached),
)

public fun <T : Any> appendLoadingPagingFlow(items: List<T>): Flow<PagingData<T>> = createPagingFlow(
    items = items,
    refresh = LoadState.NotLoading(endOfPaginationReached = false),
    append = LoadState.Loading,
)

public fun <T : Any> appendErrorPagingFlow(
    items: List<T>,
    throwable: Throwable,
): Flow<PagingData<T>> = createPagingFlow(
    items = items,
    refresh = LoadState.NotLoading(endOfPaginationReached = false),
    append = LoadState.Error(throwable),
)

private fun <T : Any> createPagingFlow(
    items: List<T>,
    refresh: LoadState,
    append: LoadState,
): Flow<PagingData<T>> = flowOf(
    PagingData.from(
        data = items,
        sourceLoadStates = LoadStates(
            refresh = refresh,
            prepend = LoadState.NotLoading(endOfPaginationReached = false),
            append = append,
        ),
    ),
)
