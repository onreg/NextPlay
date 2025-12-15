## 1) core/db Module Overview

### What this module owns (and what it must NOT own)
**Owns**
- Room database definition + schema: `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`, `core/db/schemas/io.github.onreg.core.db.NextPlayDatabase/1.json`
- Persistence models: entities, junction/cross-ref entities, relation wrappers: `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GamePlatformCrossRef.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/game/model/GameWithPlatforms.kt`
- DAOs and query definitions: `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameRemoteKeysDao.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/platform/dao/PlatformDao.kt`
- TypeConverters for DB-only types: `core/db/src/main/kotlin/io/github/onreg/core/db/common/converter/InstantTypeConverter.kt`
- DI provisioning of DB/DAOs and a transaction helper: `core/db/src/main/kotlin/io/github/onreg/core/db/di/DatabaseModule.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/di/DaoModule.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/TransactionProvider.kt`

**Must NOT own**
- Repository contracts or implementations: `data/game/api/src/main/kotlin/io/github/onreg/data/game/api/GameRepository.kt`, `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/GameRepositoryImpl.kt`
- Networking (Retrofit APIs, DTOs) or “fetch” logic: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`, `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt`
- Cross-module mapping/domain rules (kept in `data/*/impl` today): `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/GameEntityMapper.kt`

### Public API surface (what other modules are intended to depend on)
- DB + DAOs are public and injectable:
  - `NextPlayDatabase`: `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
  - Provided by Hilt as a singleton: `core/db/src/main/kotlin/io/github/onreg/core/db/di/DatabaseModule.kt`
  - DAOs exposed via Hilt: `core/db/src/main/kotlin/io/github/onreg/core/db/di/DaoModule.kt`
- Transaction wrapper intended for consumers doing multi-DAO work:
  - `TransactionProvider` + `withTransaction` implementation: `core/db/src/main/kotlin/io/github/onreg/core/db/TransactionProvider.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/di/DatabaseModule.kt`
  - Used by Paging RemoteMediator to keep refresh/update atomic: `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt`
- DB models are consumed across modules (mapping layer in `data/*/impl`):
  - Entities used by mappers: `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/GameEntityMapper.kt`
  - Relation wrapper used in paging: `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/GameRepositoryImpl.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/game/model/GameWithPlatforms.kt`

### Key technologies used
- **Room (AndroidX) + KSP compiler**
  - Room annotations and DB: `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
  - KSP Room compiler + schema export args: `core/db/build.gradle.kts`
- **Room Paging integration (PagingSource)**
  - DAO exposes `PagingSource<Int, GameWithPlatforms>`: `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt`
  - Room paging dependency: `core/db/build.gradle.kts`
- **Hilt DI**
  - DB and DAO modules: `core/db/src/main/kotlin/io/github/onreg/core/db/di/DatabaseModule.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/di/DaoModule.kt`
  - Convention plugin applies Hilt + KSP: `build-logic/convention/src/main/kotlin/HiltConventionPlugin.kt`
- **Transactions**
  - `@Transaction` on relation query and multi-step insert: `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt`
  - Consumer-side transactions via `TransactionProvider` → `RoomDatabase.withTransaction`: `core/db/src/main/kotlin/io/github/onreg/core/db/di/DatabaseModule.kt`
