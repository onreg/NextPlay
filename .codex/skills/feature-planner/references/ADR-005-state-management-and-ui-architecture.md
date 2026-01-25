# ADR-005: State Management and UI Architecture

- Status: Accepted
- Date: 2026-01-11
- Project: NextPlay

## Context
NextPlay is a Compose-first, multi-module Android app. UI is split into:
- `:app` as the entry point and navigation host.
- `:feature:*` as screen orchestration (Compose panes and ViewModels).
- `:presentation:*` as reusable UI components, UI models, and UI mappers.
- `:core:*` as shared primitives (theme/components) and Android helpers (lifecycle/resources).
- `:data:*` as repository contracts (`api`) and implementations (`impl`) backing Paging + offline cache.

The state and event architecture is implemented in code but was not explicitly documented as an "as-is" decision.

## Decision
The current UI architecture is MVVM with a reducer-like state delegate:
- Screen state is owned by feature ViewModels and exposed as `StateFlow`.
- ViewModels handle user inputs via explicit `onX` functions.
- One-off actions are exposed as `Flow` of events backed by a `Channel`.
- Paging is produced in the data layer and transformed into UI-ready `PagingData` inside ViewModels using injected UI mappers.
- Navigation and other UI-only side effects are triggered in Composables by collecting event flows (not inside ViewModels).

## Overview
### UI stack overview (ASCII diagram)

```
User actions (Compose callbacks)
        |
        v
:feature:* Composable pane (collect state + collect events, runs side effects)
        |
        v
:feature:* ViewModel (StateFlow state + Channel-backed events)
        |
        v
:data:*:api Repository interface (Flow<PagingData<...>>)
        |
        v
:data:*:impl Repository impl (Pager + RemoteMediator + Room PagingSource)
        |
        v
:core:db (Room) <-> :core:network (Retrofit/OkHttp)
        |
        v
Back to UI:
PagingData<API model> -> (presentation mapper in VM) -> PagingData<UI model>
        |
        v
:presentation:* Composables render content based on Paging LoadState
```

## Design and conventions
### State model and ownership
- Primary state holder: feature ViewModel.
- State representation:
  - `StateFlow<State>` for "screen state" using a delegate that wraps an internal `MutableStateFlow`.
  - `StateFlow<UI>` for derived state created via `combine(...).stateIn(...)`.
  - Paging uses `Flow<PagingData<...>>` in repositories, and is exposed to UI as `StateFlow<PagingData<UI model>>` and then as `LazyPagingItems` in Compose.
- Reducer-style updates:
  - State updates are performed via a `reduce { current -> new }` function on the delegate (internally `MutableStateFlow.update`).
- Game example (ownership split):
  - A "pane state" exists as a single `data object` (no fields).
  - A separate state delegate holds "bookmark ids" as `Set<String>`, and that state is merged with the repository paging stream to produce UI paging state.

### Event model (one-off actions) and navigation triggering
- One-off event representation: `Channel<Event>` exposed as `Flow<Event>` via `receiveAsFlow()`.
- Emission: ViewModel sends events using `viewModelScope.launch { channel.send(event) }`.
- Consumption and side effects:
  - Feature Composables collect event flows using a lifecycle-aware collector and perform side effects:
    - Navigation: event -> `NavHostController.navigate(...)`.
    - Paging UI actions: event -> `LazyPagingItems.refresh()` / `LazyPagingItems.retry()`.
- Game example:
  - `onCardClicked(gameId)` emits a navigation event.
  - `onRefreshClicked()` / `onRetryClicked()` emit paging action events that are translated to Paging calls in the Composable.

### ViewModel conventions (dependencies, constructor injection, saved state, scoping)
- Construction: `@HiltViewModel` with constructor injection.
- Dependencies typically injected:
  - Repository interface from `:data:*:api`.
  - UI mapper interface from `:presentation:*` (resource-aware mapping allowed).
- Coroutine scope: `viewModelScope`.
- Saved state: no `SavedStateHandle` usage in current feature ViewModels.
- Paging scoping: repository paging flow is `cachedIn(viewModelScope)` before being exposed to UI.
- Access modifiers: ViewModels are `internal` to their feature module; panes are exposed as `public` composables.

### Compose integration (state collection, paging integration, lifecycle)
- Collecting state:
  - `collectAsStateWithLifecycle()` is used for `StateFlow` in panes.
  - A project-level `collectWithLifecycle(...)` helper collects `Flow` inside a `LaunchedEffect` using `repeatOnLifecycle(STARTED)` and `collectLatest`.
- Paging in Compose:
  - `PagingData` is collected using `collectAsLazyPagingItems()`.
  - UI reads `LazyPagingItems.loadState` (source + mediator) to decide between full-screen loading/error/empty and list rendering.
- Pull-to-refresh:
  - Material3 `PullToRefreshBox` drives refresh by calling the provided `onRefresh` callback (which ultimately triggers Paging refresh via the event bridge).
