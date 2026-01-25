package io.github.onreg.testing.unit.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.assertEquals

public class TestObserver<T>(
    private val testScope: TestScope,
    flow: Flow<T>,
) {
    private val values = mutableListOf<T>()
    private val job = testScope.launch {
        flow.collect { values.add(it) }
    }

    public fun assert(vararg expected: T) {
        testScope.advanceUntilIdle()
        assertEquals(expected.toList(), values)
    }

    public fun assertLatest(expected: T) {
        testScope.advanceUntilIdle()
        assertEquals(expected, values.last())
    }

    public fun latestValue(): T {
        testScope.advanceUntilIdle()
        return values.last()
    }

    public suspend fun cancel() {
        job.cancelAndJoin()
    }
}
