# Agent micro-docs (index)

This folder contains short, context-specific rules. Do not read everything.
Read only documents whose triggers match your change.

## How to use

- Identify edited paths + main intent (ViewModel, tests, mapping, snapshot).
- Use the triggers table below.
- If multiple triggers match, read all matched docs (usually 1–3).

## Triggers table (paths/symbols)

- Editing a production ViewModel (`**/src/main/kotlin/**/**ViewModel*.kt`, `@HiltViewModel`, or class extends `ViewModel`):
  - Read `viewmodel.md`
- Editing a screen-level Composable (Pane/Screen) under `feature/**/src/main/**/impl/pane/**` (symbols: `hiltViewModel`, `collectWithLifecycle`, Composable named `*Pane`/`*Screen`):
  - Read `composable-screen.md`
- Editing a Pane that creates an assisted ViewModel (symbols: `@AssistedInject`, `@AssistedFactory`, `hiltViewModel<..., ...Factory>`, `creationCallback`):
  - Read `assisted-viewmodel.md`
- Editing Compose tests (imports `androidx.compose.ui.test.*` / uses `createComposeRule` / `createAndroidComposeRule` / `ComposeContentTestRule`):
  - Read `compose-test.md`
- Editing Room DAO tests in `core/db/**/src/test/**` (symbols: `Room.inMemoryDatabaseBuilder`, `RobolectricTestRunner`, `AndroidJUnit4`):
  - Read `dao-test.md`
- Editing unit tests (`**/src/test/**`, `**/*Test.kt`, `**/*TestDriver.kt`):
  - Read `unit-test.md`
- Editing mappers (folders named `mapper`, files `*Mapper*.kt`):
  - Read `mapper.md`
- Editing DI modules (paths `**/di/**`, files `*Module.kt`, symbols: `@Module`, `@InstallIn`, `@Binds`, `@Provides`):
  - Read `di.md`
- Editing data-layer paging pipeline (symbols: `Pager(...)`, `RemoteMediator`, `PagingConfig`, `pagingSource()` in `data/**/impl/**`):
  - Read `paging-data.md`
- Editing Room schema production code under `core/db/**/src/main/**` (symbols: `@Database`, `@Entity`, `@Dao`, `Room.databaseBuilder`):
  - Read `room-schema.md`
- Editing app navigation host (paths `app/src/main/kotlin/**`, symbols: `NavHost`, `NavController`, `composable(...)`, `navArgument`):
  - Read `navigation.md`
- Editing reusable UI Composables (paths `presentation/**/components/**`, `core/ui/**/components/**`, symbols: `testTag`, `*TestTags`):
  - Read `composable-component.md`
- Before creating a new Kotlin source file:
  - Read `code-organization.md`
  - Read `packages.md`
