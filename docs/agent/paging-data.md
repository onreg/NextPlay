## Paging rules (data layer)

Applies when editing Paging pipeline in the data layer:
- `data/**/impl/**Repository*.kt`
- `*RemoteMediator*.kt`
- code using `Pager(...)`, `PagingConfig`, `RemoteMediator`, or DAO `pagingSource()`.

### Ownership boundaries

- `data/<domain>/api` exposes repository contracts returning `Flow<PagingData<ApiModel>>`.
- `data/<domain>/impl` owns:
  - `Pager(...)` setup;
  - `RemoteMediator` implementation and remote-key handling;
  - mapping between DTO/entity and `data/<domain>/api` models.
- `core/db` owns Room entities/DAOs and PagingSources.
- `core/network` owns Retrofit APIs and DTOs.

### Repository implementation pattern

- Create a `Pager(config = ..., remoteMediator = ...) { dao.pagingSource(...) }`.
- Expose `Pager(...).flow` and map items to API models:
  - `pagingData.map(entityMapper::map)` (do not map in UI).
- Keep the repository implementation free of UI formatting and Android framework types.

### `RemoteMediator` rules

- `RemoteMediator` decides which page to load based on:
  - `LoadType`;
  - `PagingState` (last loaded items);
  - remote keys stored in the database.
- On refresh:
  - clear only the tables scoped to the feed being refreshed (and their remote keys);
  - persist the new data and remote keys in one transaction.
- Use `TransactionProvider` (or `RoomDatabase.withTransaction`) to persist a refresh/append atomically.
- Parse `next` links defensively (treat malformed/absent next page as end-of-pagination).

### Paging config and factories

- Prefer providing `PagingConfig` from DI when multiple repositories share a config.
- If a mediator needs runtime parameters (for example `gameId`), expose a `fun interface` factory and create the mediator in DI.
- If a mediator does not need runtime parameters, inject it lazily via `Provider<RemoteMediator<...>>` when needed.

### Tests

- Repository tests assert Paging output by snapshotting `PagingData`:
  - `val items = repository.getX(...).asSnapshot()` then `assertEquals(expected, items)`.
- `RemoteMediator` tests:
  - stub the API response and DTO->entity mapping;
  - verify database clear/insert and remote-keys behavior;
  - assert `MediatorResult.Success(endOfPaginationReached = ...)` vs `MediatorResult.Error`.

### Verification

After completing Paging changes, verify with this checklist:

- Repository contract stays `Flow<PagingData<ApiModel>>` in `data/*/api`.
- `Pager` and `RemoteMediator` stay in `data/*/impl` (no paging creation in ViewModels/Composables).
- All refresh/append persistence is transactional and scoped (clears only what must be cleared).
- Paging output mapping is done in the data layer (DTO/entity -> API model), and is covered by snapshot-based tests.

