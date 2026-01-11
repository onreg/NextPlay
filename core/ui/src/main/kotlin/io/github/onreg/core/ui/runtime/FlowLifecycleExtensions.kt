package io.github.onreg.core.ui.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * Collects values from a [Flow] inside a [Composable], automatically starting and stopping
 * with the provided [lifecycleOwner] and [minActiveState]. The collector suspends when the
 * lifecycle is below the given state and resumes when active again, always invoking the latest [action].
 */
@Composable
public fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
) {
    val currentAction by rememberUpdatedState(action)
    LaunchedEffect(this, lifecycleOwner, minActiveState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            this@collectWithLifecycle.collectLatest { value ->
                currentAction(value)
            }
        }
    }
}