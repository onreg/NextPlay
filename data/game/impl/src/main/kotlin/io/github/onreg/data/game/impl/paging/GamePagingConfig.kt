package io.github.onreg.data.game.impl.paging

import androidx.paging.PagingConfig

// TODO: Need to move globally
public data class GamePagingConfig(
    val pageSize: Int,
    val prefetchDistance: Int,
    val initialLoadSize: Int,
    val maxSize: Int,
    val startingPage: Int = 1
) {
    public fun asPagingConfig(): PagingConfig = PagingConfig(
        pageSize = pageSize,
        prefetchDistance = prefetchDistance,
        initialLoadSize = initialLoadSize,
        maxSize = maxSize,
        enablePlaceholders = false
    )
}
