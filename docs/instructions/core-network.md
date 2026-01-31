## core/network: rules for implementing or changing networking

### Boundaries
- Owns only the raw networking layer:
    - Retrofit service interfaces and DTOs (no domain mapping):
        - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`
        - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDto.kt`
        - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/PaginatedResponseDto.kt`
    - OkHttp/Moshi/Retrofit setup and DI wiring via Hilt:
        - `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
        - `core/network/src/main/kotlin/io/github/onreg/core/network/di/ApiModule.kt`
    - RAWG auth integration (API key query param):
        - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptor.kt`
    - Moshi adapters used by DTOs (currently `Instant`):
        - `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/InstantJsonAdapter.kt`
        - (adapter registration) `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`

- Must NOT own:
    - Repository contracts/implementations or paging orchestration:
        - `data/game/api/src/main/kotlin/io/github/onreg/data/game/api/GameRepository.kt`
        - `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/GameRepositoryImpl.kt`
        - `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt`
    - Domain models and cross-module mapping rules:
        - `data/game/api/src/main/kotlin/io/github/onreg/data/game/api/model/Game.kt`
        - `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/GameDtoMapper.kt`
    - Persistence, Room, or DB transactions:
        - `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt`
        - `core/db/src/main/kotlin/io/github/onreg/core/db/TransactionProvider.kt`

### What other modules should depend on
- Inject Retrofit service interfaces (not `Retrofit` directly):
    - Service API: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`
    - Example consumer: `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt`
- DTOs are consumed only by mapping layer in `data/*/impl`:
    - DTO: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDto.kt`
    - Example mapper: `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/GameDtoMapper.kt`
- DI modules are wiring only; consumers typically shouldn’t reference them:
    - `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
    - `core/network/src/main/kotlin/io/github/onreg/core/network/di/ApiModule.kt`

### Directory placement
- DI:
    - `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
    - `core/network/src/main/kotlin/io/github/onreg/core/network/di/ApiModule.kt`
- Moshi adapters:
    - `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/InstantJsonAdapter.kt`
- RAWG services:
    - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/`
- RAWG DTOs:
    - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/`
- RAWG interceptors:
    - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/interceptor/`
- Tests:
    - `core/network/src/test/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptorTest.kt`

### Naming and API design conventions
- Service interfaces:
    - Name: `*Api`
    - Location: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/`
    - `public interface`, `public suspend fun`, Retrofit annotations, RAWG-style query names (example uses `page_size`):
        - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`
- DTOs:
    - Name: `*Dto`
    - Location: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/`
    - Moshi: `@JsonClass(generateAdapter = true)` + field-level `@Json(name = "...")`:
        - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDto.kt`
        - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/PaginatedResponseDto.kt`
    - Nullability/defaults: match existing style (nullable for optional, defaults for collections):
        - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDto.kt`
        - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/PaginatedResponseDto.kt`

### Auth, base URL, logging, interceptors
- RAWG key is injected as query param `key=<apiKey>` for every request:
    - Interceptor: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptor.kt`
    - OkHttp wiring: `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
    - Key source: `BuildConfig.RAWG_API_KEY` (configured in):
        - `core/network/build.gradle.kts`
        - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptor.kt`
- Base URL is currently RAWG-only and hard-coded:
    - `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
- Logging:
    - Debug: BODY, Release: NONE, based on `BuildConfig.DEBUG`:
        - `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
    - Interceptor order: API key first, logging second:
        - `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
    - Implication: debug logs may include the final URL with the API key.

### Serialization rules (Moshi) and `Instant`
- `Instant` parsing is customized to accept an ISO-8601 date string (`yyyy-MM-dd`) and convert to midnight UTC `Instant`:
    - `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/InstantJsonAdapter.kt`
- If an endpoint returns a full timestamp or non-date format, this adapter may return `null` (because it parses via `LocalDate.parse(...)`):
    - `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/InstantJsonAdapter.kt`
- If new endpoints require a different date/time format:
    - Add/adjust Moshi adapter(s) in:
        - `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/`
    - Register in:
        - `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`

## Add or change an API endpoint (checklist)

1. Ensure the module can configure (API key is required at build configuration time):
    - `core/network/build.gradle.kts`

2. Choose where the endpoint belongs:
    - Extend existing service when it is the same area:
        - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`
    - Or add a new service interface:
        - Create `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/<Thing>Api.kt`

3. Implement Retrofit declaration:
    - Use `public suspend fun` and Retrofit annotations:
        - Reference style: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/GameApi.kt`

4. Add/extend DTOs:
    - Add DTO(s) under:
        - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/`
    - Use Moshi annotations consistently:
        - Reference style: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDto.kt`
        - Paginated wrapper style: `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/PaginatedResponseDto.kt`
    - Keep DTOs raw (no domain-level computed properties or mapping helpers).

5. Confirm date/time handling:
    - If response uses date-only strings, `InstantJsonAdapter` applies:
        - `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/InstantJsonAdapter.kt`
    - If response uses timestamps, update/add adapters and registration:
        - Adapter location: `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/`
        - Registration: `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`

6. DI wiring (only if you created a new service interface):
    - Add `@Provides fun provide<Thing>Api(retrofit: Retrofit): <Thing>Api = retrofit.create(...)` in:
        - `core/network/src/main/kotlin/io/github/onreg/core/network/di/ApiModule.kt`

7. Client-level concerns (interceptors, logging, base URL, timeouts):
    - Implement/wire client changes only in:
        - `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
    - If you need a second client/Retrofit instance, introduce qualifiers (none currently exist) and keep the default path unchanged:
        - Current single-client reference: `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`

8. Error handling expectations (current repo behavior):
    - No typed error wrapper is defined in `core/network`; suspend calls throw on failure.
    - Example call site relying on exceptions:
        - `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediator.kt`

## Tests: add or update network-layer coverage
- Tests live under JVM unit tests:
    - `core/network/src/test/kotlin/`
- Existing test to copy for interceptors:
    - `core/network/src/test/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptorTest.kt`
- Current style uses:
    - `kotlin.test` (`@Test`, assertions):
        - `core/network/src/test/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptorTest.kt`
    - Mockito-Kotlin:
        - `core/network/src/test/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptorTest.kt`
- If you add new interceptor behavior:
    - Add/extend unit tests in the same package:
        - `core/network/src/test/kotlin/io/github/onreg/core/network/rawg/interceptor/`

## Fast review checklist (before finishing)
- No repositories, paging orchestration, domain models, or mappers added to `core/network`.
- Services are `*Api` under:
    - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/`
- DTOs are `*Dto` under:
    - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/`
- `ApiModule` provides any new service interface:
    - `core/network/src/main/kotlin/io/github/onreg/core/network/di/ApiModule.kt`
- Any client-level change is isolated to:
    - `core/network/src/main/kotlin/io/github/onreg/core/network/di/NetworkModule.kt`
- Date/time parsing is compatible with returned formats:
    - `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/InstantJsonAdapter.kt`
- Debug logging does not leak secrets beyond what is currently accepted (API key in URL is currently loggable in debug due to order).
- Tests added/updated:
    - `core/network/src/test/kotlin/...`
