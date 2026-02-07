# Game Details Screen (Offline-First) Implementation Plan

## Overview
Implement an offline-first Game Details screen reachable from the existing game list, including paged Screenshots, Movies, and Series sections (Room + RemoteMediator + Paging 3), plus external intents (website, image viewer, video player), and UI behavior matching the referenced Figma designs and the API example docs.

- Relevant ADRs:
  - ADR-001 (Application Architecture): keep `:app` as composition root; features orchestrate UI/state; data is `api` contracts + `impl` IO; paging lives in data impl.
  - ADR-002 (DI): use Hilt; bind `data/*/api` interfaces in `data/*/impl` modules installed in `SingletonComponent`; keep UI mapping in `presentation/*` installed in `ViewModelComponent`.
  - ADR-003 (Networking): keep Retrofit APIs + DTOs in `:core:network`; return `NetworkResponse<T>`; map DTOs in `data/*/impl`.
  - ADR-004 (Persistence): keep Room in `:core:db`; use RemoteMediator + Room + remote keys tables; persist inside `TransactionProvider`.
  - ADR-005 (UI architecture): MVVM with `StateFlow` + Channel-backed events; navigation + external intents triggered in Composables by collecting events.
  - ADR-008 (Testing): prefer JVM unit tests; test repositories/mediators/mappers; add ViewModel tests and (where valuable) JVM Compose UI tests.
- ADR Conflicts:
  - None identified. (Note: existing `GamesPane` currently takes a `NavHostController`; this plan refactors to callback-based navigation so `:app` fully owns navigation strings, aligning better with ADR-001.)
- Assumptions:
  - Existing modules currently named `:feature:game`, `:data:game:*`, and `:presentation:game` represent the **game list** feature, and must be renamed to **game-list** first (see Step 0).
  - The existing placeholder `feature/game/.../pane/GameDetailsPane.kt` will be removed during the rename to avoid naming conflicts with the new `:feature:game-details`.
  - `released` is parsed by Moshi into `Instant?` (consistent with `GameDto.releaseDate: Instant?`).
  - Developers can be persisted as a list of names (strings) unless the UI requires richer fields.
  - “Bookmark toggle behavior” remains ViewModel-local (same as `GamesPaneViewModel` today) and is not persisted.
- Open Questions:
  - Should the details screen show per-section skeletons while online but uncached (screenshots/movies/series), or should sections appear only after the first cached page is present? (Default in this plan: show section skeletons while loading; hide only when offline error + no cached items.)

