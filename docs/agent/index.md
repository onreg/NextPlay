# Agent micro-docs (index)

This folder contains short, context-specific rules. Do not read everything.
Read only documents whose triggers match your change.

## How to use

- Identify edited paths + main intent (ViewModel, tests, mapping, snapshot).
- Use the triggers table below.
- If multiple triggers match, read all matched docs (usually 1–3).

## Triggers table (paths/symbols)

- Editing `app/src/main/java/**/presentation/**/*ViewModel*.kt` or class extends `ViewModel`:
  - Read `viewmodel.md`
- Editing Compose tests (imports `androidx.compose.ui.test.*` / uses `createComposeRule` / `createAndroidComposeRule` / `ComposeContentTestRule`):
  - No micro-doc yet (we will add one later). Follow existing Compose tests patterns for now.
  - Do not apply `unit-test.md` rules here (Compose tests have different structure and helpers).
- Editing Room DAO tests in `core/db/**/src/test/**` (symbols: `Room.inMemoryDatabaseBuilder`, `AndroidJUnit4`):
  - No micro-doc yet (we will add one later). Follow existing DAO tests patterns for now.
  - Do not apply `unit-test.md` rules here (these tests are closer to integration).
- Editing unit tests (`**/src/test/**`, `**/*Test.kt`, `**/*TestDriver.kt`):
  - Read `unit-test.md`
- Before creating a new Kotlin source file:
  - Read `code-organization.md`
