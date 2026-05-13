## Compose screen rules (Pane/Screen)

Applies when creating or editing a screen-level Composable (the “pane” that wires a ViewModel + the “screen” that renders UI), following `feature/game-list/.../pane/GamesPane.kt` patterns.

### Public Pane vs internal Screen

- Keep a small `public fun <Name>Pane(...)` as the entry point.
  - Accept only UI/environment inputs (e.g. `modifier`, `isLargeScreen`) and navigation callbacks (e.g. `onOpenDetails: (Id) -> Unit`).
  - Obtain the ViewModel inside the Pane via `hiltViewModel<...>()`.
- Keep the rendering Composable `internal fun <Name>PaneScreen(...)` stateless.
  - Pass in UI state and callbacks as parameters.
  - Provide safe defaults for callbacks (`= {}`) so previews/tests can call it without wiring everything.
- Every Composable should accept a `modifier: Modifier = Modifier` parameter.
  - Parent configures layout concerns via `modifier` (padding, size, alignment/placement).
  - Child applies `modifier` to the root container and avoids owning external spacing/placement.

### Split UI branches into components

- Split state-driven branches (Loading/Error/Empty/Content sections, etc.) into small components placed under `.../pane/component/`.
- Prefer one file per branch component (e.g. `LoadingComponent.kt`, `ErrorComponent.kt`) in the `component` folder.
- Each UI branch component must have its own preview(s) that render that branch in isolation.
- Keep `...Pane.kt` focused on wiring + state branching and delegate visuals to components.

### State collection and side effects

- Collect ViewModel paging/state in the Pane and pass it down:
  - Paging: `val pagingState = viewModel.pagingState.collectAsLazyPagingItems()`
  - Screen takes `LazyPagingItems<UiModel>` (not `Flow<PagingData<...>>`).
- Collect one-off events in the Pane with lifecycle awareness:
  - Use `viewModel.events.collectWithLifecycle { ... }`.
  - Handle navigation by calling the callback passed into the Pane.
  - Handle paging side effects in response to events by calling `pagingState.retry()` / `pagingState.refresh()`.
- Keep side effects in the Pane. `...Screen` should render based on inputs and invoke callbacks, but should not navigate or directly call paging actions.

### Error/empty/content structure

- Prefer rendering via reusable presentation components (example pattern: `GameList(...)`) and use slot callbacks for branches:
  - `onError = { errorType -> ErrorComponent(...) }`
  - `onEmpty = { EmptyComponent(...) }`

### Resources and theming

- Do not create a `Scaffold`/theme inside `...Screen` unless the screen owns it. Prefer letting the host provide layout chrome.
- When multiple `R` classes are used in one file, use aliased imports (`import ...R as CoreUiR`) to keep call sites explicit.

### Previews

- Previews should call `...Screen`, not the Pane (no ViewModel, no Hilt).
- Centralize preview setup in a single `private fun <Name>Preview(...)`:
  - Wrap content with `NextPlayTheme { ... }`.
  - If a `Scaffold` is needed for padding in previews, do it only in the preview helper and pass `Modifier.padding(paddingValues)` into the screen.
  - For paging previews, accept `Flow<PagingData<UiModel>>` and collect inside the preview helper via `collectAsLazyPagingItems()`.
- Provide previews for the 3 main states:
  - `FilledPreview` (content is visible)
  - `LoadingPreview` (skeleton/progress state)
  - `ErrorPreview` (error state)
- If the Pane supports large screens (e.g. `isLargeScreen` parameter), add large-screen previews for the same 3 states using `@TabletThemePreview`.
- Prefer using existing `*TestData` providers for preview inputs:
  - Feature-level: `feature/<domain>/.../impl/test/*TestData.kt`
  - Presentation/component-level: `presentation/.../components/.../test/*TestData.kt`
  - Paging flows: follow `GamesPane.kt` style by using `Flow<PagingData<...>>` from a `*TestData` object (often built with `emptyPagingFlow()`, `loadingPagingFlow()`, `loadedPagingFlow(...)`, `errorPagingFlow(...)`).

### Verification

After completing changes, verify with this checklist:

- The Pane only wires the ViewModel, collects state, and performs side effects (navigation, paging refresh/retry).
- The Screen is stateless and can be previewed/tested by passing in state + callbacks.
- State-driven branches are implemented as small components under `.../pane/component/` and are previewed.
- Previews cover Filled/Loading/Error (and large-screen variants when supported) and do not depend on Hilt/ViewModel.
