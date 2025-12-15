## Module overview

### Boundaries (what it owns vs must not own)
**Owns**
- Raw Retrofit service interfaces for RAWG and their DTOs (no domain mapping): `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`, `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDto.kt`, `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/PaginatedResponseDto.kt`
- Network DI wiring (Moshi, OkHttp, Retrofit, service creation) via Hilt: `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`, `core/network/src/main/kotlin/io/github/onreg/core/network/di/ApiModule.kt`
- RAWG auth integration via an OkHttp interceptor that injects `key=<apiKey>` query param: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptor.kt`
- JSON parsing adapters for types used by DTOs (currently `Instant`): `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/InstantJsonAdapter.kt`, `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`

**Must NOT own**
- Repository contracts/implementations and paging orchestration (owned by `data/*`): `data/game/api/src/main/kotlin/io/github/onreg/data/game/api/GameRepository.kt`, `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/GameRepositoryImpl.kt`, `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt`
- Domain models and cross-module mapping rules (kept in `data/*`): `data/game/api/src/main/kotlin/io/github/onreg/data/game/api/model/Game.kt`, `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/GameDtoMapper.kt`
- Persistence and DB transactions (owned by `core/db`): `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`, `core/db/src/main/kotlin/io/github/onreg/core/db/TransactionProvider.kt`

### Public API surface (what other modules should depend on)
- **Retrofit service interfaces** (inject these; do not inject `Retrofit` directly in app code): `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`, used via DI in `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt`
- **DTOs** (consumed by mapping layer in `data/*/impl`): `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDto.kt`, referenced by `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/GameDtoMapper.kt`
- **Hilt-provided singletons** (internal wiring; consumers usually don’t reference these types directly): `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`, `core/network/src/main/kotlin/io/github/onreg/core/network/di/ApiModule.kt`

### Key technologies used
- **Retrofit (suspend APIs)**: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`, `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`, deps in `core/network/build.gradle.kts`
- **OkHttp + HttpLoggingInterceptor**: `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`, deps in `core/network/build.gradle.kts`
- **Moshi (JSON) + KotlinJsonAdapterFactory**: `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`, DTO annotations in `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDto.kt`
- **Hilt DI** (SingletonComponent modules): `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`, `core/network/src/main/kotlin/io/github/onreg/core/network/di/ApiModule.kt`, plugin applied in `core/network/build.gradle.kts`
- **java.time.Instant + custom adapter**: `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/InstantJsonAdapter.kt`, used by `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDto.kt`

### Directory structure
- `core/network/src/main/kotlin/io/github/onreg/core/network/di/`: Hilt DI provisioning for Moshi/OkHttp/Retrofit/APIs (`NetworkModule.kt`, `ApiModule.kt`)
- `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/`: Moshi adapters (`InstantJsonAdapter.kt`)
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/`: Retrofit service interfaces (`GameApi.kt`)
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/`: DTOs for RAWG payloads (`GameDto.kt`, `PaginatedResponseDto.kt`)
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/interceptor/`: OkHttp interceptors for RAWG concerns (`RawgApiKeyInterceptor.kt`)
- `core/network/src/test/kotlin/io/github/onreg/core/network/rawg/interceptor/`: unit tests for interceptors (`RawgApiKeyInterceptorTest.kt`)

### Naming conventions & patterns
- **Service interfaces**: `*Api` in `rawg/api/`, `public interface`, `suspend fun` endpoints, Retrofit annotations and snake_case query names (`page_size`): `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`
- **DTOs**: `*Dto` in `rawg/dto/`, annotated with Moshi `@JsonClass(generateAdapter = true)` and field-level `@Json(name = "...")`: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDto.kt`, `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/PaginatedResponseDto.kt`
- **Auth**: RAWG API key is injected as a query param named `key` via an OkHttp interceptor: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptor.kt`
  - The key value comes from `BuildConfig.RAWG_API_KEY`, populated at build time from env var or Gradle property: `core/network/build.gradle.kts`, `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptor.kt`
- **Base URL**: currently hard-coded to RAWG (`https://api.rawg.io/api/`) as a private constant: `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
- **Interceptors & logging**:
  - Logging is `BODY` in debug and `NONE` in release, based on `BuildConfig.DEBUG`: `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
  - Interceptor order is API key first, logging second: `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
