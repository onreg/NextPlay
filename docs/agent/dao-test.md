## Room DAO testing rules (core/db `src/test`)

Applies to Room DAO tests under `core/db/**/src/test/**`.
These tests are database integration-style tests and should not follow `unit-test.md`.

### Test setup

- Use `@RunWith(RobolectricTestRunner::class)`.
- Create DB with `Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NextPlayDatabase::class.java)`.
- Use `.allowMainThreadQueries()` in test DB setup.
- Keep DAOs as test class properties created from the same DB instance.
- Populate the database with default fixture data in `@BeforeTest` using DAO methods.
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
- Insert those default fixtures in `@BeforeTest` so each test starts from the same baseline dataset.
- When validating relationships, insert data through production DAO methods (for example `insertGamesWithPlatforms(...)`) instead of bypassing write paths.
- Add only scenario-specific data in test bodies when baseline fixtures are not enough.

### Paging assertions

- Validate paging DAO output by directly calling `pagingSource().load(PagingSource.LoadParams.Refresh(...))`.
- Assert result type is `PagingSource.LoadResult.Page`.
- Assert full page data with `assertEquals(expected, result.data)`.
- Verify ordering through expected list order (for list-position ordering behavior).

### Deletion and table-scope behavior

- Do not use raw SQL or `database.query(...)` in tests.
- For clear/delete behaviors, verify state only through DAO read methods.
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
- Default fixture data is defined as class properties and inserted in `@BeforeTest`.
- DB is closed in `@AfterTest`.
- Test body is wrapped with `runTest`.
- Paging tests call `PagingSource.LoadParams.Refresh` and assert `LoadResult.Page`.
- Tests do not use raw SQL (`SELECT ...`) or `database.query(...)`.
- Clear/delete tests verify affected and unaffected data via DAO methods and collection sizes/content.
- Update tests verify changed data is persisted and unaffected data stays unchanged, using DAO methods.
- Assertions use `kotlin.test` and compare full expected models when possible.
