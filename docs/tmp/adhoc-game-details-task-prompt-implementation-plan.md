# Game Details Screen (Offline-First) Implementation Plan

## Overview
Implement a fully featured, offline-first Game Details screen opened from the existing game list, including:
- Cache-first details (Room) + background refresh (RAWG `/games/{id}`)
- Offline-first paged sections (Room + Paging3 RemoteMediator) for screenshots, movies, and series
- External intents for website, screenshot viewing, and movie playback

- Relevant ADRs:
  - ADR-001 (Architecture): keep `:app` for nav wiring, `:feature:*` for orchestration, `:presentation:*` for reusable UI/mapping, `:data:*:api` contracts + `:data:*:impl` IO, `:core:*` for infra.
  - ADR-002 (DI): Hilt everywhere; data impl bindings in `SingletonComponent`; presentation mappers in `ViewModelComponent`; features depend only on `data/*/api`.
  - ADR-003 (Networking): Retrofit interfaces + DTOs live in `:core:network`, return `NetworkResponse<T>`, API key via `RawgApiKeyInterceptor`.
  - ADR-004 (Persistence): Room in `:core:db`, PagingSource + RemoteMediator + remote keys tables; schema export; destructive migrations expected.
  - ADR-005 (State/UI): MVVM with StateFlow + event Flow; paging mapped in VM via injected mappers; side effects (navigation/intents) in Composables.
  - ADR-008 (Testing): prefer JVM unit tests; Robolectric for Compose + Room/Android; paging tests via snapshot/utilities.
- ADR Conflicts:
  - Task Prompt “intended” module split (`feature/games-list`, `feature/games-details`, `data/*`) does not match current repo modules (`:feature:game`, `:data:game:*`). Plan proposes adding new modules for details/screenshots/movies/series and a new details feature module, while keeping the existing list in `:feature:game`.
  - Endpoint mismatch in `docs/task/game_details.md` (`/games/{id}/series`) vs Task Prompt + example (`/games/{id}/game-series`). Plan follows Task Prompt + `docs/api/game_series.md` as source of truth: `/games/{id}/game-series`.
- Assumptions:
  - `:feature:game` remains the game list feature; a new `:feature:game-details` (or similarly named) module will host the details screen.
  - A single list key is used for the existing game list cache (e.g., `listKey = "default"`), enabling future multiple lists.
  - Room destructive migration remains acceptable for the upcoming schema bump (per ADR-004).
  - “Offline-first refresh” for details means: render cached DB row when present; always trigger a network refresh on entering; if no cached row and refresh fails, show full-screen error.
- Open Questions:
  - Should the details feature live in a new `:feature:*` module (`:feature:game-details`) or remain inside `:feature:game` to avoid Gradle churn?
  - Should movie quality selection be persisted as a single chosen URL (recommended) or should the full `data: Map<String, String>` be stored (needs Room converter)?
  - HTML description rendering: is a simple HTML-to-text approach acceptable, or should we render styled spans (e.g., via `HtmlCompat`) in a TextView interop?

## Files to Modify
- `settings.gradle.kts` - include new feature/data/presentation modules (if we proceed with the intended split).
- `app/src/main/kotlin/io/github/onreg/nextplay/MainActivity.kt` - route details screen to the new details feature composable (and support pushing series items).
- `feature/game/src/main/kotlin/io/github/onreg/feature/game/impl/pane/GamesPane.kt` - keep existing navigation event; update route constant if needed.
- `core/network/src/main/kotlin/io/github/onreg/core/network/di/ApiModule.kt` - provide new Retrofit service interfaces (details/screenshots/movies/series).
- `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt` - add new entities + DAOs and bump Room version.
- `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt` - update paging query to use list membership join table (avoid `GameEntity.insertionOrder` conflicts with series).
- `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt` - persist game list membership rows + list-specific remote keys.
- `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/GameEntityMapper.kt` - stop storing list ordering in `GameEntity`; return bundles including membership rows.
- `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/di/GameModule.kt` - bind the new DAOs/mappers/config if signatures change.

## New Files to Create
> Module and package names below assume we adopt the Task Prompt’s intended split. If we keep everything inside existing modules, move these files accordingly while preserving layer boundaries.

- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameDetailsApi.kt` - `GET /games/{id}`.
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameScreenshotsApi.kt` - `GET /games/{id}/screenshots`.
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameMoviesApi.kt` - `GET /games/{id}/movies`.
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameSeriesApi.kt` - `GET /games/{id}/game-series`.
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDetailsDto.kt` - DTO for details response (subset needed by UI).
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/ScreenshotDto.kt` - screenshots item DTO.
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/MovieDto.kt` - movies item DTO.
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/DeveloperDto.kt` - developer DTO (id + name).

- `core/db/src/main/kotlin/io/github/onreg/core/db/details/entity/GameDetailsEntity.kt` - details cache table keyed by `gameId`.
- `core/db/src/main/kotlin/io/github/onreg/core/db/details/entity/DeveloperEntity.kt` - developers dimension table.
- `core/db/src/main/kotlin/io/github/onreg/core/db/details/entity/GameDeveloperCrossRef.kt` - join table for details -> developers.
- `core/db/src/main/kotlin/io/github/onreg/core/db/details/dao/GameDetailsDao.kt` - observe + upsert details + developer relations.

- `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameListEntity.kt` - list membership keyed by `listKey`.
- `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameListRemoteKeysEntity.kt` - list-specific remote keys keyed by (`listKey`, `gameId`).
- `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameListDao.kt` - paging source for a list key + clear list membership.
- `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameListRemoteKeysDao.kt` - get/insert list keys.

- `core/db/src/main/kotlin/io/github/onreg/core/db/screenshots/entity/ScreenshotEntity.kt` - screenshots cache (scoped by `gameId`).
- `core/db/src/main/kotlin/io/github/onreg/core/db/screenshots/entity/ScreenshotRemoteKeysEntity.kt` - remote keys for screenshots keyed by (`gameId`, `screenshotId`).
- `core/db/src/main/kotlin/io/github/onreg/core/db/screenshots/dao/ScreenshotDao.kt` - paging source by `gameId` + clear-by-game.
- `core/db/src/main/kotlin/io/github/onreg/core/db/screenshots/dao/ScreenshotRemoteKeysDao.kt` - remote keys by item id.

- `core/db/src/main/kotlin/io/github/onreg/core/db/movies/entity/MovieEntity.kt` - movies cache (scoped by `gameId`, with chosen bestUrl).
- `core/db/src/main/kotlin/io/github/onreg/core/db/movies/entity/MovieRemoteKeysEntity.kt` - remote keys keyed by (`gameId`, `movieId`).
- `core/db/src/main/kotlin/io/github/onreg/core/db/movies/dao/MovieDao.kt` - paging source by `gameId` + clear-by-game.
- `core/db/src/main/kotlin/io/github/onreg/core/db/movies/dao/MovieRemoteKeysDao.kt` - remote keys by item id.

- `core/db/src/main/kotlin/io/github/onreg/core/db/series/entity/SeriesGameEntity.kt` - series membership keyed by `parentGameId` (ordering isolated from list ordering).
- `core/db/src/main/kotlin/io/github/onreg/core/db/series/entity/SeriesRemoteKeysEntity.kt` - series remote keys keyed by (`parentGameId`, `gameId`).
- `core/db/src/main/kotlin/io/github/onreg/core/db/series/dao/SeriesDao.kt` - paging source for series (select `GameEntity` joined with membership, ordered by membership order).
- `core/db/src/main/kotlin/io/github/onreg/core/db/series/dao/SeriesRemoteKeysDao.kt` - remote keys for series.

- `data/details/api/src/main/kotlin/io/github/onreg/data/details/api/GameDetailsRepository.kt` - observe + refresh contract.
- `data/details/api/src/main/kotlin/io/github/onreg/data/details/api/model/GameDetails.kt` - API-facing model.
- `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/GameDetailsRepositoryImpl.kt` - offline-first impl.
- `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/mapper/GameDetailsDtoMapper.kt` - DTO -> API model mapping.
- `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/mapper/GameDetailsEntityMapper.kt` - API model <-> DB entities mapping.
- `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/di/DetailsModule.kt` - Hilt bindings.

