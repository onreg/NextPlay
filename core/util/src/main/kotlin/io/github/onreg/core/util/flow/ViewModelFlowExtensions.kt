package io.github.onreg.core.util.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DEFAULT_SUBSCRIPTION_TIMEOUT_MS = 5_000L

public fun <State, Remote, UI> StateFlow<State>.deriveState(
    scope: CoroutineScope,
    remote: Flow<Remote>,
    merge: (State, Remote) -> UI,
    initial: UI,
    started: SharingStarted = SharingStarted.WhileSubscribed(DEFAULT_SUBSCRIPTION_TIMEOUT_MS),
): StateFlow<UI> = combine(this, remote, merge)
    .stateIn(scope, started, initial)

public fun <State> MutableStateFlow<State>.reduce(block: (State) -> State) {
    update(block)
}

public fun <Event> CoroutineScope.sendEvent(channel: SendChannel<Event>, event: Event) {
    launch {
        channel.send(event)
    }
}