- Local UI state:
  - Some components use Compose state (e.g., `rememberPullToRefreshState`).
  - No `rememberSaveable` usage in the scanned UI sources.

### Mapping and UI models (where they live, who depends on what)
- Layered mapping is explicit and multi-step:
  - Network DTO -> API model (data impl mapper).
  - DB relation model -> API model, and API model list -> DB insertion bundle (data impl mapper).
  - API model (PagingData) -> UI model (PagingData) (presentation mapper injected into ViewModel).
  - UI primitives (e.g., chip model) live in `:core:ui`.
- Resource-aware mapping:
  - Presentation mappers can use Android resources indirectly via a `ResourcesProvider` from `:core:util-android`.
- Dependency direction (as used by UI mapping):
  - Feature ViewModel depends on presentation mapper interfaces.
  - Presentation mapper depends on data api models (and may depend on core Android utilities).

### Error handling and UI feedback (snackbars, empty/error states, retry)
- Primary error surface: Paging `LoadState.Error` (source and mediator).
- Classification: errors are mapped into a coarse UI error enum:
  - `IOException` -> "NETWORK"
  - anything else -> "OTHER"
- UI feedback patterns:
  - Full-screen loading when there is no cached data and refresh is loading (source or mediator).
  - Full-screen error when there is no cached data and refresh fails.
  - Empty state when there is no cached data, not loading, and end-of-pagination reached.
  - Inline append loading and append error item when list already has content.
  - Image loading errors are shown inline via placeholder icons inside the image component.
- Retry:
  - Retry and refresh originate from UI callbacks, go through ViewModel as events, and are executed as Paging calls in the Composable.
- Snackbars/toasts:
  - No Snackbar usage was found in the scanned UI sources; errors are rendered inline as dedicated UI components.

## Testing strategy
### Testing strategy for UI state (unit tests, test drivers, snapshot tests if present)
- Unit tests (state and mapping):
  - ViewModels are instantiated directly (no Hilt test harness).
  - Coroutines main dispatcher is overridden via a JUnit rule.
  - Flows are tested via a small test observer utility that collects values in a `TestScope`.
  - Paging data is asserted using Paging testing `asSnapshot()` (wrapped in a helper).
  - UI mappers are unit tested by mapping `PagingData` and snapshotting results.
- Compose tests:
  - Compose UI tests run with `createComposeRule()` under Robolectric.
  - Tests use "test driver" classes (DSL-style) to set content, control Paging states, and assert semantics (tags/text/content descriptions).
- Snapshot/screenshot tests:
  - No Paparazzi usage was found in tests under the scanned scopes.

## Forbidden or avoided practices
(only if evidenced)
- LiveData is not used in scanned UI sources; state is Flow/StateFlow-based.
- ViewModels do not take `NavHostController` and do not directly call Compose-only Paging APIs (`LazyPagingItems.refresh/retry`); those side effects run in Composables by collecting event flows.
- SharedFlow-based one-off event buses are not used; one-off events are Channel-backed.

## Consequences
(positive, negative, operational impact)
Positive:
- Clear state ownership: ViewModel owns state, UI observes.
- One-off actions are explicit and testable via event streams.
- Paging responsibilities are separated: data owns Pager/RemoteMediator, UI owns rendering and interaction.
- UI mapping is centralized and injectable, enabling consistent formatting and resource usage.

Negative:
- Channel-backed events are rendezvous by default; if there is no active collector, sending can suspend (within a launched coroutine).
- "Screen state" for the games pane is currently a placeholder object, while meaningful UI state is partially embedded in paging-related state, which can make state modeling less obvious.
- Navigation is triggered in the Composable layer, so navigation behavior is not unit-tested at the ViewModel level (only the event emission is).

Operational impact:
- Requires consistent lifecycle-aware collection to avoid missed/suspended event sends.
- Paging flows are cached per-ViewModel instance, aligning list stability with ViewModel lifetime.

## Compliance checklist
(PR and LLM)
- [ ] Keep screen state as `StateFlow` owned by the feature ViewModel.
- [ ] Update state via delegate `reduce { ... }` (or equivalent) rather than mutating state in UI.
- [ ] Model one-off actions as event flows (Channel-backed in current code) and handle side effects in Composables.
- [ ] Do not inject `NavHostController` into ViewModels; emit navigation events instead.
- [ ] Keep Paging creation (Pager, RemoteMediator, Room PagingSource) in `data/*/impl`.
- [ ] Use `cachedIn(viewModelScope)` for Paging flows consumed by UI.
- [ ] Map API models to UI models via injected presentation mappers (do not format resources in data layer).
- [ ] In Compose, use lifecycle-aware collection (`collectAsStateWithLifecycle` / lifecycle-aware Flow collection).
- [ ] For UI error states, keep classification consistent (IOException vs other) unless new requirements appear.
- [ ] Add or update unit tests using existing test drivers and paging snapshot helpers when changing state/event behavior.

