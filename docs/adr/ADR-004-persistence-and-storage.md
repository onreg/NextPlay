# ADR-004: Persistence and Storage

- Status: Accepted
- Date: 2026-01-11
- Project: NextPlay

## Context
NextPlay uses on-device persistence to:
- Provide an offline-backed, paged game list (Room + Paging 3).
- Support stable ordering for paged content (via an explicit insertion order).
- Store Paging 3 "remote keys" required by `RemoteMediator` to decide which network page to load next.

This ADR documents the current ("as-is") persistence approach in the repository. It does not propose redesigns.

## Decision
- Use a single Room database (`NextPlayDatabase`) stored in a SQLite file named "next_play.db".
- Export Room schema JSON for version control and review.
- Use destructive migrations at runtime (no explicit migration objects in current sources).
- Implement offline-backed paging using:
  - A Room `PagingSource` returning relation models (`GameWithPlatforms`).
  - A Paging 3 `RemoteMediator` that fetches from the RAWG API and persists into Room.
  - A dedicated "remote keys" table keyed by game id.
- Use `TransactionProvider` (backed by Room `withTransaction`) to make multi-DAO writes atomic from non-DAO layers (for example, `RemoteMediator`).
- No DataStore / SharedPreferences usage was found in the scanned sources (see "Open questions" for confirmation scope).

## Overview
### Persistence stack overview

```
UI/ViewModel
  |
  v
GameRepository (data/game/api)
  |
  v
Pager (Paging 3)
  |                     +----------------------+
  |                     | GameRemoteMediator   |
  |                     | - calls RAWG API     |
  |                     | - writes Room cache  |
  |                     +----------------------+
  v
Room PagingSource (GameDao.pagingSource)
  |
  v
NextPlayDatabase (Room) -> SQLite file "next_play.db"
```

## Design and conventions
### Room database design

#### - Database module(s) and ownership
- Room lives in the `:core:db` module.
- The app constructs `NextPlayDatabase` via Hilt in a singleton-scoped module and exposes DAOs for injection.
- The Room database name is a module constant and is currently "next_play.db".

#### - Entities and relations (high-level)
Current tables (Room entities):
- `games` (`GameEntity`)
  - Columns include id, title, imageUrl, releaseDate (as `Instant?`), rating, insertionOrder.
- `platforms` (`PlatformEntity`)
  - Stores platform id only.
- `game_platforms` (`GamePlatformCrossRef`)
  - Many-to-many join table (composite primary key: gameId + platformId).
  - Foreign keys to `games` and `platforms` with `onDelete = CASCADE`.
- `game_remote_keys` (`GameRemoteKeysEntity`)
  - Remote keys per game id (prevKey, nextKey).
  - Foreign key to `games` with `onDelete = CASCADE`.

Relation wrapper:
- `GameWithPlatforms` models `GameEntity` + related `PlatformEntity` via a junction on `game_platforms`.

Type conversion:
- `InstantTypeConverter` persists `Instant?` as `Long?` epoch millis.

#### - DAOs and query patterns (high-level)
- `GameDao`
  - Provides a PagingSource query ordering rows by `insertionOrder`.
  - Implements multi-table insert via a bundle (`GameInsertionBundle`) that includes:
    - `PlatformEntity` list
    - `GameEntity` list
    - `GamePlatformCrossRef` list
  - Conflict strategies:
    - Games: REPLACE
    - Cross refs: REPLACE
    - Platforms: inserted via `PlatformDao` with IGNORE
- `PlatformDao`
  - Inserts platforms with IGNORE to avoid duplicate key failures.
- `GameRemoteKeysDao`
  - Inserts remote keys with REPLACE.
  - Loads a remote key by game id.

#### - Transactions and consistency (TransactionProvider usage, @Transaction, etc.)
- `GameDao.pagingSource()` is annotated with `@Transaction` to return `GameWithPlatforms` consistently.
- `GameDao.insertGamesWithPlatforms(...)` is annotated with `@Transaction` to keep multi-table inserts atomic.
- `TransactionProvider` is provided by `:core:db` and implemented using Room `withTransaction`.
- `RemoteMediator` uses `TransactionProvider.run { ... }` to make "clear + insert + write keys" a single transaction at the consumer layer.

### Paging + offline cache flow

#### - PagingSource location
- `GameDao.pagingSource()` returns `PagingSource<Int, GameWithPlatforms>` from Room.
- Ordering is by `GameEntity.insertionOrder` (not by id).

#### - RemoteMediator responsibilities
- `GameRemoteMediator` (in `:data:game:impl`) is responsible for:
  - Choosing the network page to load (refresh vs append).
  - Calling the RAWG API with (page, pageSize).
  - Mapping DTOs -> app models -> database bundle (games, platforms, cross refs).
  - Writing the bundle and remote keys inside a transaction.

#### - Remote keys strategy (how keys are stored and used)
- Keys are stored per game id in `game_remote_keys` with columns `prevKey` and `nextKey`.
- On REFRESH:
  - Mediator starts from an initial page constant (currently 1).
- On APPEND:
  - Mediator reads the remote key for the last loaded game id and uses `nextKey` as the next page.
  - If the last item exists but its key is missing, mediator returns an error ("Missing remote key for id=...").
- PREPEND is treated as "no more pages" (mediator returns success with no prepend key).

