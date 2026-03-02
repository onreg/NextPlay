## Compose UI testing rules

Applies to Robolectric Compose UI tests under `src/test` that use:
- `androidx.compose.ui.test.*`
- `createComposeRule` / `ComposeContentTestRule`

Does not apply to:
- Non-Compose unit tests under `src/test` → read `unit-test.md`
- Room DAO tests under `core/db/**/src/test/**` (these are closer to integration)

### Test setup

- Use Robolectric for JVM Compose tests: `@RunWith(RobolectricTestRunner::class)`.
- Use `@get:Rule val composeRule = createComposeRule()`.
- For instrumentation Compose tests under `src/androidTest`, use `createAndroidComposeRule` and `AndroidJUnit4` (same driver + selector rules apply).
- Keep test classes `internal`.

### Test naming

- Use backticked, sentence-like names that read as a spec (e.g. `should show empty state when not loading and no cached data`).
- Prefer one behavior per test; split scenarios instead of branching inside a test.

### Arrange / act / assert structure

- Arrange via a `*TestDriver.Builder(composeRule)` that sets up inputs and calls `composeRule.setContent { ... }` in `build()`.
- Act through a single user intent (tap, swipe, scroll, retry, etc.) exposed as a driver method.
- Assert on visible UI first, then assert callbacks/side effects (counts, last clicked id/url, etc.).

### Test drivers

- Keep the driver constructor `private` and expose a `Builder` to configure the UI state/inputs.
- In the `Builder`, define inputs as `var` properties with “empty” defaults (e.g. `emptyPagingFlow()`, `emptyFlow()`, empty state models).
  - Builder methods must reassign these `var` properties.
- Keep all `composeRule.onNode...` queries inside the driver; tests should not repeat selectors.
- Store callback effects in the driver (counters, last clicked value) and update them via lambdas passed to the Composable under test.
- Assert callback effects inside `composeRule.runOnIdle { ... }` to avoid race conditions.

### Node selection rules

- Prefer `onNodeWithTag(...)` with stable `*TestTags` constants when asserting state-driven UI branches or groups of elements (loading/error/empty/content containers).
  - If the UI has no test tags yet, add them in production code (tags are part of the component API).
- Prefer `onNodeWithContentDescription(...)` for icon-only buttons.
- Use `onNodeWithText(...)` for static UI that does not depend on state (fixed labels/headers/actions).
  - Prefer localized strings via `ApplicationProvider.getApplicationContext<Context>().getString(R.string...)` instead of hardcoded text.
  - Avoid `onNodeWithText` for dynamic content (values that change with state, loaded data, user-generated text).

### Paging, Flow, and state inputs

- For `Flow<PagingData<...>>`, prefer existing paging test helpers (e.g. `loadedPagingFlow`, `loadingPagingFlow`, `emptyPagingFlow`, `errorPagingFlow`).
- Collect the paging flow via `collectAsLazyPagingItems()` in the `build` function, pass `LazyPagingItems` into tested component.

### Scrolling and clicking

- If a node might be off-screen, call `performScrollTo()` before `performClick()`.
- When multiple nodes match, iterate deterministically (fetch nodes, try click, `composeRule.waitForIdle()`, verify expected side effect).
- Avoid sleeps/time-based waits; Compose test rules are synchronized by default.

### Assertions

- Prefer `assertIsDisplayed`, `assertIsNotDisplayed`
- Use `assertCountEquals` when asserting the number of matching nodes (e.g. for lists, multiple error messages, etc.).

### Theming

- Wrap content in the app theme (`NextPlayTheme`) only when the Composable under test depends on it (Material tokens, typography, shapes, etc.).
