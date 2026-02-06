package io.github.onreg.feature.game.details.impl

import io.github.onreg.data.details.api.RefreshResult
import io.github.onreg.testing.unit.coroutines.MainDispatcherRule
import io.github.onreg.testing.unit.flow.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class GameDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `should refresh on init`() = runTest {
        val driver = GameDetailsViewModelTestDriver.Builder().build()

        assertEquals(1, driver.repository.refreshCount)
    }

    @Test
    fun `should set error when refresh fails without cache`() = runTest {
        val driver = GameDetailsViewModelTestDriver
            .Builder()
            .refreshResult(RefreshResult.Failure(IOException("boom")))
            .build()

        driver.viewModel.state.test(this) {
            val state = latestValue()
            assertNotNull(state.initialError)
            assertFalse(state.isInitialLoading)
        }
    }

    @Test
    fun `should toggle description state`() = runTest {
        val driver = GameDetailsViewModelTestDriver.Builder().build()

        driver.viewModel.state.test(this) {
            assertFalse(latestValue().isDescriptionExpanded)
            driver.viewModel.onToggleDescription()
            assertTrue(latestValue().isDescriptionExpanded)
        }
    }
}
