package io.github.onreg.testing.unit.flow

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle

@VisibleForTesting
public suspend fun <T> Flow<T>.test(
    testScope: TestScope,
    block: suspend TestObserver<T>.() -> Unit,
) {
    val observer = TestObserver(testScope = testScope, flow = this)
    testScope.advanceUntilIdle()
    try {
        observer.block()
    } finally {
        observer.cancel()
    }
}
