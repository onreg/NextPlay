## Unit testing rules

### Test naming

- Use backticked, sentence-like names that read as a spec (e.g. `state is error when refresh fails and details are absent`).
- Keep naming consistent across the codebase (imperative `should ...` is fine; outcome-focused `returns ...` / `maps ...` / `is ... when ...` is preferred).
- Prefer one behavior per test; split scenarios into separate tests instead of branching inside one test.

### Arrange / act / assert structure

- Act with a single call (or a single user intent) that triggers the behavior under test.
- Assert on outputs first (returned value, emitted state/event), then verify key interactions/side effects.
- Keep assertions focused on what the unit owns; avoid asserting through multiple collaborators.

### Test drivers (builders)

- Prefer `*TestDriver` helpers to keep tests readable and to centralize mocking/stubbing.
- Keep the driver constructor `private` and expose a fluent `Builder` that sets up only what a test needs.
- Hold mocks inside the driver and expose them as `val` so tests can `verify(...)` interactions.
- In the driver `Builder`, define mocks as properties and stub them in dedicated builder methods (e.g. `gameEntityMapperMap(...)`, `daoObserveGame(...)`).
- Name builder methods after the collaborator + behavior being stubbed.
- Build the subject-under-test lazily (`val viewModel by lazy { ... }`) to allow builders to finish stubbing first.

### Mocking & stubbing

- Use `org.mockito.kotlin` (`mock`, `stub`, `doReturn`, `doAnswer`) for collaborators.
- For suspend functions, stub with `onBlocking { ... } doReturn ...` (or `doAnswer` when return depends on builder state).
- Prefer `flowOf(...)` for simple, fixed emissions.
- Use `MutableStateFlow` only when a test needs to drive multiple emissions.

### Coroutines & dispatchers

- Wrap coroutine/Flow tests in `runTest`.
- For ViewModel tests, install `MainDispatcherRule` so anything using `Dispatchers.Main` runs on a test dispatcher.

### Flow, Paging, and stream testing

- Use `Flow<T>.test(this)` from `testing/unit` to collect values and make assertions (`latestValue()`, `assertLatest(...)`) when a test needs to observe multiple emissions.
- For one-shot streams, use terminal operators like `.first()`.
- Convert `Flow<PagingData<T>>` into `List<T>` with `asSnapshot()` and assert on the list (e.g. `assertEquals(expected, items)`).
- For mocking paging flows, use the shared paging builders from `PagingDataBuilders.kt` instead of creating PagingData manually.

### Assertions & interaction verification

- Prefer `kotlin.test` assertions whole values with (`assertEquals`, `assertTrue`, `assertFalse`, `assertIs`) instead of checking individual properties.
- For `Result`, assert via `isSuccess` / `isFailure` and check `exceptionOrNull()` when needed.
- Use Mockito `verify(...)` for interaction verification.
- Prefer `verifyNoInteractions(mock)` over multiple negative `verify(..., never())` calls when nothing should happen.
