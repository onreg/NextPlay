## Assisted ViewModel rules (Pane wiring)

Applies when editing a pane that creates a ViewModel with runtime parameters (symbols: `@AssistedInject`, `@AssistedFactory`, `hiltViewModel<..., ...Factory>`, `creationCallback`).

### ViewModel contract

- Runtime parameters are `val` constructor params annotated with `@Assisted` (for example `@Assisted private val gameId: Int`).
- Provide an `@AssistedFactory` with a `create(...)` method matching the runtime params.
- Keep the rest of the dependencies constructor-injected as usual.

### Pane wiring

- Create the ViewModel inside the Pane using the Hilt factory overload:
  - `hiltViewModel<VM, VM.Factory>(creationCallback = { it.create(param) })`
- Treat the Pane as the owner of:
  - lifecycle-aware state collection;
  - lifecycle-aware events collection;
  - navigation/paging side effects translated from events into callbacks/actions.

### Verification

After completing assisted ViewModel wiring changes, verify with this checklist:

- Runtime params are assisted and immutable (`val`), not obtained via `SavedStateHandle` by default.
- Pane uses `hiltViewModel<VM, VM.Factory>(creationCallback = ...)` and does not manually instantiate the ViewModel.
- Side effects remain in the Pane; the Screen stays stateless.

