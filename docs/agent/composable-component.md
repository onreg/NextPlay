## Compose component rules (reusable UI)

Applies when editing reusable Composables (not panes/screens), typically under:
- `presentation/**/components/**`
- `core/ui/**/components/**`
- component-level `*TestTags.kt` and `*TestData.kt`

### API shape

- Keep components stateless:
  - accept UI models and callbacks as parameters;
  - avoid reading ViewModels or repositories.
- Every Composable accepts `modifier: Modifier = Modifier` and applies it to the root container.
- Provide safe defaults for callbacks (`= {}`) so previews/tests can call the Composable without wiring everything.

### State branching and slots

- Prefer “branch via slots” APIs for reusable components:
  - `onError = { ... }`, `onEmpty = { ... }`, etc.
- Keep paging-specific UI components accepting `LazyPagingItems<T>` (not `Flow<PagingData<T>>`).

### Test tags

- Provide stable tags for state-driven branches and key nodes:
  - define them in `.../test/*TestTags.kt`, next to the component.
  - prefer prefix + id tags for lists (for example `CARD_PREFIX + id`).
- Prefer `Modifier.testTag(...)` over text-based selectors in tests.

### Previews

- Add previews for key visual states (loaded/empty/error/loading where applicable).
- Wrap preview content with `NextPlayTheme`.
- Keep preview setup centralized in one private preview helper function when there are multiple previews.

### Verification

After completing component changes, verify with this checklist:

- Component is stateless and takes data + callbacks as inputs.
- `modifier` is present and applied to the root.
- Stable test tags exist for important nodes and list items.
- Previews cover the main visual states and do not depend on ViewModels/Hilt.

