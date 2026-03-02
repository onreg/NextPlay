## Unit testing rules

Applies to non-Compose unit tests under `src/test`.
Does not apply to:
- Compose tests (e.g., `createComposeRule`, `ComposeContentTestRule`)
- Room DAO tests under `core/db/**/src/test/**` (e.g., `Room.inMemoryDatabaseBuilder`, `AndroidJUnit4`)

### Test naming

- Use backticked, sentence-like names that read as a spec (e.g. `should show empty state when not loading and no cached data`).
- Prefer one behavior per test; split scenarios into separate tests instead of branching inside one test.

### Arrange / act / assert structure

- Act with a single call (or a single user intent) that triggers the behavior under test.
- Assert on outputs first (returned value, emitted state/event), then verify key interactions/side effects.
- Keep assertions focused on what the unit owns; avoid asserting through multiple collaborators.

### Test drivers (builders)

- Prefer `*TestDriver` helpers to keep tests readable and to centralize mocking/stubbing.
- Keep the driver constructor `private` and expose a fluent `Builder` that sets up only what a test needs.
- Hold mocks inside the driver and expose them as `val` so tests can `verify(...)` interactions.
- In the driver `Builder`, define mocks as basic properties (`private val repository: GameRepository = mock()`) and stub them only in dedicated builder methods (e.g. `gameEntityMapperMap(...)`, `daoObserveGame(...)`).
- Do not stub in property initializers (avoid `mock { on { ... } doReturn ... }` in the `Builder` fields). Keep all stubbing inside explicit builder methods so tests control setup.
- Name builder methods after the collaborator + behavior being stubbed.
- Build the subject-under-test lazily (`val viewModel by lazy { ... }`) to allow builders to finish stubbing first.

### Mocking & stubbing

- Use `org.mockito.kotlin` (`mock`, `stub`, `doReturn`, `doAnswer`) for collaborators.
- For suspend functions, stub with `onBlocking { ... } doReturn ...` (or `doAnswer` when return depends on builder state).
- Always use `flowOf(...)` for stubbing `Flow` values (including multiple emissions via `flowOf(first, second, ...)`).

### Coroutines & dispatchers

- Wrap coroutine/Flow tests in `runTest`.
- For ViewModel tests, install `MainDispatcherRule` so anything using `Dispatchers.Main` runs on a test dispatcher.

### Flow, Paging, and stream testing

- Use `Flow<T>.test(this)` from `testing/unit` to collect values and make assertions (`latestValue()`, `assertLatest(...)`) when a test needs to observe multiple emissions.
- Use `Flow<T>.test(this)` when a test needs to interleave actions and assertions (subscribe → assert initial → act → assert next).
- Do not use `Flow<T>.test(this)` for one-shot assertions; prefer terminal operators like `.first()` / `.single()` when you only need a single value.
- For one-shot streams, use terminal operators like `.first()`.
- Convert `Flow<PagingData<T>>` into `List<T>` with `androidx.paging.testing.asSnapshot()` and assert on the list (e.g. `assertEquals(expected, items)`).
- Convert `PagingData<T>` into `List<T>` with `io.github.onreg.testing.unit.paging.asSnapshot()` and assert on the list.

### Assertions & interaction verification

- Prefer `kotlin.test` assertions on whole values (`assertEquals`, `assertTrue`, `assertFalse`, `assertIs`) instead of checking individual properties.
- For `Result`, assert via `isSuccess` / `isFailure` and check `exceptionOrNull()` when needed.
- Use Mockito `verify(...)` for interaction verification.
- Prefer `verifyNoInteractions(mock)` over multiple negative `verify(..., never())` calls when nothing should happen.

### Verification

After completing changes (tests and any required production code), verify with this checklist:

- Tests are placed under `src/test` and are not Compose or Room DAO tests.
- Coroutines and Flow tests are wrapped in `runTest`.
- ViewModel-related tests install `MainDispatcherRule` when `Dispatchers.Main` is used.
- Tests use a `*TestDriver.Builder` (private constructor, fluent builder) for setup, and the subject is built lazily after stubbing.
- All stubbing is done via explicit builder methods (no stubbing in property initializers).
- Flow and Paging assertions use the recommended helpers (`Flow<T>.test(this)` when observing multiple emissions, snapshot helpers for paging).