- `data/screenshots/api/src/main/kotlin/io/github/onreg/data/screenshots/api/GameScreenshotsRepository.kt` - screenshots paging contract.
- `data/screenshots/api/src/main/kotlin/io/github/onreg/data/screenshots/api/model/Screenshot.kt` - API model.
- `data/screenshots/impl/src/main/kotlin/io/github/onreg/data/screenshots/impl/GameScreenshotsRepositoryImpl.kt` - Pager wiring.
- `data/screenshots/impl/src/main/kotlin/io/github/onreg/data/screenshots/impl/paging/ScreenshotsRemoteMediator.kt` - per-game mediator.
- `data/screenshots/impl/src/main/kotlin/io/github/onreg/data/screenshots/impl/mapper/ScreenshotDtoMapper.kt` - DTO -> API model.
- `data/screenshots/impl/src/main/kotlin/io/github/onreg/data/screenshots/impl/mapper/ScreenshotEntityMapper.kt` - API model <-> DB mapping.
- `data/screenshots/impl/src/main/kotlin/io/github/onreg/data/screenshots/impl/di/ScreenshotsModule.kt` - Hilt bindings.

- `data/movies/api/src/main/kotlin/io/github/onreg/data/movies/api/GameMoviesRepository.kt` - movies paging contract.
- `data/movies/api/src/main/kotlin/io/github/onreg/data/movies/api/model/Movie.kt` - API model.
- `data/movies/impl/src/main/kotlin/io/github/onreg/data/movies/impl/GameMoviesRepositoryImpl.kt` - Pager wiring.
- `data/movies/impl/src/main/kotlin/io/github/onreg/data/movies/impl/paging/MoviesRemoteMediator.kt` - per-game mediator.
- `data/movies/impl/src/main/kotlin/io/github/onreg/data/movies/impl/quality/MovieQualitySelector.kt` - pure Kotlin “best URL” logic.
- `data/movies/impl/src/main/kotlin/io/github/onreg/data/movies/impl/mapper/MovieDtoMapper.kt` - DTO -> API model mapping.
- `data/movies/impl/src/main/kotlin/io/github/onreg/data/movies/impl/mapper/MovieEntityMapper.kt` - API model <-> DB mapping.
- `data/movies/impl/src/main/kotlin/io/github/onreg/data/movies/impl/di/MoviesModule.kt` - Hilt bindings.

- `data/series/api/src/main/kotlin/io/github/onreg/data/series/api/GameSeriesRepository.kt` - series paging contract.
- `data/series/impl/src/main/kotlin/io/github/onreg/data/series/impl/GameSeriesRepositoryImpl.kt` - Pager wiring (reuses `data/game/api` `Game` model).
- `data/series/impl/src/main/kotlin/io/github/onreg/data/series/impl/paging/SeriesRemoteMediator.kt` - per-parent mediator.
- `data/series/impl/src/main/kotlin/io/github/onreg/data/series/impl/di/SeriesModule.kt` - Hilt bindings.

- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPane.kt` - entry composable for details.
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/GameDetailsViewModel.kt` - state + events + paging streams.
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/model/GameDetailsState.kt` - screen state (details + UI toggles + errors).
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/model/GameDetailsEvent.kt` - one-off actions (go back, open URL, open media, open series game).

- `presentation/details/src/main/kotlin/io/github/onreg/ui/details/presentation/model/*.kt` - UI models for details card/sections.
- `presentation/details/src/main/kotlin/io/github/onreg/ui/details/presentation/mapper/GameDetailsUiMapper.kt` - API -> UI mapping (formatting, one-decimal rating, date formatting).
- `presentation/details/src/main/kotlin/io/github/onreg/ui/details/presentation/components/*.kt` - reusable Compose components (details card, description block, horizontal paged carousels for screenshots/movies/series header sections).

## Implementation Steps

### Step 1: Add new Gradle modules (feature + data + presentation)
- Where:
  - `settings.gradle.kts`
  - `data/**/build.gradle.kts`, `feature/**/build.gradle.kts`, `presentation/**/build.gradle.kts`
- What:
  - Create modules for details/screenshots/movies/series (api + impl), and a `feature` module for details orchestration, plus an optional `presentation/details` UI module.
  - Wire `:app` to depend on new `data/*/impl` modules to include DI bindings.
- Why:
  - Matches Task Prompt architecture notes and ADR-001 dependency direction (`feature` depends on `data/*/api`, not `impl`).
- How:
  - Example contract module dependencies (no implementation bodies):
    ```kotlin
    // data/details/api
    interface GameDetailsRepository {
        fun observeGameDetails(gameId: Int): kotlinx.coroutines.flow.Flow<GameDetails?>
        suspend fun refreshGameDetails(gameId: Int): RefreshResult
    }
    ```
