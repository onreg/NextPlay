package io.github.onreg.testing.unit.paging

import androidx.annotation.VisibleForTesting
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import kotlinx.coroutines.flow.flowOf

@VisibleForTesting
public suspend fun <T : Any> PagingData<T>.asSnapshot(): List<T> {
    return flowOf(this).asSnapshot()
}
