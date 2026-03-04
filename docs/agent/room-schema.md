## Room schema rules (core/db production code)

Applies when editing Room schema code under `core/db/**/src/main/**`:
- `@Database` (`NextPlayDatabase`)
- `@Entity` and cross-ref entities
- `@Dao` interfaces/abstract classes
- database/DAO DI modules (`Room.databaseBuilder`, DAO providers)

### Entities and table/column naming

- Keep `tableName` and column names centralized in constants:
  - `@Entity(tableName = XxxEntity.TABLE_NAME)`
  - `internal companion object { const val TABLE_NAME = "..."; const val COL = "..." }`
- Prefer explicit `@ColumnInfo(name = ...)` to decouple Kotlin names from SQL names.
- Keep entity models minimal and persistence-focused (do not add UI formatting or derived fields).

### DAOs and queries

- Prefer `@Dao interface` when it’s just `@Query`/`@Insert`/`@Delete` methods.
- Use an `abstract class` DAO when:
  - it needs constructor injection of another DAO; or
  - it exposes a transactional “write bundle” method (annotate with `@Transaction`).
- For SQL, prefer raw string literals and constants for table/column references:
  - `"SELECT ${Table.TABLE_NAME}.* FROM ... ORDER BY ..."`
- Paging DAO methods return `PagingSource<..., ...>` and should be wrapped with `@Transaction` when returning relation models.

### Database registration and versioning

- When adding/removing entities or changing columns, update `NextPlayDatabase`:
  - add entity classes to the `entities = [...]` list;
  - bump the `version` number.
- Schema is exported (`exportSchema = true`), so schema JSONs must stay consistent with the version.

### DI

- `Room.databaseBuilder(...)` and DB-related singletons are provided from `core/db/.../di/**` and installed into `SingletonComponent`.
- Keep `TransactionProvider` used by data-layer mediators so paging persistence is atomic.

### Verification

After completing Room schema changes, verify with this checklist:

- Entities define `TABLE_NAME`/column constants and use explicit `@ColumnInfo(name = ...)`.
- DAOs follow interface vs abstract-class split (constructor deps/transactions only in abstract class).
- Relation-returning paging queries are `@Transaction`.
- `NextPlayDatabase` `entities` and `version` are updated for schema changes, and schema export remains enabled.

