## Room DAO testing rules (core/db `src/test`)

Applies to Room DAO tests under `core/db/**/src/test/**`.
These tests are database integration-style tests and should not follow `non-compose-test.md`.

### Test setup

- Use `@RunWith(RobolectricTestRunner::class)`.
- Create DB with `Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NextPlayDatabase::class.java)`.
- Use `.allowMainThreadQueries()` in test DB setup.
- Keep DAOs as test class properties created from the same DB instance.
- Prefer arranging only the data needed by each test inside the test body.
- Use `@BeforeTest` only when a shared baseline meaningfully reduces duplication across most tests.
- Close DB in `@AfterTest`.

### Test naming and structure

- Use backticked, sentence-like names that read as a spec (e.g. `should show empty state when not loading and no cached data`).
- Wrap test body in `runTest`.
- Keep tests in Arrange / Act / Assert flow:
  - Arrange: insert fixtures through DAOs.
  - Act: execute DAO API under test.
  - Assert: verify returned models or affected table contents.

### Data setup

- Define default fixture objects as test class properties (games, platforms, cross-refs, list entries).
- Insert fixtures in the test body.
- Use direct `copy(...)` only for a single-level, one-off change in a test body.
- If a variation is nested or reused, extract a small semantic helper for it on the fixture or expected state (e.g. `withDrivingLicence()`, `withBookmarked()`, `withExpandedDescription()`).
- For DAO tests, do not wrap `Entity(...)` construction in generic helpers such as `game(id, title, rating)`.
- Only extract helpers for named scenarios or state changes; otherwise build entities inline in the test.
- Prefer composing small helpers over parameterizing one large helper (e.g. `gameDetails.readyState().withExpandedDescription().withBookmarked()`).
- When validating relationships, insert data through production DAO methods (for example `insertGamesWithPlatforms(...)`) instead of bypassing write paths.
- Add only scenario-specific data needed for the assertion.

### Paging assertions

- Validate paging DAO output through the shared `loadDaoRefreshPage()` test extension in `core/db/src/test/kotlin/io/github/onreg/core/db/test/DaoTestHelpers.kt`.
- Keep call sites explicit by loading the DAO paging source first, then asserting full page data with `assertEquals(expected, page.data)`.
- Verify ordering through expected list order (for list-position ordering behavior).

### Deletion and table-scope behavior

- Do not create helper/test-only DAOs just to inspect database state in tests.
- Prefer verifying behavior through existing production DAO read/write methods when they already expose the needed state.
- Use the shared `tableRowsExist()` and `tableRowsDoNotExist()` test extensions for presence/absence checks.
- Use `countTableRows()` only when the test needs the exact number of rows in a table, and assert that count directly with `assertEquals(expectedCount, database.countTableRows(TABLE_NAME))`.
- Use other raw queries via `database.query(...)` or `database.openHelper.writableDatabase` only when the production DAO does not expose a method needed to validate the behavior under test.
- For clear/delete behaviors, use production DAO methods when available; otherwise use raw queries rather than introducing extra DAOs solely for test inspection.
- Assert both:
  - data that must be cleared (returned collections are empty / size is `0`);
  - data that must remain unchanged (returned collections keep expected size/content).

### Assertions

- Use `kotlin.test` assertions (`assertEquals`, `assertTrue`).
- Prefer whole-model equality assertions over field-by-field checks.
- For update scenarios, assert that DAO reads return updated data.
- Also assert unaffected records/fields remain unchanged when update scope is partial.

### Verification

After completing DAO test changes, verify with this checklist:

- Test uses `RobolectricTestRunner` and an in-memory `Room` DB.
- Single-level fixture variations use direct `copy(...)`; nested or repeated variations use small focused helpers/extensions.
- Shared fixture objects are treated as immutable baselines and are never mutated in place.
- Test helper functions describe named scenarios or state changes, not generic `Entity(...)` factories.
- DB is closed in `@AfterTest`.
- Test body is wrapped with `runTest`.
- Paging tests use `loadDaoRefreshPage()` from `DaoTestHelpers.kt`.
- Tests do not introduce helper/test-only DAOs solely for reads, deletes, or table snapshots.
- Tests prefer existing production DAO methods for verification and use `tableRowsExist()` or `tableRowsDoNotExist()` for presence/absence checks.
- Tests use `countTableRows()` only for exact-count assertions, such as verifying that multiple unaffected parent rows remain.
- Clear/delete tests verify affected and unaffected data with production DAO reads when available, otherwise with raw queries.
- Update tests verify changed data is persisted and unaffected data stays unchanged, using DAO methods.
- Assertions use `kotlin.test` and compare full expected models when possible.