## Evidence index
App entry and navigation host:
- `app/src/main/kotlin/io/github/onreg/nextplay/NextPlayApp.kt`
- `app/src/main/kotlin/io/github/onreg/nextplay/MainActivity.kt`

Feature game state, events, ViewModel, pane wiring:
- `feature/game/src/main/kotlin/io/github/onreg/feature/game/impl/GamesPaneViewModel.kt`
- `feature/game/src/main/kotlin/io/github/onreg/feature/game/impl/model/GamePaneState.kt`
- `feature/game/src/main/kotlin/io/github/onreg/feature/game/impl/pane/GamesPane.kt`
- `feature/game/src/main/kotlin/io/github/onreg/feature/game/impl/pane/GameDetailsPane.kt`
- `feature/game/src/main/kotlin/io/github/onreg/feature/game/impl/test/GamesPaneTestTags.kt`

State delegate and lifecycle collection helper:
- `core/util-android/src/main/kotlin/io/github/onreg/core/util/android/lifecycle/ViewModelDelegate.kt`
- `core/ui/src/main/kotlin/io/github/onreg/core/ui/runtime/FlowLifecycleExtensions.kt`

Compose primitives and theme examples:
- `core/ui/src/main/kotlin/io/github/onreg/core/ui/theme/Theme.kt`
- `core/ui/src/main/kotlin/io/github/onreg/core/ui/components/image/DynamicAsyncImage.kt`

Paging UI and error classification:
- `presentation/game/src/main/kotlin/io/github/onreg/ui/game/presentation/components/list/GameList.kt`
- `presentation/game/src/main/kotlin/io/github/onreg/ui/game/presentation/components/card/GameCard.kt`
- `presentation/game/src/main/kotlin/io/github/onreg/ui/game/presentation/components/card/GameCardError.kt`
- `presentation/game/src/main/kotlin/io/github/onreg/ui/game/presentation/components/card/model/GameCardUI.kt`
- `presentation/game/src/main/kotlin/io/github/onreg/ui/game/presentation/components/card/model/GameListErrorType.kt`

Presentation mappers and DI:
- `presentation/game/src/main/kotlin/io/github/onreg/ui/game/presentation/mapper/GameUiMapper.kt`
- `presentation/game/src/main/kotlin/io/github/onreg/ui/game/presentation/di/GamePresentationModule.kt`
- `presentation/platform/src/main/kotlin/io/github/onreg/ui/platform/mapper/PlatformUiMapper.kt`
- `presentation/platform/src/main/kotlin/io/github/onreg/ui/platform/di/PlatformPresentationModule.kt`

Resources access abstraction:
- `core/util-android/src/main/kotlin/io/github/onreg/core/util/android/resources/ResourcesProvider.kt`
- `core/util-android/src/main/kotlin/io/github/onreg/core/util/android/di/AndroidUtilsModule.kt`

Data contracts and paging pipeline ownership:
- `data/game/api/src/main/kotlin/io/github/onreg/data/game/api/GameRepository.kt`
- `data/game/api/src/main/kotlin/io/github/onreg/data/game/api/model/Game.kt`
- `data/game/api/src/main/kotlin/io/github/onreg/data/game/api/model/GamePlatform.kt`
- `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/GameRepositoryImpl.kt`
- `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/di/GameModule.kt`
- `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt`
- `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/GameDtoMapper.kt`
- `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/GameEntityMapper.kt`

Network response and API used by paging:
- `core/network/src/main/kotlin/io/github/onreg/core/network/retrofit/NetworkResponse.kt`
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`

Testing utilities and representative tests:
- `testing/unit/src/main/kotlin/io/github/onreg/testing/unit/coroutines/MainDispatcherRule.kt`
- `testing/unit/src/main/kotlin/io/github/onreg/testing/unit/flow/FlowTest.kt`
- `testing/unit/src/main/kotlin/io/github/onreg/testing/unit/flow/TestObserver.kt`
- `testing/unit/src/main/kotlin/io/github/onreg/testing/unit/paging/PagingSnapshot.kt`
- `feature/game/src/test/kotlin/io/github/onreg/feature/game/impl/GamesPaneViewModelTest.kt`
- `feature/game/src/test/kotlin/io/github/onreg/feature/game/impl/GamesPaneViewModelTestDriver.kt`
- `feature/game/src/test/kotlin/io/github/onreg/feature/game/impl/pane/GamesPaneTest.kt`
- `feature/game/src/test/kotlin/io/github/onreg/feature/game/impl/pane/GamesPaneTestDriver.kt`
- `presentation/game/src/test/kotlin/io/github/onreg/ui/game/presentation/components/list/GameListTest.kt`
- `presentation/game/src/test/kotlin/io/github/onreg/ui/game/presentation/components/list/GameListTestDriver.kt`
- `presentation/game/src/test/kotlin/io/github/onreg/ui/game/presentation/mapper/GameUiMapperTest.kt`
- `presentation/game/src/test/kotlin/io/github/onreg/ui/game/presentation/mapper/GameUiMapperTestDriver.kt`

## Open questions
None.