## Files to Modify
- `settings.gradle.kts` - rename existing modules to `:feature:game-list`, `:data:game-list:*`, `:presentation:game-list`, and include new modules (`:feature:game-details`, `:data:details:*`, `:data:screenshots:*`, `:data:movies:*`, `:data:series:*`).
- `app/build.gradle.kts` - add dependencies on new feature module and new data impl modules for DI wiring.
- `app/src/main/kotlin/io/github/onreg/nextplay/MainActivity.kt` - move route strings into `:app`; wire `GamesPane` + `GameDetailsPane` with callbacks; add details destination arguments.
- `core/network/src/main/kotlin/io/github/onreg/core/network/di/ApiModule.kt` - provide new Retrofit APIs (details/screenshots/movies/series).
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt` - either extend with more endpoints or split into additional `*Api` interfaces (preferred: split by responsibility).
- `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt` - add new entities + DAOs and bump Room version.
- `core/db/src/main/kotlin/io/github/onreg/core/db/di/DaoModule.kt` - provide new DAOs.
- `feature/game-list/src/main/kotlin/io/github/onreg/feature/game/list/impl/pane/GamesPane.kt` - replace `NavHostController` dependency with `onOpenGameDetails` callback.
- `feature/game-list/src/main/kotlin/io/github/onreg/feature/game/list/impl/GamesPaneViewModel.kt` - unchanged behavior; only update if needed to support callback-based navigation flow.

## New Files to Create

### New Gradle modules (wiring + source sets)
- `feature/game-details/build.gradle.kts` - `feature.convention.plugin`; depends on `:core:ui`, `:core:util-android`, `:presentation:game-list` (for shared card UI), and `data/*/api` modules used by the screen.
 - `data/details/api/build.gradle.kts` - `library.convention.plugin`; expose contracts/models for game details.
 - `data/details/impl/build.gradle.kts` - `non-ui.convention.plugin`; implement details repository (network + db).
 - `data/screenshots/api/build.gradle.kts` / `data/screenshots/impl/build.gradle.kts` - contracts + paging impl.
 - `data/movies/api/build.gradle.kts` / `data/movies/impl/build.gradle.kts` - contracts + paging impl (incl best-quality selection).
- `data/series/api/build.gradle.kts` / `data/series/impl/build.gradle.kts` - contracts + paging impl; reuse `Game` model (`:data:game-list:api`) and shared `GameEntity` storage.

### Core Network (RAWG)
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameDetailsApi.kt`
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameScreenshotsApi.kt`
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameMoviesApi.kt`
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameSeriesApi.kt`
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDetailsDto.kt`
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/ScreenshotDto.kt`
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/MovieDto.kt`

### Core DB (Room)
- `core/db/src/main/kotlin/io/github/onreg/core/db/details/entity/GameDetailsEntity.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/details/dao/GameDetailsDao.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/game/list/entity/GameListEntity.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/game/list/entity/GameListRemoteKeysEntity.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/game/list/dao/GameListDao.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/game/list/dao/GameListRemoteKeysDao.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/screenshots/entity/ScreenshotEntity.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/screenshots/entity/GameScreenshotCrossRef.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/screenshots/entity/GameScreenshotRemoteKeysEntity.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/screenshots/dao/GameScreenshotsDao.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/screenshots/dao/GameScreenshotRemoteKeysDao.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/movies/entity/MovieEntity.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/movies/entity/GameMovieCrossRef.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/movies/entity/GameMovieRemoteKeysEntity.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/movies/dao/GameMoviesDao.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/movies/dao/GameMovieRemoteKeysDao.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/series/entity/SeriesEntity.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/series/entity/SeriesRemoteKeysEntity.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/series/dao/SeriesDao.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/series/dao/SeriesRemoteKeysDao.kt`
- `core/db/schemas/io.github.onreg.core.db.NextPlayDatabase/<new-version>.json` - Room exported schema update (generated).

### Data layer (contracts + implementations)
- `data/details/api/src/main/kotlin/io/github/onreg/data/details/api/GameDetailsRepository.kt`
- `data/details/api/src/main/kotlin/io/github/onreg/data/details/api/model/GameDetails.kt`
- `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/GameDetailsRepositoryImpl.kt`
- `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/di/GameDetailsModule.kt`
- `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/mapper/GameDetailsDtoMapper.kt`
- `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/mapper/GameDetailsEntityMapper.kt`
- `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/mapper/impl/*Impl.kt`
- `data/screenshots/api/src/main/kotlin/io/github/onreg/data/screenshots/api/GameScreenshotsRepository.kt`
- `data/screenshots/api/src/main/kotlin/io/github/onreg/data/screenshots/api/model/Screenshot.kt`
- `data/screenshots/impl/src/main/kotlin/io/github/onreg/data/screenshots/impl/GameScreenshotsRepositoryImpl.kt`
- `data/screenshots/impl/src/main/kotlin/io/github/onreg/data/screenshots/impl/di/GameScreenshotsModule.kt`
- `data/screenshots/impl/src/main/kotlin/io/github/onreg/data/screenshots/impl/paging/GameScreenshotsRemoteMediator.kt`
- `data/screenshots/impl/src/main/kotlin/io/github/onreg/data/screenshots/impl/mapper/*`
- `data/movies/api/src/main/kotlin/io/github/onreg/data/movies/api/GameMoviesRepository.kt`
- `data/movies/api/src/main/kotlin/io/github/onreg/data/movies/api/model/Movie.kt`
- `data/movies/impl/src/main/kotlin/io/github/onreg/data/movies/impl/GameMoviesRepositoryImpl.kt`
- `data/movies/impl/src/main/kotlin/io/github/onreg/data/movies/impl/di/GameMoviesModule.kt`
- `data/movies/impl/src/main/kotlin/io/github/onreg/data/movies/impl/paging/GameMoviesRemoteMediator.kt`
- `data/movies/impl/src/main/kotlin/io/github/onreg/data/movies/impl/mapper/*`
- `data/series/api/src/main/kotlin/io/github/onreg/data/series/api/GameSeriesRepository.kt`
- `data/series/impl/src/main/kotlin/io/github/onreg/data/series/impl/GameSeriesRepositoryImpl.kt`
- `data/series/impl/src/main/kotlin/io/github/onreg/data/series/impl/di/GameSeriesModule.kt`
- `data/series/impl/src/main/kotlin/io/github/onreg/data/series/impl/paging/GameSeriesRemoteMediator.kt`
- `data/series/impl/src/main/kotlin/io/github/onreg/data/series/impl/mapper/*`

### Feature layer (details screen orchestration)
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPane.kt`
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/GameDetailsViewModel.kt`
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/model/GameDetailsState.kt`
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/model/GameDetailsEvent.kt`
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/test/GameDetailsTestTags.kt`
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/ui/model/*Ui.kt`
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/ui/mapper/*Mapper.kt`
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/ui/components/*`

### Tests (JVM unit tests)
- `data/details/impl/src/test/kotlin/io/github/onreg/data/details/impl/GameDetailsRepositoryImplTest.kt`
- `data/screenshots/impl/src/test/kotlin/io/github/onreg/data/screenshots/impl/paging/GameScreenshotsRemoteMediatorTest.kt`
- `data/movies/impl/src/test/kotlin/io/github/onreg/data/movies/impl/mapper/MovieQualitySelectorTest.kt`
- `data/series/impl/src/test/kotlin/io/github/onreg/data/series/impl/paging/GameSeriesRemoteMediatorTest.kt`
- `feature/game-details/src/test/kotlin/io/github/onreg/feature/game/details/impl/ui/mapper/GameDetailsUiMapperTest.kt`
- `feature/game-details/src/test/kotlin/io/github/onreg/feature/game/details/impl/GameDetailsViewModelTest.kt`
- (Optional) `feature/game-details/src/test/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneTest.kt` (Robolectric Compose UI test for description expand/collapse and section visibility rules).

## Manual Tests Summary
- MT1: Navigate from game list to details and back.
- MT2: Offline-first details: cached-first then refresh behavior.
- MT3: First-open (no cache) shows skeleton then content.
- MT4: First-open (no cache) with network failure shows full-screen error + Retry.
- MT5: Details card content and bookmark behavior.
- MT6: Website row validation + browser intent.
- MT7: Description expand/collapse toggles.
- MT8: Screenshots paging + image viewer intent.
- MT9: Movies paging + video player intent (quality selection rule).
- MT10: Developers section visibility.
- MT11: Series paging + open another details instance.
- MT12: Offline: hide empty paged sections.
- MT13: Non-goals: no Share and no Similar Games section.

## Implementation Steps

### Step 0: Rename existing “Game” feature/modules to **game-list**
### Step 0: Rename existing “Game” feature/modules to **game-list**
- Goal: Align codebase with the Task Prompt naming where “Game” == the **game list** feature, and unlock reuse of shared `Game`/`GameEntity` for Series.
- Scope:
  - Gradle modules:
    - `:feature:game` -> `:feature:game-list`
    - `:data:game:api` / `:data:game:impl` -> `:data:game-list:api` / `:data:game-list:impl`
    - `:presentation:game` -> `:presentation:game-list`
  - Kotlin packages (hyphens are not valid in packages, so use dot-separated packages):
    - `io.github.onreg.feature.game.*` -> `io.github.onreg.feature.game.list.*`
    - `io.github.onreg.data.game.*` -> `io.github.onreg.data.game.list.*`
    - `io.github.onreg.ui.game.presentation.*` -> `io.github.onreg.ui.game.list.presentation.*`
  - Core DB “game list” persistence:
    - Keep `GameEntity` as a **shared game cache** (used by game-list + series).
    - Move list-specific ordering/keys out of `core/db/game/**` into `core/db/game/list/**` (see Step 6 for schema details).
  - Cleanup:
    - Remove the placeholder `feature/game/.../pane/GameDetailsPane.kt` during the rename; the real details screen will live in `:feature:game-details`.
  - Tests:
    - Rename/update existing unit tests across `core/db`, `data/*`, `presentation/*`, and `feature/*` packages and module paths to match the new names.
- Outcome: The “game list” feature is consistently named across `feature`, `data`, `presentation`, and list-specific `core/db` code, and the shared `Game`/`GameEntity` types are positioned for reuse by Series.

### Step 1: Wire new Gradle modules into the build
- Where: `settings.gradle.kts`
- What: Include the new modules so they participate in compilation and DI graph composition:
  - `:feature:game-details`
  - `:data:details:api`, `:data:details:impl`
  - `:data:screenshots:api`, `:data:screenshots:impl`
  - `:data:movies:api`, `:data:movies:impl`
  - `:data:series:api`, `:data:series:impl`
- Why: Required to match the Task Prompt’s intended module split and ADR-001 layering.
- How:
  - Add `include(...)` entries mirroring existing modules (`:data:game-list:*`, `:presentation:*`, `:feature:*`).
- Outcome: Gradle recognizes the new modules and can compile them.

### Step 2: Add build scripts and dependencies for new modules
- Where:
  - `feature/game-details/build.gradle.kts`
  - `data/**/(api|impl)/build.gradle.kts`
- What: Create module build files using existing convention plugins and correct dependency direction.
- Why: Ensures consistent build conventions and ADR-compliant dependency graph.
- How (high-level):
  - `data/*/api`: `library.convention.plugin` and expose only contracts/models.
  - `data/*/impl`: `non-ui.convention.plugin`, depend on its `api` module + `:core:network` + `:core:db` + Paging runtime.
  - `feature/game-details`: `feature.convention.plugin`, depend on `:core:ui`, `:core:util-android`, `:presentation:game-list`, and the required `data/*/api`.
- Outcome: Modules compile with correct dependencies and namespaces.

### Step 3: Update `:app` dependencies to compose the DI graph
- Where: `app/build.gradle.kts`
- What: Add dependencies:
  - `implementation(projects.feature.gameDetails)`
  - `implementation(projects.data.details.impl)`, `implementation(projects.data.screenshots.impl)`, `implementation(projects.data.movies.impl)`, `implementation(projects.data.series.impl)`
- Why: ADR-001/ADR-002 require `:app` to include data impl modules so their Hilt modules are on the classpath.
- Outcome: Hilt can bind new repositories and the app can render the new feature.

### Step 4: Make `:app` own navigation routes and pass callbacks into features
- Where:
  - `app/src/main/kotlin/io/github/onreg/nextplay/MainActivity.kt`
  - `feature/game-list/src/main/kotlin/io/github/onreg/feature/game/list/impl/pane/GamesPane.kt`
- What:
  - Define route constants + helper in `:app` (e.g., `AppRoutes.gameDetailsRoute(gameId)`).
  - Change `GamesPane` to take `onOpenGameDetails: (String) -> Unit` instead of `NavHostController`.
  - Keep the `GamesPaneViewModel` event emission; in `GamesPane`, call `onOpenGameDetails(gameId)` when receiving `GoToDetails`.
- Why: Keeps navigation ownership in `:app` (ADR-001) and prevents cross-feature coupling.
- How:
  - Planned public signature update (no bodies shown):
    ```kotlin
    @Composable
    fun GamesPane(
        modifier: Modifier = Modifier,
        isLargeScreen: Boolean = false,
        onOpenGameDetails: (String) -> Unit,
    )
    ```
- Outcome: Game list still navigates to details, but without feature modules depending on `NavHostController`.

### Step 5: Define RAWG APIs and DTOs for details + screenshots + movies + series
- Where:
  - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/*.kt`
  - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/*.kt`
  - `core/network/src/main/kotlin/io/github/onreg/core/network/di/ApiModule.kt`
- What:
  - Add Retrofit APIs:
    - `GET /games/{id}`
    - `GET /games/{id}/screenshots?page=&page_size=`
    - `GET /games/{id}/movies?page=&page_size=`
    - `GET /games/{id}/game-series?page=&page_size=`
  - Add DTOs aligned with `docs/api/*.md` examples (source of truth):
    - `GameDetailsDto` with `id`, `name`, `background_image`, `released`, `platforms`/`parent_platforms`, `website`, `rating`, `description`, `developers`.
    - `ScreenshotDto` with `id`, `image`, `width`, `height`.
    - `MovieDto` with `id`, `name`, `preview`, `data: Map<String, String>`.
- Why: ADR-003 requires Retrofit interfaces + DTOs in `:core:network`, and Task Prompt requires these endpoints.
- How (planned signatures only):
  ```kotlin
  interface GameDetailsApi {
      suspend fun getGameDetails(id: Int): NetworkResponse<GameDetailsDto>
  }

  interface GameScreenshotsApi {
      suspend fun getScreenshots(id: Int, page: Int, pageSize: Int): NetworkResponse<PaginatedResponseDto<ScreenshotDto>>
  }
  ```
- Outcome: Data impl modules can call these APIs through injected interfaces.

### Step 6: Extend Room schema for game details + screenshots + movies + series
- Where:
  - `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
  - `core/db/src/main/kotlin/io/github/onreg/core/db/di/DaoModule.kt`
  - new `core/db/src/main/kotlin/io/github/onreg/core/db/**` packages for entities/daos
- What:
  - **Refine shared game storage** so it can be reused by both game-list and series:
    - Keep `GameEntity` as the shared cache for game card fields (id/title/image/releaseDate/rating/platforms).
    - Move list/series-specific ordering and metadata into join tables:
      - `GameListEntity` (membership + `position` + `listKey`).
      - `SeriesEntity` (membership + `position` + `parentGameId`).
    - Update remote keys to be scoped to these join tables (instead of global keys keyed by only `gameId`):
      - `GameListRemoteKeysEntity` keyed by `(listKey, gameId)`.
      - `SeriesRemoteKeysEntity` keyed by `(parentGameId, gameId)`.
  - Add `GameDetailsEntity` keyed by `gameId` for cached details.
  - Add per-collection entities + membership tables + remote keys:
    - Screenshots: `ScreenshotEntity` + cross-ref table keyed by `gameId` with `insertionOrder`; remote keys keyed by `(gameId, screenshotId)`.
    - Movies: `MovieEntity` + cross-ref table keyed by `gameId` with `insertionOrder`; remote keys keyed by `(gameId, movieId)`.
    - Series: reuse shared `GameEntity` as the card cache; store membership/order in `SeriesEntity`.
  - Add DAOs:
    - Observe details by id, insert/replace details.
    - Provide PagingSource per collection ordering by join-table insertion order.
    - Insert bundles transactionally (via `TransactionProvider` in RemoteMediators).
  - Bump `NextPlayDatabase` version and ensure schema export updates.
- Why: ADR-004 offline-first paging requires Room as source of truth + remote keys; Task Prompt requires cached-first details and paged sections with independent pagination state.
- How (example entity shapes; no method bodies):
  ```kotlin
  @Entity(tableName = "game_details")
  data class GameDetailsEntity(
      @PrimaryKey val gameId: Int,
      val name: String,
      val bannerImageUrl: String?,
      val releaseDate: Instant?,
      val websiteUrl: String?,
      val rating: Double?,
      val descriptionHtml: String?,
      val developers: List<String>,
  )
  ```
  Example join-table shapes (position + per-screen metadata lives here, not on `GameEntity`):
  ```kotlin
  @Entity(
      tableName = "game_list",
      primaryKeys = ["listKey", "gameId"],
  )
  data class GameListEntity(
      val listKey: String,
      val gameId: Int,
      val position: Int,
  )

  @Entity(
      tableName = "series",
      primaryKeys = ["parentGameId", "gameId"],
  )
  data class SeriesEntity(
      val parentGameId: Int,
      val gameId: Int,
      val position: Int,
  )
  ```
  (If persisting `List<String>` requires a converter, add a Room TypeConverter in `:core:db` and register it in `NextPlayDatabase`.)
- Outcome: Database can store and serve cached details + paged content offline-first.

### Step 6a: Refactor **game-list** paging to use `GameListEntity` (shared `GameEntity`)
- Why: With Series and Game List sharing the same underlying game JSON structure, ordering/metadata must not live on `GameEntity` itself. Keeping ordering in `GameListEntity` enables:
  - reuse of the same `GameEntity` rows for series without fighting list ordering,
  - future support for multiple lists via `listKey`,
  - correct remote keys scoping per collection.
- Where:
  - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt` (remove list-specific ordering fields)
  - `core/db/src/main/kotlin/io/github/onreg/core/db/game/list/**` (new entities/daos)
  - `data/game-list/impl/src/main/kotlin/io/github/onreg/data/game/list/impl/**` (repository + RemoteMediator updates)
  - Unit tests:
    - `core/db/src/test/**` (DAO + remote keys tests)
    - `data/game-list/impl/src/test/**` (repository + RemoteMediator tests/drivers)
- What:
  - Introduce `listKey` (start with a single constant like `"default"` for the existing list screen).
  - Update paging DAO query to order by `GameListEntity.position` (not a column on `GameEntity`).
  - Update the game-list RemoteMediator:
    - Refresh clears only the membership + remote keys for `listKey`.
    - Inserts/upserts shared `GameEntity` + platform relations.
    - Inserts membership rows into `GameListEntity` with sequential `position`.
    - Writes `GameListRemoteKeysEntity` keyed by `(listKey, gameId)`.
- Outcome: Game list paging remains functionally identical to users, but the underlying schema supports shared reuse by Series and correct per-collection ordering.

### Step 7: Implement `data/details` contracts + repository (offline-first refresh)
- Where:
  - `data/details/api/src/main/kotlin/io/github/onreg/data/details/api/GameDetailsRepository.kt`
  - `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/GameDetailsRepositoryImpl.kt`
  - `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/di/GameDetailsModule.kt`
- What:
  - Contract exposes cached stream + explicit refresh:
    ```kotlin
    interface GameDetailsRepository {
        fun observeGameDetails(gameId: Int): Flow<GameDetails?>
        suspend fun refreshGameDetails(gameId: Int)
    }
    ```
  - Impl:
    - Read cached entity from `GameDetailsDao` and map to `GameDetails` API model.
    - Refresh calls `GameDetailsApi.getGameDetails(gameId)` and persists mapped `GameDetailsEntity` via `TransactionProvider` (or direct DAO if single-table).
    - Translate `NetworkResponse.Failure` into an exception for callers (feature decides UI error behavior).
  - Hilt module binds `GameDetailsRepositoryImpl` to `GameDetailsRepository`.
- Why: Task Prompt requires cached-first details plus network refresh; ADR-001 boundaries keep IO in data impl and UI observes via contracts.
- Outcome: Feature can render cached details immediately and request refresh/retry.

### Step 8: Implement `data/screenshots` paging repository (RemoteMediator + Room)
- Where:
  - `data/screenshots/api/.../GameScreenshotsRepository.kt`
  - `data/screenshots/impl/.../GameScreenshotsRepositoryImpl.kt`
  - `data/screenshots/impl/.../paging/GameScreenshotsRemoteMediator.kt`
  - `data/screenshots/impl/.../di/GameScreenshotsModule.kt`
- What:
  - Contract:
    ```kotlin
    interface GameScreenshotsRepository {
        fun getScreenshots(gameId: Int): Flow<PagingData<Screenshot>>
    }
    ```
  - RemoteMediator mirrors the existing **game-list** RemoteMediator behavior but:
    - Keys are isolated per `gameId`.
    - Clear-on-refresh only clears rows for that `gameId` (not global table wipe).
  - DAO paging source uses join table insertion order for stable UI ordering.
- Why: Task Prompt requires offline-first pagination with same pattern/config as game list, and isolated pagination state per collection.
- Outcome: Screenshots can page, cache, and render offline-first.

### Step 9: Implement `data/movies` paging repository + best-quality URL selection
- Where:
  - `data/movies/api/.../GameMoviesRepository.kt`
  - `data/movies/api/.../model/Movie.kt`
  - `data/movies/impl/...`
- What:
  - Contract:
    ```kotlin
    interface GameMoviesRepository {
        fun getMovies(gameId: Int): Flow<PagingData<Movie>>
    }

    data class Movie(
        val id: Int,
        val name: String?,
        val previewUrl: String?,
        val videoUrl: String,
    )
    ```
  - Mapper implements the “best quality” rule:
    - Prefer `data["max"]`.
    - Else pick the highest numeric key (e.g., `"720"` over `"480"`).
- Why: Matches Task Prompt requirements and keeps URL selection logic testable in data layer (ADR-008).
- Outcome: Movies section can page offline-first and open a single best-quality URL.

### Step 10: Implement `data/series` paging repository using shared `GameEntity` + series join table
- Where:
  - `data/series/api/.../GameSeriesRepository.kt`
  - `data/series/impl/...`
  - `core/db/.../series/*`
- What:
  - Contract returns `PagingData<Game>` (reusing the existing card model):
    ```kotlin
    interface GameSeriesRepository {
        fun getSeries(parentGameId: Int): Flow<PagingData<Game>>
    }
    ```
  - RemoteMediator:
    - Calls `/games/{id}/game-series`.
    - Maps results to the existing `GameEntity` bundle (games + platforms + cross refs) and inserts/updates shared `games` table.
    - Writes membership/order into `SeriesEntity` keyed by `parentGameId`.
    - Writes remote keys keyed by `(parentGameId, gameId)` so series paging is independent per parent.
    - On refresh, clear only series membership/keys for that `parentGameId` (do not clear global `games` table).
- Why: Task Prompt requires reuse of `GameEntity` and a series membership join table keyed by parent `gameId`.
- Outcome: Series carousel can page offline-first and reuse existing game card UI/mapping.

### Step 11: Keep details UI mapping and UI models inside `:feature:game-details`
- Why: Per ADR-001, `:presentation:*` must only contain reusable components. Game details UI models/mappers are screen-specific and unlikely to be reused without substantial coupling to this feature.
- What:
  - Define feature-local UI models (e.g., `GameDetailsUi`, `DescriptionUi`) and a feature-local mapper that:
    - Formats release date for display.
    - Formats rating to one decimal.
    - Determines whether website row is visible based on URL validity.
    - Reuses `PlatformUiMapper` from `:presentation:platform` where applicable.
- Outcome: UI mapping stays close to the screen that owns it, and `:presentation:*` remains strictly reusable.

### Step 12: Build the details screen composables inside `:feature:game-details` (reuse-only from presentation)
- What:
  - Implement screen composables in the feature module and reuse only clearly reusable UI from:
    - `:presentation:game-list` (existing `GameCard` for series items to ensure identical appearance).
    - `:presentation:platform` and `:core:ui` primitives.
  - If a new component is clearly reusable across multiple features (e.g., a generic expandable text), place it in `:core:ui` rather than creating a `:presentation:game-details`.
- Outcome: Details UI is implemented without introducing a feature-specific “presentation” module.

### Step 13: Implement `feature/game-details` ViewModel state + events (offline-first UX)
- Where:
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/GameDetailsViewModel.kt`
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/model/GameDetailsState.kt`
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/model/GameDetailsEvent.kt`
- What:
  - State includes:
    - `gameId`
    - `isBookmarked` (local only, consistent with game list behavior)
    - `isDescriptionExpanded`
    - cached details stream mapped to UI model
    - `initialLoadState` (loading/error) for “no cache yet” path
  - Events include:
    - `GoBack`
    - `OpenUrl(url: String)`
    - `OpenImage(url: String)`
    - `OpenVideo(url: String)`
    - `OpenGameDetails(gameId: Int)` (for series tap)
  - On init/open:
    - Collect `observeGameDetails(gameId)`; if null, keep skeleton visible until non-null.
    - Trigger `refreshGameDetails(gameId)` after first collection starts (always refresh).
    - If refresh fails and cached is still null: expose full-screen error + Retry action.
  - Paging flows:
    - `screenshots = screenshotsRepository.getScreenshots(gameId).cachedIn(viewModelScope)`
    - `movies = moviesRepository.getMovies(gameId).cachedIn(viewModelScope)`
    - `series = seriesRepository.getSeries(gameId).cachedIn(viewModelScope)`
- Why: Implements required offline-first behavior and keeps side effects as events (ADR-005).
- Outcome: Screen state supports skeleton, cached content, retry, and external intent actions.

### Step 14: Implement `feature/game-details` Composable pane and connect navigation + intents
- Where: `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPane.kt`
- What:
  - Public API accepts callbacks owned by `:app`:
    ```kotlin
    @Composable
    fun GameDetailsPane(
        gameId: Int,
        modifier: Modifier = Modifier,
        onGoBack: () -> Unit,
        onOpenGameDetails: (Int) -> Unit,
    )
    ```
  - Collect ViewModel state and render:
    - Full-screen skeleton when no cached details and still loading.
    - Full-screen error + Retry when no cached details and refresh fails.
    - Content list with required sections in order.
  - Collect events and perform side effects:
    - `GoBack` -> `onGoBack()`
    - `OpenGameDetails(id)` -> `onOpenGameDetails(id)`
    - `OpenUrl/OpenImage/OpenVideo` -> launch system intents via `LocalContext`.
  - Section visibility rule:
    - For screenshots/movies/series: if there are no cached items and refresh fails with an `IOException`, hide the entire section (including header).
- Why: Meets Task Prompt responsibilities (feature owns intent launching) while keeping routing in `:app`.
- Outcome: Details screen is functional end-to-end from list navigation.

### Step 15: Add/adjust automated tests per ADR-008
- Why: Feature changes include new repositories/mediators/mappers, a shared-game schema refinement, and significant UI behavior.
- What to test:
  - Rename/update existing tests for **game-list** modules (`feature/data/presentation/core-db`) so they still cover the same behavior after the schema refactor:
    - `core/db`: paging query ordering via `GameListEntity.position` and remote keys keyed by `(listKey, gameId)`
    - `data/game-list/impl`: RemoteMediator refresh/append behavior using `GameListEntity` + `GameListRemoteKeysEntity`
    - `presentation/game-list`: `GameUiMapper` and list component tests still pass under the new package/module names
    - `feature/game-list`: ViewModel + pane tests still validate paging events and navigation event emission
  - `data/details/impl`: cached-first flow + refresh error propagation.
  - `data/screenshots/impl`: RemoteMediator paging key isolation per `gameId`.
  - `data/series/impl`: RemoteMediator key isolation per `parentGameId` and membership ordering via `SeriesEntity.position`.
  - `data/movies/impl`: best-quality selection logic.
  - `feature/game-details`: UI mapper URL validity + formatting, and ViewModel state transitions for skeleton/error/cached content and events for intents/navigation.
- Outcome: Core behavior is covered by JVM unit tests.

### Step 16: Prepare design references (Figma)
- Why: Ensure verification compares against intended designs (Task Prompt includes Figma links).
- How:
  - Use `mcp__figma` to fetch the frames:
    - Dark theme: `nov1xXgQhkBdxSAiZA3x2E` node `2:3111`
    - Light theme: `nov1xXgQhkBdxSAiZA3x2E` node `2:3737`
  - Capture screenshots/spec notes for:
    - Header layout (back + title)
    - Banner aspect ratio and cropping
    - Details card rows and spacing
    - Section headers and carousel item sizing
    - Loading and error states
- Outcome: A checklist of UI expectations is available for final UI verification and manual testing steps.

## Design Verification (Figma)
- Frames:
  - Dark theme details: file `nov1xXgQhkBdxSAiZA3x2E`, node `2:3111`
  - Light theme details: file `nov1xXgQhkBdxSAiZA3x2E`, node `2:3737`
- Verify key elements and states:
  - Header: back button position and title truncation behavior.
  - Banner: height, content scale, and placeholder behavior.
  - Details card:
    - Release date formatting and placement.
    - Platforms row uses the same icon treatment as the game list cards.
    - Website row visibility and chevron/iconography (if present).
    - Bookmark toggle visuals match list behavior.
    - Rating formatting (one decimal).
  - Description: collapsed to 5 lines with “Read more/less” styling.
  - Paged sections:
    - Section header typography and spacing.
    - Carousel item sizes, spacing, and scroll behavior.
  - Offline/no-cache states:
    - Full-screen skeleton when details not cached yet.
    - Full-screen error state with Retry when initial fetch fails.

## Manual Testing (mcp__mobile-mcp)
- Prerequisites:
  - A running Android emulator with Google APIs (any supported device).
  - Build variant: `debug`.
  - App package: `io.github.onreg.nextplay`.
  - RAWG API key configured for the build environment.
- Navigation starting point:
  - Launch app to the game list screen (start destination).
- Notes:
  - Use `mcp__mobile-mcp` `mobile_list_elements_on_screen` when an element label is unknown.
  - Take screenshots at key points using `mobile_take_screenshot` (or `mobile_save_screenshot` if needed).
  - For offline/online transitions, use `mcp__mobile-mcp` to launch the Android Settings app and toggle Airplane mode:
    - `mobile_launch_app` with `packageName = "com.android.settings"`
    - Use `mobile_list_elements_on_screen` + taps to find and toggle “Airplane mode”.

## Final Verification Steps (Required)

### Step N-4: Static analysis (reports-driven)
- Command to run:
  ```bash
  ./gradlew codeQuality
  ```
- Fix: Apply fixes based on `build/reports/detekt/detekt.txt`, `build/reports/ktlint/ktlint.txt`, and `build/reports/lint/lint.txt`, then re-run until clean.

### Step N-3: Unit tests (reports-driven)
- Command to run:
  ```bash
  ./gradlew testDebugUnitTest
  ```
- Fix: Apply fixes based on test reports, then re-run until green.

### Step N-2: Build an installable artifact
- Command to run:
  ```bash
  ./gradlew :app:assembleDebug
  ```
- Outcome: APK is available under `app/build/outputs/apk/debug/` (confirm exact filename in the build output).

### Step N-1: Install + launch on emulator
- How:
  - Use `mcp__mobile-mcp` `mobile_list_available_devices` to select an emulator device id.
  - Install:
    - Option A (Gradle install task):
      - Run:
        ```bash
        ./gradlew :app:installDebug
        ```
    - Option B (direct install):
      - Use `mcp__mobile-mcp` `mobile_install_app` with the built APK path.
  - Launch:
    - Use `mcp__mobile-mcp` `mobile_launch_app` with `packageName = "io.github.onreg.nextplay"`.

### Step N: Design verification (Figma)
- Compare the implemented UI to the referenced frames/states.
- Use `mcp__mobile-mcp` screenshots to capture evidence for comparison.
- If this step fails, fix the issue and repeat this step until it passes before proceeding.

### Step N+1: Manual test MT1 - Navigate list -> details -> back
- Validate:
  - Tapping a game card opens details for that game.
  - Back returns to game list.
- Actions (`mcp__mobile-mcp`):
  - `mobile_list_elements_on_screen` to locate a game card; `mobile_click_on_screen_at_coordinates` to open it.
  - `mobile_take_screenshot` on details screen.
  - Tap back button; `mobile_take_screenshot` on list screen.
- If this test fails, fix the issue and repeat this same step until it passes before proceeding.

### Step N+2: Manual test MT2 - Cached-first then refresh behavior
- Validate:
  - Re-opening a previously opened game shows cached details immediately, then refreshes (no blocking skeleton over cached content).
- Actions (`mcp__mobile-mcp`):
  - Open a game details, wait for content to load, then go back.
  - Disable network:
    - `mobile_launch_app` with `packageName = "com.android.settings"`
    - Toggle “Airplane mode” ON (use `mobile_list_elements_on_screen` + taps).
  - Re-open the same game details; observe cached content renders.
  - Re-enable network:
    - Return to Settings and toggle “Airplane mode” OFF.
  - Return to details and observe content remains visible and updates (if server data differs).
- If this test fails, fix the issue and repeat this same step until it passes before proceeding.

### Step N+3: Manual test MT3 - No cache shows skeleton then content
- Validate:
  - For a game not previously opened (clear app data or choose a new game), details shows full-screen skeleton until cached content exists, then renders content.
- Actions (`mcp__mobile-mcp`):
  - (If needed) uninstall + reinstall app to clear cache using `mobile_uninstall_app` then `mobile_install_app` or `:app:installDebug`.
  - Launch app and open a game; take screenshots during skeleton and after content.
- If this test fails, fix the issue and repeat this same step until it passes before proceeding.

### Step N+4: Manual test MT4 - No cache + network failure shows error + Retry
- Validate:
  - When no cache exists and network fails, show full-screen error with Retry.
  - Retry triggers a refetch and eventually shows content when network is restored.
- Actions (`mcp__mobile-mcp`):
  - Ensure no cache (fresh install).
  - Disable network:
    - `mobile_launch_app` with `packageName = "com.android.settings"`
    - Toggle “Airplane mode” ON.
  - Open details; capture error screenshot.
  - Re-enable network (Airplane mode OFF), return to app, tap Retry; capture success screenshot.
- If this test fails, fix the issue and repeat this same step until it passes before proceeding.

### Step N+5: Manual test MT5 - Details card content + bookmark behavior
- Validate:
  - Release date shown.
  - Platforms row uses same icon treatment as list.
  - Rating shown with one decimal.
  - Bookmark toggle matches game list behavior (local-only).
- Actions (`mcp__mobile-mcp`):
  - Open details; take screenshot of details card.
  - Toggle bookmark; navigate back to list and confirm the same visual/behavior expectation (as applicable), then return.
- If this test fails, fix the issue and repeat this same step until it passes before proceeding.

### Step N+6: Manual test MT6 - Website row validation + browser intent
- Validate:
  - Website row only appears when URL is valid.
  - Tapping it opens system browser to that URL.
- Actions (`mcp__mobile-mcp`):
  - Open a game with website; tap website row; verify browser opens (screenshot).
  - Return to app; open a game without/invalid website; confirm row is absent (screenshot).
- If this test fails, fix the issue and repeat this same step until it passes before proceeding.

### Step N+7: Manual test MT7 - Description expand/collapse
- Validate:
  - Collapsed to 5 lines by default.
  - “Read more” expands; “Read less” collapses.
- Actions (`mcp__mobile-mcp`):
  - Open details; screenshot collapsed description.
  - Tap “Read more”; screenshot expanded.
  - Tap “Read less”; screenshot collapsed again.
- If this test fails, fix the issue and repeat this same step until it passes before proceeding.

### Step N+8: Manual test MT8 - Screenshots paging + image viewer intent
- Validate:
  - “Screenshots” section shows when content exists.
  - Carousel pages/scrolls and loads more as needed.
  - Tapping a screenshot opens system image viewer for that single image.
- Actions (`mcp__mobile-mcp`):
  - Scroll to screenshots; swipe horizontally on carousel using `mobile_swipe_on_screen`.
  - Tap a screenshot; verify external viewer opens (screenshot).
  - Return to app.
- If this test fails, fix the issue and repeat this same step until it passes before proceeding.

### Step N+9: Manual test MT9 - Movies paging + video intent (quality selection)
- Validate:
  - “Movies” section shows when content exists.
  - Tapping a movie opens system video player for that single movie.
  - Playback works (best-quality URL selection rule applied).
- Actions (`mcp__mobile-mcp`):
  - Scroll to movies; swipe carousel; tap a movie.
  - Confirm the external player opens and starts playback (screenshot).
- If this test fails, fix the issue and repeat this same step until it passes before proceeding.

### Step N+10: Manual test MT10 - Developers section visibility
- Validate:
  - Developers are shown when non-empty, hidden when empty.
- Actions (`mcp__mobile-mcp`):
  - Open a game with developers present; screenshot section.
  - Open a game with developers empty; confirm section absent; screenshot.
- If this test fails, fix the issue and repeat this same step until it passes before proceeding.

### Step N+11: Manual test MT11 - Series paging + open another details instance
- Validate:
  - “Series” section shows when content exists.
  - Carousel pages.
  - Tapping a series game pushes a new details screen for that gameId (back stack contains previous details).
- Actions (`mcp__mobile-mcp`):
  - Scroll to series; tap an item; screenshot new details.
  - Tap back; verify it returns to previous game details; screenshot.
- If this test fails, fix the issue and repeat this same step until it passes before proceeding.

### Step N+12: Manual test MT12 - Offline hides empty paged sections
- Validate:
  - When offline and a paged section has no cached content, the section is hidden entirely (including title).
- Actions (`mcp__mobile-mcp`):
  - Fresh install (no cached pages).
  - Disable network:
    - `mobile_launch_app` with `packageName = "com.android.settings"`
    - Toggle “Airplane mode” ON.
  - Open details; verify screenshots/movies/series sections are not shown; screenshot.
- If this test fails, fix the issue and repeat this same step until it passes before proceeding.

### Step N+13: Manual test MT13 - Non-goals (no Share, no Similar Games)
- Validate:
  - No Share action in header.
  - No “Similar Games” section.
- Actions (`mcp__mobile-mcp`):
  - Open details; visually confirm absence; screenshot.
- If this test fails, fix the issue and repeat this same step until it passes before proceeding.
