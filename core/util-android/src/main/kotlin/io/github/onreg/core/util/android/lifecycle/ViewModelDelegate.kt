package io.github.onreg.core.util.android.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

public interface ViewModelDelegate<State, Event> {
    public val events: Flow<Event>

    public fun state(): StateFlow<State>

    public fun <Remote, UI> CoroutineScope.mergedState(
        remote: Flow<Remote>,
        merge: (State, Remote) -> UI,
        initial: UI,
        started: SharingStarted = SharingStarted.WhileSubscribed(5_000L),
    ): StateFlow<UI>

    public fun CoroutineScope.sendEvent(event: Event)

    public fun reduce(block: (State) -> State)
}

public class ViewModelDelegateImpl<State, Event>(initial: State) :
    ViewModelDelegate<State, Event> {
    private val stateFlow: MutableStateFlow<State> = MutableStateFlow(initial)
    private val _events = Channel<Event>()

    public override val events: Flow<Event> = _events.receiveAsFlow()

    override fun state(): StateFlow<State> = stateFlow.asStateFlow()

    override fun <Remote, UI> CoroutineScope.mergedState(
        remote: Flow<Remote>,
        merge: (State, Remote) -> UI,
        initial: UI,
        started: SharingStarted,
    ): StateFlow<UI> = combine(stateFlow, remote, merge)
        .stateIn(this, started, initial)

    override fun CoroutineScope.sendEvent(event: Event) {
        launch { _events.send(event) }
    }

    override fun reduce(block: (State) -> State) {
        stateFlow.update(block)
    }
}