- **TypeConverters**
  - Instant ↔ Long epoch millis: `core/db/src/main/kotlin/io/github/onreg/core/db/common/converter/InstantTypeConverter.kt`
  - Registered on DB: `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
- **java.time support via desugaring**
  - `Instant` is used in entities/converter: `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/common/converter/InstantTypeConverter.kt`
  - Desugaring enabled in library convention: `build-logic/convention/src/main/kotlin/LibraryConventionPlugin.kt`
- **Migrations policy (current)**
  - DB version is `1`: `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`, `core/db/schemas/io.github.onreg.core.db.NextPlayDatabase/1.json`
  - Runtime migration strategy is destructive: `core/db/src/main/kotlin/io/github/onreg/core/db/di/DatabaseModule.kt`

### Directory structure (what lives where)
- `core/db/src/main/kotlin/io/github/onreg/core/db/`: module root
  - `NextPlayDatabase.kt`: Room database class (entities list + DAO accessors + module-wide `@TypeConverters`)
  - `TransactionProvider.kt`: consumer-facing transaction abstraction (wraps `RoomDatabase.withTransaction`)
- `core/db/src/main/kotlin/io/github/onreg/core/db/common/converter/`: Room `@TypeConverter` implementations shared across features
- `core/db/src/main/kotlin/io/github/onreg/core/db/di/`: Hilt modules providing the database and DAOs
- `core/db/src/main/kotlin/io/github/onreg/core/db/<feature>/`: one package per persisted domain area (existing examples in repo: `game/`, `platform/`)
  - `<feature>/entity/`: tables (`@Entity`), join/cross-ref entities, plus FK/index definitions
  - `<feature>/dao/`: DAOs and query APIs (`@Dao`)
  - `<feature>/model/`: Room projection/relation models (e.g., `@Embedded` + `@Relation`) and write bundles used by DAOs
- `core/db/schemas/io.github.onreg.core.db.NextPlayDatabase/`: exported Room schema JSON snapshots (versioned)
- `core/db/src/test/kotlin/io/github/onreg/core/db/<feature>/...`: DAO tests (in-memory Room + `runTest`) colocated by feature

### Existing naming conventions and patterns (DB, tables, entities, DAOs)
- **Entities**
  - Suffix `*Entity`: `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`
  - `@Entity(tableName = X.TABLE_NAME)` with `internal companion object` constants for table/columns: `core/db/src/main/kotlin/io/github/onreg/core/db/platform/entity/PlatformEntity.kt`
  - Column names are explicitly set via `@ColumnInfo(name = CONST)` even when they match property name: `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`
- **Tables**
  - Table names are snake_case and generally plural: `games`, `platforms`, `game_platforms`, `game_remote_keys` (see entity constants and exported schema: `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`, `core/db/schemas/io.github.onreg.core.db.NextPlayDatabase/1.json`)
  - Column names are camelCase strings (e.g., `imageUrl`, `releaseDate`, `insertionOrder`): `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`
- **Indices & FKs**
  - Indices declared at entity level via `indices = [Index(...)]`: `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameRemoteKeysEntity.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GamePlatformCrossRef.kt`
  - FKs use `onDelete = CASCADE` for dependent rows (remote keys and cross-refs): `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameRemoteKeysEntity.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GamePlatformCrossRef.kt`
- **Relations**
  - Many-to-many via cross-ref + `@Relation(... associateBy = Junction(...))`: `core/db/src/main/kotlin/io/github/onreg/core/db/game/model/GameWithPlatforms.kt`
- **DAOs**
  - Suffix `*Dao`: `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameRemoteKeysDao.kt`
  - Writes are `suspend` and use explicit conflict strategies (REPLACE/IGNORE): `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameRemoteKeysDao.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/platform/dao/PlatformDao.kt`
  - Relation-returning query is `@Transaction` + returns `PagingSource`: `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt`
  - Cross-table insert is done inside a `@Transaction` method and ensures parent rows inserted before junction rows: `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt`

## 2) How to add a new database feature (step-by-step checklist)

1) **Pick the package location**
- Create feature-scoped folders mirroring existing layout: `.../<feature>/entity`, `.../<feature>/dao`, and optionally `.../<feature>/model` (see `core/db/src/main/kotlin/io/github/onreg/core/db/game/` and `core/db/src/main/kotlin/io/github/onreg/core/db/platform/`).

2) **Add the Entity (table)**
- Create `public data class <Thing>Entity` annotated with `@Entity(tableName = <Thing>Entity.TABLE_NAME)` (pattern: `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`).
- Add `@PrimaryKey` (single PK) or `primaryKeys = [...]` (composite PK) (examples: `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GamePlatformCrossRef.kt`).
- Add `@ColumnInfo(name = ...)` for each column using constants in an `internal companion object` (pattern: `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`).
- Add indices (`indices = [Index(...)]`) when you query/join on a column (examples: `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameRemoteKeysEntity.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GamePlatformCrossRef.kt`).
- Add FKs (`foreignKeys = [ForeignKey(... onDelete = CASCADE)]`) for dependent tables (examples: `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameRemoteKeysEntity.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GamePlatformCrossRef.kt`).
- If you need embedded objects / relations, create a `model/` wrapper using `@Embedded` + `@Relation` (example: `core/db/src/main/kotlin/io/github/onreg/core/db/game/model/GameWithPlatforms.kt`).

3) **Add/update TypeConverters (if needed)**
- Implement a converter class under `core/db/src/main/kotlin/io/github/onreg/core/db/common/converter/` (example: `core/db/src/main/kotlin/io/github/onreg/core/db/common/converter/InstantTypeConverter.kt`).
- Register it on the DB via `@TypeConverters(...)` (current pattern registers at DB level: `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`).

4) **Add the DAO and queries**
- Create `@Dao` interface for simple CRUD (patterns: `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameRemoteKeysDao.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/platform/dao/PlatformDao.kt`).
- Use `suspend` for write operations; pick conflict strategy explicitly (examples: REPLACE in `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameRemoteKeysDao.kt`, IGNORE in `core/db/src/main/kotlin/io/github/onreg/core/db/platform/dao/PlatformDao.kt`).
- Use `@Transaction` for:
  - relation-returning queries (example: `pagingSource()` in `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt`)
  - multi-step operations that must be atomic (example: `insertGamesWithPlatforms()` in `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt`)
- If you need Paging:
  - return a `PagingSource<Key, Value>` from the DAO (example: `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt`)
  - ensure the module has Room paging dependency (already present): `core/db/build.gradle.kts`

5) **Wire into `NextPlayDatabase`**
- Add your new entity to `@Database(entities = [...])` and bump the `version` if schema changes (DB definition: `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`).
- Add a DAO accessor `public abstract fun <thing>Dao(): <Thing>Dao` (pattern: `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`).

6) **Wire into DI**
- If another module needs to inject this DAO, add a provider in `core/db/src/main/kotlin/io/github/onreg/core/db/di/DaoModule.kt` (pattern: existing providers for `GameDao`/`GameRemoteKeysDao` in `core/db/src/main/kotlin/io/github/onreg/core/db/di/DaoModule.kt`).
- DB + `TransactionProvider` are already provided in `core/db/src/main/kotlin/io/github/onreg/core/db/di/DatabaseModule.kt`.

7) **Wire into repositories/datasources (what exists today)**
- Current pattern: repository implementations inject DAOs directly (no separate local datasource abstraction in the game flow), e.g. `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/GameRepositoryImpl.kt`.
- If you need atomic multi-table updates outside the DAO, inject `TransactionProvider` and wrap the block (pattern: `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt` + `core/db/src/main/kotlin/io/github/onreg/core/db/TransactionProvider.kt`).
- Add mapping between API models and entities in the relevant `data/*/impl` module (pattern: `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/GameEntityMapper.kt`).

8) **Handle schema changes**
- Schema export is enabled (`exportSchema = true`) and configured to write JSON files to `core/db/schemas` via KSP args (DB + Gradle: `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`, `core/db/build.gradle.kts`).
- Current migration behavior is destructive: `.fallbackToDestructiveMigration()` (so any version bump without explicit migrations will wipe local data): `core/db/src/main/kotlin/io/github/onreg/core/db/di/DatabaseModule.kt`.

9) **Add unit tests**
- Add/adjust unit tests under `core/db/src/test/kotlin/...`.

## 3) How to add tests for a new database feature

### Where tests live (and why)
- `core/db` uses JVM unit tests under `core/db/src/test/kotlin/...` with `AndroidJUnit4` + Robolectric deps (examples + deps: `core/db/src/test/kotlin/io/github/onreg/core/db/game/dao/GameDaoTest.kt`, `core/db/build.gradle.kts`).
- Rationale (as implemented): tests can use `ApplicationProvider` + in-memory Room DB without device/emulator (test setup: `core/db/src/test/kotlin/io/github/onreg/core/db/game/dao/GameRemoteKeysDaoTest.kt`, deps: `core/db/build.gradle.kts`).

### Standard test setup used here
- In-memory DB:
  - `Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NextPlayDatabase::class.java)`
- Allow main thread queries in tests:
  - `.allowMainThreadQueries()` (same files as above)
- Coroutines:
  - `kotlinx.coroutines.test.runTest { ... }` (same files as above)
- Assertions:
  - `kotlin.test` assertions (`assertEquals`, `assertTrue`, `assertNull`) (examples: `core/db/src/test/kotlin/io/github/onreg/core/db/game/dao/GameDaoTest.kt`, `core/db/src/test/kotlin/io/github/onreg/core/db/game/dao/GameRemoteKeysDaoTest.kt`)
- Query verification:
  - Direct SQL `database.query("SELECT ...")` for row counts / invariants (examples: `core/db/src/test/kotlin/io/github/onreg/core/db/platform/dao/PlatformDaoTest.kt`, `core/db/src/test/kotlin/io/github/onreg/core/db/game/dao/GameDaoTest.kt`)
- Paging query verification:
  - Call `PagingSource.load(LoadParams.Refresh(...))` and assert `LoadResult.Page` (example: `core/db/src/test/kotlin/io/github/onreg/core/db/game/dao/GameDaoTest.kt`)
- Examples: `core/db/src/test/kotlin/io/github/onreg/core/db/game/dao/GameDaoTest.kt`, `core/db/src/test/kotlin/io/github/onreg/core/db/platform/dao/PlatformDaoTest.kt`)