#### - Cache invalidation / refresh (if present)
- On REFRESH, mediator clears the `games` table before inserting new results.
- Cascading foreign keys are used so related rows can be removed when parent rows are deleted:
  - Unit tests assert that clearing games removes:
    - `game_platforms` rows
    - `game_remote_keys` rows
- `platforms` are not cleared by the REFRESH path; platforms may accumulate over time (see "Open questions" about intended lifecycle).

### Key-value and lightweight storage (if present)
- No DataStore, SharedPreferences, or encrypted preferences usage was found by keyword search in the repository sources scanned for this ADR.

### Concurrency and threading model (dispatchers, IO boundaries, Flow)
- Persistence APIs are primarily suspend-based (DAO inserts/deletes) plus Paging APIs (PagingSource, RemoteMediator).
- No production dispatcher injection or explicit `Dispatchers.IO` boundaries were found in the persistence flow code:
  - Room executes queries using its configured executors (defaults apply in current builder).
  - Paging executes mediator and paging source work on its coroutine context; the code does not override dispatchers.
- Transaction boundaries:
  - DAO-level transactions use `@Transaction`.
  - Consumer-level transactions use `TransactionProvider` backed by Room `withTransaction`.

## Testing strategy
### Testing strategy for persistence
- `:core:db` has DAO-focused tests using:
  - `Room.inMemoryDatabaseBuilder(...)` and `.allowMainThreadQueries()`.
  - `runTest` from coroutines test.
  - Assertions that validate:
    - PagingSource ordering and relation materialization (`GameWithPlatforms`).
    - Cascade delete behavior for cross refs and remote keys.
- `:data:game:impl` tests cover repository, mediator, and mappers using:
  - Mockito-Kotlin mocks.
  - Test drivers/DSL classes to reduce boilerplate.

## Forbidden or avoided practices
(only if evidenced)
- Production persistence code uses Room abstractions (entities, DAOs, Room transactions) rather than direct SQLite APIs.
- Direct SQL execution (`execSQL`, raw `query`) appears in tests to validate invariants, not in production persistence wiring.

## Consequences
(positive, negative, operational impact)
Positive:
- A single database module centralizes persistence concerns and makes schema export predictable.
- Cascade foreign keys reduce manual cleanup code for dependent tables when `games` are deleted.
- Paging integration is straightforward: Room cache is the source of truth for UI paging, synchronized by `RemoteMediator`.

Negative:
- Runtime schema changes are handled by destructive migration, which can wipe local data on version bumps.
- REFRESH clears games but does not clear platforms; depending on intended lifecycle, this may leave unused platform rows.

Operational impact:
- Schema export is enabled; changes to entities should update the generated schema JSON.
- Any Room version bump without migrations will delete existing on-device DB content at runtime due to destructive migration.

## Compliance checklist
(PR and LLM)
- [ ] Keep Room database, entities, DAOs, converters, and `TransactionProvider` in `:core:db`.
- [ ] Export schema changes (Room schema JSON) when modifying entities or the database version.
- [ ] If changing the schema version, expect destructive migration behavior unless the runtime policy changes.
- [ ] Use `@Transaction` for relation-returning DAO queries and multi-step DAO writes.
- [ ] Use `TransactionProvider` for multi-DAO atomic work initiated outside DAOs (for example, in `RemoteMediator`).
- [ ] For paging flows, store remote keys in a dedicated table and keep the cache write + key write atomic.
- [ ] Add or update DAO tests for cascade behavior and query ordering when changing persistence behavior.

## Evidence index
- Room database and DI: `core/db/src/main/kotlin/io/github/onreg/core/db/{NextPlayDatabase.kt,TransactionProvider.kt,di/*.kt}`
- Schema export configuration: `core/db/{build.gradle.kts,schemas/io.github.onreg.core.db.NextPlayDatabase/*.json}`
- Room entities, relations, DAOs: `core/db/src/main/kotlin/io/github/onreg/core/db/{game/**,platform/**,common/converter/**}`
- Paging + persistence integration: `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/{GameRepositoryImpl.kt,di/GameModule.kt,paging/**,mapper/**}`
- Core DB tests: `core/db/src/test/kotlin/io/github/onreg/core/db/**`
- Data impl tests (paging/repository/mappers): `data/game/impl/src/test/kotlin/io/github/onreg/data/game/impl/**`
- Unit test helpers: `testing/unit/src/main/kotlin/io/github/onreg/testing/unit/**`
- Internal persistence guidance: `docs/instructions/core-db.md`

## Open questions
- The `GameRemoteMediator` code uses an initial page value of 1, but the unit test driver stubs API calls with page 0. Are `:data:game:impl` paging tests currently passing, and which initial page is intended?
- `PlatformDao` exists, but `:core:db` DI provides only `GameDao` and `GameRemoteKeysDao`. Is `PlatformDao` intentionally non-injectable (only used via `NextPlayDatabase.platformDao()` inside `GameDao`), or is its DI binding missing?
- The REFRESH path clears `games` (and relies on cascades for related tables) but does not clear `platforms`. Is `platforms` intended to be an append-only dimension table, or is cleanup expected elsewhere?
- Are there any persistence mechanisms outside Room (for example, preferences, DataStore, file cache) that are intentionally used but not captured by the scanned locations and keyword searches?
