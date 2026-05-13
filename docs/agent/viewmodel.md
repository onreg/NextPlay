## ViewModel editing rules (presentation)

### Dependencies & inputs

- Inject all dependencies via the constructor (repositories, mappers, small platform wrappers like `UrlOpener`); avoid `Context`/Android framework types.
- ViewModels must be `internal`.
- Use assisted injection for runtime parameters (navigation arguments, deep-link params, etc.); keep them immutable (`val`).
  - Prefer the Hilt-assisted pattern: `@HiltViewModel(assistedFactory = ...)` + `@AssistedInject` constructor + `@AssistedFactory`.
  - Do not use `SavedStateHandle` unless there is an explicit, documented decision for it in ADRs.
- Keep the API surface small; expose only what the UI needs (`state`, `events`, paging flows, intent methods like `onXClicked`).

### State

- Prefer a single UI-facing `StateFlow<...>` as the screen’s source of truth.
  - Exception: keep Paging 3 streams separate. Paging is designed to be its own stream and should not be forced into the screen `state`.
- Use sealed hierarchies (`sealed interface`/`sealed class`) when UI is conditional or mutually exclusive (whole-screen states like `Loading`/`Error`/`Ready`, or smaller sections where only one variant can be shown at a time).
- For whole-screen states, keep shared UI pieces on the base type (e.g. `headerUi`).
- Keep state immutable. If the UI state is directly owned by the ViewModel, update it via small reducers (`reduce { copy(...) }`).
- If the UI state is derived (mapped from remote data + local state), update only the local state and let mapping produce the UI state (avoid “mutating” derived UI state).
- If mixing remote + local state, keep local state minimal and merge it into derived state (e.g. via `deriveState`).

### One-off events

- Use one-off events for navigation and transient UI effects (snackbar, scroll-to-top, refresh/retry triggers).
- Back events with a `Channel` and expose as `Flow` via `receiveAsFlow()` (no replay; no “event” fields inside `state`).
- Keep a single `Channel` per ViewModel for all one-off events (navigation, snackbars/toasts, transient UI effects).
- Send events only via `viewModelScope.sendEvent(channel, event)` (do not call `channel.send(...)` directly).
- Keep event types explicit and small (`sealed interface`/`sealed class`).

### Side effects & external actions

- Perform external actions behind interfaces and trigger them from intent methods.
  - Today: navigation is still being wired. Until there is a navigation interface, use one-off events for navigation.
  - Target direction: treat navigation and all other side effects as platform actions behind interfaces.
- Use a stream-driven initial load inside the ViewModel (e.g. `Flow.onStart { refresh() }` when observing data) and make the side effect obvious.
- Run async work in `viewModelScope`; avoid starting work from property initializers unless it’s an explicit, stable pattern.

### UI mapping

- Map domain models to UI models in the ViewModel layer (prefer dedicated mappers), not in Composables.
- Keep mapping pure; do not perform IO or state writes inside mapping lambdas.

### Paging

- Keep paging streams separate from screen `state`.
  - If paging output depends on local state (e.g. bookmarks), derive paging via a merge of local state + repository paging stream (e.g. via `deriveState`).
- Map `PagingData` items to UI models (`pagingData.map(mapper::map)`), and `cachedIn(viewModelScope)` for screen lifetime caching.

### Methods

- Treat functions as UI intents (`onXClicked`, `onXChanged`); they either update local state, send a one-off event, or launch async work and reflect results in state.
- Avoid “doEverything” methods; prefer small, composable intent handlers.

### Verification

After completing ViewModel changes, verify with this checklist:

- All dependencies are injected via the constructor (no `Context`/Android framework types).
- Runtime parameters use assisted injection and remain immutable (`val`).
- The UI reads from a single `StateFlow` source of truth (paging streams stay separate).
- One-off events are implemented via a single `Channel` exposed as `Flow` via `receiveAsFlow()`, and events are sent via `viewModelScope.sendEvent(...)`.
- Domain-to-UI mapping happens in the ViewModel layer (pure mapping; no IO or state writes inside mapping).
- Paging streams map items and are `cachedIn(viewModelScope)`.
