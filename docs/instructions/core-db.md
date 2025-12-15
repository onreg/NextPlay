## core/db: rules for implementing or changing persistence

### Boundaries
- Owns only Room persistence layer:
    - Room DB definition + schema export:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
        - `core/db/schemas/io.github.onreg.core.db.NextPlayDatabase/1.json`
    - Entities / cross-ref entities / relation models:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GamePlatformCrossRef.kt`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/model/GameWithPlatforms.kt`
    - DAOs and SQL queries:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameRemoteKeysDao.kt`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/platform/dao/PlatformDao.kt`
    - DB-only type converters:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/common/converter/InstantTypeConverter.kt`
    - DI providers for DB, DAOs, and transaction helper:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/di/DatabaseModule.kt`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/di/DaoModule.kt`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/TransactionProvider.kt`

- Must NOT own:
    - Repository contracts or implementations:
        - `data/game/api/src/main/kotlin/io/github/onreg/data/game/api/GameRepository.kt`
        - `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/GameRepositoryImpl.kt`
    - Networking, DTOs, "fetch" logic:
        - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`
        - `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt`
    - Cross-module mapping/domain rules (keep in `data/*/impl`):
        - `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/GameEntityMapper.kt`

### Directory placement
- Create new persisted area under:
    - `core/db/src/main/kotlin/io/github/onreg/core/db/<feature>/`
    - Subfolders (mirror existing examples):
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/model/`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/platform/entity/`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/platform/dao/`

### Naming and schema conventions (use existing examples as source of truth)
- Entities:
    - Suffix `*Entity`:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`
    - `@Entity(tableName = X.TABLE_NAME)` and constants in `internal companion object`:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/platform/entity/PlatformEntity.kt`
    - Use `@ColumnInfo(name = CONST)` for every column:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`
- Table names: snake_case, generally plural (verify in schema JSON):
    - `core/db/schemas/io.github.onreg.core.db.NextPlayDatabase/1.json`
- Column names: camelCase strings (verify in entity definitions):
    - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`
- Indices and foreign keys:
    - Indices declared at entity level:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameRemoteKeysEntity.kt`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GamePlatformCrossRef.kt`
    - Prefer `onDelete = CASCADE` for dependent rows:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameRemoteKeysEntity.kt`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GamePlatformCrossRef.kt`

### Entity creation or modification checklist
1. Create or update entity file under:
    - `core/db/src/main/kotlin/io/github/onreg/core/db/<feature>/entity/<Thing>Entity.kt`

2. Primary keys:
    - Single PK example:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`
    - Composite PK example:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GamePlatformCrossRef.kt`

3. Indices:
    - Add `indices = [Index(...)]` when filtering/joining on columns (examples):
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameRemoteKeysEntity.kt`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GamePlatformCrossRef.kt`

4. Foreign keys:
    - Add `foreignKeys = [ForeignKey(... onDelete = CASCADE)]` for dependent tables (examples):
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameRemoteKeysEntity.kt`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GamePlatformCrossRef.kt`

5. Relations:
    - Create relation wrapper in:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/<feature>/model/<ThingWithOtherThings>.kt`
    - Example many-to-many relation wrapper:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/model/GameWithPlatforms.kt`

### Type converters
- Add converters only for DB persistence needs.
- Put converters in:
    - `core/db/src/main/kotlin/io/github/onreg/core/db/common/converter/`
- Example converter:
    - `core/db/src/main/kotlin/io/github/onreg/core/db/common/converter/InstantTypeConverter.kt`
- Register converters at DB level:
    - `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`

### DAO rules (queries and writes)
- Create DAO under:
    - `core/db/src/main/kotlin/io/github/onreg/core/db/<feature>/dao/<Thing>Dao.kt`

- Writes:
    - Use `suspend` for inserts/updates/deletes.
    - Always specify conflict strategy explicitly:
        - REPLACE example:
            - `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameRemoteKeysDao.kt`
        - IGNORE example:
            - `core/db/src/main/kotlin/io/github/onreg/core/db/platform/dao/PlatformDao.kt`

- Transactions:
    - Use `@Transaction` for relation-returning queries:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt`
    - Use `@Transaction` for multi-step atomic writes:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt`

- Paging:
    - Return `PagingSource<Key, Value>` from DAO (example):
        - `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt`
    - Do not implement paging logic outside SQL/Room in this module.

### Wire into the database
- Update database definition:
    - `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
    - Add new entity to `@Database(entities = [...])`
    - Add DAO accessor: `abstract fun <thing>Dao(): <Thing>Dao`
    - If schema changed, bump `version` in `@Database(...)`

### Wire into DI
- DB provider:
    - `core/db/src/main/kotlin/io/github/onreg/core/db/di/DatabaseModule.kt`
- DAO providers (add your DAO here if it must be injectable):
    - `core/db/src/main/kotlin/io/github/onreg/core/db/di/DaoModule.kt`
- Transaction helper (for consumer-side multi-DAO atomic work):
    - `core/db/src/main/kotlin/io/github/onreg/core/db/TransactionProvider.kt`
    - Consumer example (outside this module):
        - `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt`

### Consumer integration rules (outside core/db)
- Repositories inject DAOs directly (current pattern example):
    - `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/GameRepositoryImpl.kt`
- Mapping between API/domain models and entities lives in `data/*/impl`:
    - `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/GameEntityMapper.kt`
- Do not add network calls or mapping rules into `core/db`.

### Schema export and migrations policy
- Schema export setup:
    - DB definition:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
    - KSP/Room args:
        - `core/db/build.gradle.kts`
    - Output directory:
        - `core/db/schemas/io.github.onreg.core.db.NextPlayDatabase/`
- Current runtime migration strategy (destructive):
    - `core/db/src/main/kotlin/io/github/onreg/core/db/di/DatabaseModule.kt`

## Tests: add or update DAO coverage
- Location:
    - `core/db/src/test/kotlin/io/github/onreg/core/db/<feature>/...`

- Existing test examples to copy:
    - `core/db/src/test/kotlin/io/github/onreg/core/db/game/dao/GameDaoTest.kt`
    - `core/db/src/test/kotlin/io/github/onreg/core/db/game/dao/GameRemoteKeysDaoTest.kt`
    - `core/db/src/test/kotlin/io/github/onreg/core/db/platform/dao/PlatformDaoTest.kt`

- Setup pattern used in this repo:
    - In-memory DB:
        - `Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NextPlayDatabase::class.java)`
    - Allow main thread queries:
        - `.allowMainThreadQueries()`
    - Coroutines:
        - `kotlinx.coroutines.test.runTest { ... }`
    - Assertions:
        - `kotlin.test` assertions (`assertEquals`, `assertTrue`, `assertNull`)
    - Optional direct SQL checks:
        - `database.query("SELECT ...")`
    - Paging verification:
        - call `PagingSource.load(LoadParams.Refresh(...))` and assert `LoadResult.Page`
        - example test:
            - `core/db/src/test/kotlin/io/github/onreg/core/db/game/dao/GameDaoTest.kt`

## Fast review checklist (before finishing)
- No repository/network/mapping logic added to `core/db`.
- Entity/table/column naming follows existing conventions (verify against):
    - `core/db/schemas/io.github.onreg.core.db.NextPlayDatabase/1.json`
- Indices/FKs added where needed; cascading behavior intentional.
- DAO writes are `suspend` with explicit conflict strategies.
- Transactional operations use `@Transaction` in DAO or `TransactionProvider` in consumers:
    - `core/db/src/main/kotlin/io/github/onreg/core/db/TransactionProvider.kt`
- `NextPlayDatabase.kt` updated:
    - `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
- If injection is needed, Hilt providers updated:
    - `core/db/src/main/kotlin/io/github/onreg/core/db/di/DaoModule.kt`
- Tests added/updated:
    - `core/db/src/test/kotlin/io/github/onreg/core/db/<feature>/...`
- Schema export updated under:
    - `core/db/schemas/io.github.onreg.core.db.NextPlayDatabase/`