- Outcome:
  - Project has the correct module surface to keep boundaries clean as the feature grows.

### Step 2: Extend RAWG Retrofit APIs + DTOs in `:core:network`
- Where:
  - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/*.kt`
  - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/*.kt`
  - `core/network/src/main/kotlin/io/github/onreg/core/network/di/ApiModule.kt`
- What:
  - Add Retrofit interfaces for:
    - Details: `GET games/{id}`
    - Screenshots: `GET games/{id}/screenshots?page=<n>`
    - Movies: `GET games/{id}/movies?page=<n>`
    - Series: `GET games/{id}/game-series?page=<n>`
  - Add DTOs for the minimum fields required by the Task Prompt and `docs/api/*` examples.
- Why:
  - ADR-003 mandates DTOs + Retrofit service definitions in `:core:network`, returning `NetworkResponse<T>`.
- How (signatures only):
  ```kotlin
  interface GameDetailsApi {
      suspend fun getGameDetails(id: Int): io.github.onreg.core.network.retrofit.NetworkResponse<GameDetailsDto>
  }

  interface GameScreenshotsApi {
      suspend fun getScreenshots(
          id: Int,
          page: Int,
          pageSize: Int,
      ): io.github.onreg.core.network.retrofit.NetworkResponse<io.github.onreg.core.network.rawg.dto.PaginatedResponseDto<ScreenshotDto>>
  }
  ```
- Outcome:
  - Data modules can call new endpoints while keeping network concerns isolated in `:core:network`.

### Step 3: Refactor game list persistence to use list membership ordering (avoid conflicts with series)
- Where:
  - `core/db/src/main/kotlin/io/github/onreg/core/db/game/entity/GameEntity.kt`
  - `core/db/src/main/kotlin/io/github/onreg/core/db/game/dao/GameDao.kt`
  - `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
  - `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt`
  - `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/GameEntityMapper.kt`
- What:
  - Remove/stop using `GameEntity.insertionOrder` for ordering the game list.
  - Introduce `GameListEntity(listKey, gameId, insertionOrder)` and query games via that membership ordering.
  - Introduce list-specific remote keys keyed by (`listKey`, `gameId`) so paging state is isolated per list collection.
- Why:
  - Task Prompt reuse rule: membership and ordering must be stored in collection-specific join tables.
  - Prevents ordering corruption when the same `GameEntity` is inserted for multiple collections (game list + series).
- How (data structures only):
  ```kotlin
  data class GameListEntity(
      val listKey: String,
      val gameId: Int,
      val insertionOrder: Long,
  )

  data class GameListRemoteKeysEntity(
      val listKey: String,
      val gameId: Int,
      val prevKey: Int?,
      val nextKey: Int?,
  )
  ```
- Outcome:
  - Game list ordering becomes independent from any other collection that reuses `GameEntity`.

### Step 4: Add Room tables + DAOs for cached game details (offline-first)
- Where:
  - `core/db/src/main/kotlin/io/github/onreg/core/db/details/**`
  - `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
- What:
  - Add `GameDetailsEntity` storing: `gameId`, `name`, `bannerImageUrl?`, `releasedAt?`, `websiteUrl?`, `rating?`, `descriptionHtml?`.
  - Add developer tables (`DeveloperEntity` + `GameDeveloperCrossRef`) to store developers list (hide section when empty).
  - Add `GameDetailsDao` exposing a single stream for “details + developers” for a given game id.
- Why:
  - Enables cache-first rendering on the details screen per Task Prompt; aligns with ADR-004 (Room as local source of truth).
- How (public-facing DAO signatures only):
  ```kotlin
  interface GameDetailsDao {
      fun observeGameDetails(gameId: Int): kotlinx.coroutines.flow.Flow<GameDetailsWithDevelopers?>
      suspend fun upsertDetails(details: GameDetailsEntity)
      suspend fun replaceDevelopers(
          gameId: Int,
          developers: List<DeveloperEntity>,
      )
  }
  ```
- Outcome:
  - Details feature can render cached details immediately when present, independent of paging sections.

### Step 5: Add Room tables + DAOs for screenshots paging (per game)
- Where:
  - `core/db/src/main/kotlin/io/github/onreg/core/db/screenshots/**`
  - `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
- What:
  - Persist screenshots in `ScreenshotEntity` scoped by `gameId` with stable order per game (insertionOrder).
  - Add per-game screenshot remote keys keyed by (`gameId`, `screenshotId`).
  - Add DAO returning `PagingSource<Int, ScreenshotEntity>` filtered by `gameId`.
- Why:
  - Required for offline-first pagination consistent with the existing list pattern (RemoteMediator + Room).
- How (DAO signatures only):
  ```kotlin
  interface ScreenshotDao {
      fun pagingSource(gameId: Int): androidx.paging.PagingSource<Int, ScreenshotEntity>
      suspend fun clearForGame(gameId: Int)
      suspend fun upsertAll(items: List<ScreenshotEntity>)
  }
  ```
- Outcome:
  - Screenshots can be paged, cached, and refreshed independently for each game.

### Step 6: Add Room tables + DAOs for movies paging (per game)
- Where:
  - `core/db/src/main/kotlin/io/github/onreg/core/db/movies/**`
  - `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
- What:
  - Persist movies scoped by `gameId`, storing:
    - `previewUrl?`, `name?`, and `bestVideoUrl` (computed from quality map).
  - Add remote keys keyed by (`gameId`, `movieId`).
  - Add DAO returning a paging source filtered by `gameId`.
- Why:
  - Offline-first pagination required; persisting “best URL” avoids needing a Room converter for `Map<String, String>`.
- How (entity sketch only):
  ```kotlin
  data class MovieEntity(
      val id: Int,
      val gameId: Int,
      val name: String?,
      val previewUrl: String?,
      val bestVideoUrl: String,
      val insertionOrder: Long,
  )
  ```
- Outcome:
  - Movies section works offline and can always open the best quality URL without recomputing.

### Step 7: Add Room tables + DAOs for series paging (membership join over `GameEntity`)
- Where:
  - `core/db/src/main/kotlin/io/github/onreg/core/db/series/**`
  - `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
- What:
  - Create series membership table keyed by `parentGameId`, with ordering per parent game:
    - `SeriesGameEntity(parentGameId, gameId, insertionOrder)`
  - Create series remote keys table keyed by (`parentGameId`, `gameId`) to isolate pagination state per series feed.
  - DAO query returns a `PagingSource` for series items by selecting games joined with membership, ordered by membership insertion order (and including platforms relation via existing cross refs).
- Why:
  - Task Prompt requires series to behave like the game list card UI while keeping ordering/paging state independent per parent game.
- How (repository contract only):
  ```kotlin
  interface GameSeriesRepository {
      fun getSeries(parentGameId: Int): kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<io.github.onreg.data.game.api.model.Game>>
  }
  ```
- Outcome:
  - Series section is paged, cached, and supports tap-to-open-details for nested navigation.

### Step 8: Implement data contracts for details/screenshots/movies/series
- Where:
  - `data/details/api/**`
  - `data/screenshots/api/**`
  - `data/movies/api/**`
  - `data/series/api/**`
- What:
  - Define repository interfaces and API models.
  - Keep API modules free of Android/Room/Retrofit concerns (ADR-001 + Task Prompt boundaries).
- Why:
  - Enables `:feature:*` to depend only on stable contracts, improving testability and preventing impl leakage.
- How (models only):
  ```kotlin
  data class GameDetails(
      val id: Int,
      val name: String,
      val bannerImageUrl: String?,
      val releaseDate: java.time.Instant?,
      val websiteUrl: String?,
      val rating: Double?,
      val descriptionHtml: String?,
      val developers: List<Developer>,
  )

  data class Developer(
      val id: Int,
      val name: String,
  )
  ```
- Outcome:
  - Feature and presentation layers can evolve independently from implementation details.

### Step 9: Implement offline-first `GameDetailsRepositoryImpl` (cache-first + refresh)
- Where:
  - `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/GameDetailsRepositoryImpl.kt`
  - `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/di/DetailsModule.kt`
- What:
  - Observe cached details from Room (Flow).
  - Implement `refreshGameDetails(gameId)` that:
    - Calls `GameDetailsApi.getGameDetails(gameId)`
    - On success: maps DTO -> API model -> DB entities and persists in a transaction
    - On failure: returns an error type consumable by the feature UI (IOException vs other)
- Why:
  - Satisfies Task Prompt offline-first behavior for the primary screen content.
- How (public signatures only):
  ```kotlin
  sealed interface RefreshResult {
      data object Success : RefreshResult
      data class Failure(val throwable: Throwable) : RefreshResult
  }
  ```
- Outcome:
  - Details screen can show cached content immediately and refresh in the background.

### Step 10: Implement RemoteMediators + repositories for screenshots, movies, and series
- Where:
  - `data/screenshots/impl/**`
  - `data/movies/impl/**`
  - `data/series/impl/**`
- What:
  - Implement one RemoteMediator per paged section:
    - Screenshots: uses `GameScreenshotsApi`
    - Movies: uses `GameMoviesApi` and persists `bestVideoUrl`
    - Series: uses `GameSeriesApi`, persists games + membership + remote keys
  - Each repository method returns a `Pager` with:
    - consistent `PagingConfig` (reuse the same values as the game list)
    - a Room PagingSource filtered by gameId/parentGameId
    - a per-request RemoteMediator instance created for that id (via factory/provider)
- Why:
  - Task Prompt requires “same pattern and paging configuration as the game list”, with per-collection pagination state isolation.
- How (factory shape only):
  ```kotlin
  interface ScreenshotsRemoteMediatorFactory {
      fun create(gameId: Int): androidx.paging.RemoteMediator<Int, io.github.onreg.core.db.screenshots.entity.ScreenshotEntity>
  }
  ```
- Outcome:
  - Each section paginates and caches independently and works offline per section rules.

### Step 11: Add movie quality selection utility + unit tests
- Where:
  - `data/movies/impl/src/main/kotlin/io/github/onreg/data/movies/impl/quality/MovieQualitySelector.kt`
  - `data/movies/impl/src/test/kotlin/io/github/onreg/data/movies/impl/quality/MovieQualitySelectorTest.kt`
- What:
  - Implement pure Kotlin selection:
    - Prefer `"max"` when present
    - Else pick highest numeric key (e.g. `"480"` > `"360"`)
  - Add unit tests for edge cases:
    - only `"max"`, only numeric, mixed numeric/non-numeric, empty map
- Why:
  - Required by acceptance criteria; ADR-008 recommends fast unit tests for pure logic.
- How (signature only):
  ```kotlin
  interface MovieQualitySelector {
      fun bestUrl(qualityUrls: Map<String, String>): String?
  }
  ```
- Outcome:
  - Movie playback always uses the correct URL and is robust to odd maps.

### Step 12: Add presentation UI models + mappers for details screen
- Where:
  - `presentation/details/src/main/kotlin/io/github/onreg/ui/details/presentation/**`
- What:
  - Define UI models for:
    - Details header/title
    - Details card rows (release date, platforms, website row, bookmark, rating)
    - Description UI state (collapsed/expanded)
    - Screenshots item UI
    - Movies item UI
  - Mapper formats:
    - Release date to `MMM d, yyyy` (Locale.US, same as list)
    - Rating to 1 decimal place (when non-null)
    - Website row visibility only if URL is valid
- Why:
  - ADR-001/ADR-005: formatting and resource use belongs in presentation layer, not data layer.
- How (mapper signatures only):
  ```kotlin
  interface GameDetailsUiMapper {
      fun map(details: io.github.onreg.data.details.api.model.GameDetails): GameDetailsUi
  }
  ```
- Outcome:
  - Feature ViewModel can stay thin and declarative while UI mapping stays consistent and testable.

### Step 13: Implement `GameDetailsViewModel` with cache-first rendering, refresh, and section paging
- Where:
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/GameDetailsViewModel.kt`
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/model/*.kt`
- What:
  - State includes:
    - `detailsUi: GameDetailsUi?`
    - `isInitialLoading: Boolean`
    - `initialError: ErrorUi?` (shown only when cache is empty)
    - `isDescriptionExpanded: Boolean`
    - paging flows for screenshots/movies/series (as `Flow<PagingData<...>>`, cached in VM)
  - On start:
    - collect `observeGameDetails(gameId)` from repository and map to UI
    - trigger refresh once
  - On retry:
    - re-trigger refresh
  - Bookmark behavior:
    - keep the same in-ViewModel toggle rule as the game list (Set of bookmarked ids)
  - Emit events:
    - go back
    - open website
    - open screenshot
    - open movie
    - open series game details (push new instance)
- Why:
  - Satisfies Task Prompt’s state + error rules and matches ADR-005 event-driven side effects.
- How (types only):
  ```kotlin
  sealed interface GameDetailsEvent {
      data object GoBack : GameDetailsEvent
      data class OpenUrl(val url: String) : GameDetailsEvent
      data class OpenImage(val url: String) : GameDetailsEvent
      data class OpenVideo(val url: String) : GameDetailsEvent
      data class OpenGameDetails(val gameId: Int) : GameDetailsEvent
  }
  ```
- Outcome:
  - All feature behavior is centralized in a testable VM consistent with existing patterns.

### Step 14: Implement `GameDetailsPane` UI with skeleton/error states and section hiding rules
- Where:
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPane.kt`
  - `presentation/details/src/main/kotlin/io/github/onreg/ui/details/presentation/components/*.kt`
- What:
  - Render:
    - header (back + title)
    - banner image
    - details card
    - description expand/collapse (“Read more” / “Read less”, 5 lines when collapsed)
    - screenshots carousel (paged)
    - movies carousel (paged)
    - developers section
    - series carousel (paged, uses existing `GameCard` UI)
  - Section hiding:
    - if offline and no cached items for a paged section, hide that section entirely (title included)
  - Handle events to launch:
    - browser for website
    - system image viewer for screenshot
    - system video player for movie
- Why:
  - Meets UI/interaction acceptance criteria; keeps side effects in UI layer (ADR-005).
- How (event collection pattern only):
  ```kotlin
  // Collect event Flow in composable and call Context.startActivity(...) based on event type.
  ```
- Outcome:
  - User can navigate, view cached content, refresh, and open external content correctly.

### Step 15: Wire navigation in `:app` and support pushing details for series game taps
- Where:
  - `app/src/main/kotlin/io/github/onreg/nextplay/MainActivity.kt`
  - `feature/game/src/main/kotlin/io/github/onreg/feature/game/impl/pane/GamesPane.kt`
- What:
  - Keep existing game list navigation route: `GameDetails/{gameId}`.
  - Replace placeholder `io.github.onreg.feature.game.impl.pane.GameDetailsPane` with the real details feature pane.
  - When details emits “open series game”, call `navController.navigate("GameDetails/$id")` to push another instance.
- Why:
  - Task Prompt requires series taps to open another details screen on the back stack.
- Outcome:
  - Navigation works for both list -> details and details -> series details.

### Step 16: Add/adjust automated tests for details behavior (ADR-008)
- Where:
  - `data/details/impl/src/test/**` - repository refresh behavior with cache present/absent
  - `data/screenshots/impl/src/test/**`, `data/movies/impl/src/test/**`, `data/series/impl/src/test/**` - RemoteMediator unit tests (pattern copied from `data/game/impl`)
  - `feature/game-details/src/test/**` - ViewModel tests for:
    - initial refresh trigger
    - error state when no cache + refresh fails
    - description expand/collapse state toggling
  - `feature/game-details/src/test/**` - Compose (Robolectric) tests for:
    - skeleton vs error vs content
    - “Read more/less” toggles and max-lines behavior (as semantics/expected text)
- What:
  - Follow existing test driver patterns in this repo:
    - “Builder/TestDriver” for ViewModel and RemoteMediator tests
    - Compose screen tests via `createComposeRule()` and test tags
- Why:
  - Task Prompt includes a test plan; ADR-008 recommends JVM tests + Robolectric for Compose.
- Outcome:
  - Feature behavior is guarded against regressions without relying on device tests.

### Step 17: Regenerate Room schema exports after DB changes
- Why:
  - ADR-004 requires schema export updates; DB version bump must produce new `core/db/schemas/.../*.json`.
- Command to run:
  ```bash
  ./gradlew :core:db:kspDebugKotlin
  ```

### Step 18: Run static analysis and apply fixes based on reports
- Why:
  - Ensure detekt + ktlint + lint compliance after broad multi-module changes (AGENTS + repo conventions).
- Command to run:
  ```bash
  ./gradlew codeQuality
  ```

### Step 19: Run unit tests and apply fixes based on reports
- Why:
  - Validate new repositories/mediators/ViewModels and Compose UI logic with JVM tests (ADR-008).
- Command to run:
  ```bash
  ./gradlew testDevDebugUnitTest
  ```