- **Qualifiers**: none in current setup (single `OkHttpClient` and single `Retrofit`): `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
- **Timeouts/retries**: no explicit client timeouts or retry policy is configured (OkHttp defaults apply): `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`

## How to add a new API feature
Follow this checklist to add a new RAWG endpoint/service inside `:core:network`.

1) Ensure you can build the module (RAWG API key required at configuration time): `core/network/build.gradle.kts`
   - Set `RAWG_API_KEY` as an environment variable or Gradle property (supported inputs): `core/network/build.gradle.kts`
2) Decide where the endpoint lives:
   - Add it to an existing service if it’s the same domain area (example service): `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`
   - Or create a new `*Api` interface under the same package root: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/`
3) Add/extend the Retrofit service interface:
   - Use `public interface`, `public suspend fun`, and Retrofit annotations (`@GET`, `@Query`, etc.): `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`
4) Create request/response DTOs:
   - Place DTOs under `rawg/dto/` and use Moshi `@JsonClass(generateAdapter = true)` + `@Json(name = ...)`: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDto.kt`
   - Use repo-consistent nullability: optional fields should be nullable and/or defaulted to `emptyList()` (existing examples): `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDto.kt`, `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/PaginatedResponseDto.kt`
5) Serialization rules and date handling:
   - `Instant` parsing is customized to accept an ISO-8601 *date* string (`yyyy-MM-dd`) and convert it to midnight UTC `Instant`: `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/InstantJsonAdapter.kt`
   - If the endpoint returns a full timestamp (or a non-date format), this adapter may produce `null` (because it parses via `LocalDate.parse(...)`): `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/InstantJsonAdapter.kt`
6) Auth handling:
   - RAWG API key is always appended as `key=<apiKey>` to every request made by the module’s `OkHttpClient`: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptor.kt`, `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
   - If you need an unauthenticated call path, there is no existing pattern (Unknown from repository); check whether you should (a) make the interceptor conditional or (b) add a second `OkHttpClient`/`Retrofit`: `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
7) Error handling (current repo pattern):
   - Core networking does not define a typed error model or response wrapper (only converter factory is Moshi): `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
   - Call sites currently call Retrofit suspending functions directly and rely on exceptions (example call site): `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt`
8) DI wiring:
   - If you created a new service interface, add a `@Provides` function that calls `retrofit.create(...)` in `ApiModule`: `core/network/src/main/kotlin/io/github/onreg/core/network/di/ApiModule.kt`
   - If you need a new interceptor/client-level concern, wire it in `provideOkHttpClient`: `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
9) Logging/interceptors rules:
   - Do not enable network logging in release builds (already enforced): `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
   - Be aware the current interceptor order logs the final URL (including API key) in debug builds: `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`, `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptor.kt`
10) Definition of done:
   - `:core:network` compiles with `RAWG_API_KEY` set: `core/network/build.gradle.kts`
   - Unit tests pass (existing pattern is JVM tests): `core/network/src/test/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptorTest.kt`
   - If you added new DTOs/services, add at least one unit test covering the new behavior (see templates and `data/game/impl` examples): `data/game/impl/src/test/kotlin/io/github/onreg/data/game/impl/mapper/GameDtoMapperTest.kt`
   - Update any docs that reference network boundaries (example doc referencing network module): `docs/instructions/core-db.md`

## How to write tests for networking

### Where tests live (current state)
- `:core:network` uses JVM unit tests under `core/network/src/test/kotlin/` (example): `core/network/src/test/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptorTest.kt`
- There is no `core/network/src/androidTest/` test source set in this module (current directory layout): `core/network/src`

### Current test dependencies and style
- Base unit test deps come from the library convention plugin (JUnit + kotlin-test + Mockito): `build-logic/convention/src/main/kotlin/LibraryConventionPlugin.kt`
- Current `:core:network` tests use:
  - `kotlin.test.Test`/assertions: `core/network/src/test/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptorTest.kt`
  - Mockito-Kotlin for mocking/stubbing/verifying: `core/network/src/test/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptorTest.kt`

### MockWebServer / Retrofit parsing tests (status in this repo)
- `MockWebServer` is available in the version catalog but is not currently added to `:core:network` deps: `gradle/libs.versions.toml`, `core/network/build.gradle.kts`
- `kotlinx-coroutines-test` is available and used in other modules for suspend testing (but is not currently added to `:core:network`): `gradle/libs.versions.toml`, `data/game/impl/build.gradle.kts`, `core/network/build.gradle.kts`
- There is no established JSON fixtures pattern in this repo (Unknown from repository); existing tests build payloads inline: `data/game/impl/src/test/kotlin/io/github/onreg/data/game/impl/mapper/GameDtoMapperTest.kt`
- Coroutine-based unit tests elsewhere in the repo use `kotlinx.coroutines.test.runTest` and test dispatchers (example): `data/game/impl/src/test/kotlin/io/github/onreg/data/game/impl/GameRepositoryTest.kt`
