## Compose UI testing rules

### Test setup

Robolectric (`src/test`):
- Use `@RunWith(RobolectricTestRunner::class)`.
- Use `@get:Rule val composeRule = createComposeRule()`.

Instrumentation (`src/androidTest`):
- Use `createAndroidComposeRule` and `AndroidJUnit4`.

### Test naming

- Use backticked, sentence-like names that read as a spec (example: `should show empty state when not loading and no cached data`).
- Prefer one behavior per test; split scenarios instead of branching inside a single test.

### Arrange / act / assert structure

- Arrange via a `*TestDriver.Builder(composeRule)` that sets up inputs and calls `composeRule.setContent { ... }` in `build()`.
- Act through a single user intent exposed as a driver method (tap, swipe, scroll, retry, pull-to-refresh).
- Assert on visible UI first, then assert callbacks/side effects (counts, last clicked).

### Test drivers

Implementation rules:
- Keep the driver constructor `private` and expose a `Builder` to configure UI state/inputs.
- In the `Builder`, define inputs as `var` properties with baseline fixture defaults (small, realistic, and valid) so each test starts from the same baseline UI state.
  - Provide explicit builder methods to override baseline data for empty/loading/error states (for example `emptyState()`, `loading()`, `error(...)`) instead of relying on “empty-by-default” inputs.
- Builder methods should reassign these `var` properties.
- In the driver, define *all* UI nodes needed by tests as `private` properties (for example `private val retryButton = composeRule.onNodeWithTag(...)`).
  - Reuse these node properties inside driver methods like `driver.assertEmptyStateDisplayed()` and `driver.clickRetryButton()` to keep selectors centralized and consistent.

### Node selection rules

Prefer stable selectors, from most to least preferred:

1) `onNodeWithTag(...)` + stable `*TestTags` constants
   - Use tags for state-driven UI branches or containers (loading/error/empty/content).
   - If the UI has no tag yet, add it in production code.
   - Keep tags next to the component that owns them (pattern in this repo: `.../src/main/.../test/*TestTags.kt`).
2) `onNodeWithContentDescription(...)`
   - Prefer for icon-only buttons.
3) `onNodeWithText(...)`
   - Use for fixed labels/actions, and for message assertions where the *text is part of the spec* (for example: correct error message).
   - Prefer localized strings via `ApplicationProvider.getApplicationContext<Context>().getString(R.string...)` instead of hardcoded text.

### Paging, Flow, and state inputs

- For `Flow<PagingData<...>>`, prefer paging flow helpers:
  - `emptyPagingFlow`, `loadingPagingFlow`, `loadedPagingFlow`, `errorPagingFlow`, `appendLoadingPagingFlow`, `appendErrorPagingFlow`
  - Location: `core/ui-runtime/src/main/kotlin/io/github/onreg/core/ui/runtime/paging/PagingDataBuilders.kt`
- If a specific UI branch depends on `LoadStates`, it’s acceptable to build a `PagingData.from(..., sourceLoadStates = ...)` directly in the driver builder.
- Collect paging via `collectAsLazyPagingItems()` in `build()` and pass `LazyPagingItems` into the tested Composable/screen.

### Scrolling and clicking

- If a node might be off-screen, call `performScrollTo()` before `performClick()`.
- When multiple nodes match, make selection deterministic:
  - Prefer adding/using a tag that includes an id (for example, `CARD_PREFIX + id`) over relying on index order.

### Assertions

- Prefer `assertIsDisplayed` / `assertIsNotDisplayed` to check if node is visible or hidden for user.
- Use `assertCountEquals` only when asserting the number of matching nodes (for example lists or repeated items), not for negative visibility checks.

### Theming

- Wrap content in the app theme (`NextPlayTheme`) only when the Composable under test depends on it (Material tokens, typography, shapes).

### Verification

After completing changes (tests and any required production code such as test tags), verify with this checklist:

- Tests arrange via `*TestDriver.Builder(composeRule)` that calls `composeRule.setContent { ... }` in `build()`.
- Driver builder inputs have baseline fixture defaults, and tests override only scenario-specific state.
- The driver defines `private` node properties for all UI nodes required by driver actions/assertions.
- The driver methods do not contain node-finding logic.
- Tests do not call `composeRule.onNode...` directly.
- Tests perform actions only through driver methods `driver.clickRetryButton()`.
- Tests perform assertions only through driver methods: `driver.assertEmptyStateDisplayed()`.
- No sleeps/time-based waits are used.
