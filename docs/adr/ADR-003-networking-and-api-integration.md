# ADR-003: Networking and API Integration

- Status: Accepted
- Date: 2026-01-11
- Project: NextPlay

## Context
NextPlay integrates with the RAWG public API to fetch game content. The app is structured as a multi-module project where networking is treated as a core capability and consumed by data implementations (repositories, paging mediators) rather than features directly.

This ADR documents the networking approach as it exists today (as-is) so changes to endpoints, auth, and error handling remain consistent across modules.

## Decision
- Use Retrofit on top of OkHttp as the HTTP client stack, centralized in `:core:network`.
- Use Moshi for JSON serialization with generated adapters for DTOs plus a small set of custom adapters.
- Provide a single Retrofit instance (RAWG base URL) and a single OkHttpClient instance, both owned by `:core:network`.
- Inject the RAWG API key via an OkHttp interceptor that appends the `key` query parameter to every request.
- Standardize API call return types on `NetworkResponse<T>` so transport failures are represented as values rather than thrown to callers.
- Keep Retrofit interfaces and DTOs in `:core:network`; keep mapping from DTO to app models in `data/*/impl`.

## Overview
### Networking stack overview
The networking stack lives in `:core:network` and is wired into the app using Hilt. Consumers typically inject Retrofit service interfaces (for example, `GameApi`) rather than Retrofit itself.

Data and feature modules use the network stack through repository boundaries. Paging uses a `RemoteMediator` that calls the Retrofit API and persists the result into Room for offline-backed lists.

```
ViewModel -> data/*/api repository -> data/*/impl (RemoteMediator) -> core/network *Api -> Retrofit -> OkHttp -> interceptors -> HTTP
```

## Design and conventions
### Configuration and environment
Base URL(s):
- The base URL is hard-coded in `:core:network` for RAWG and is currently the only configured host.

API key injection strategy:
- The RAWG API key is injected as a query parameter `key=<apiKey>` by `RawgApiKeyInterceptor`.
- The key value is provided via `BuildConfig.RAWG_API_KEY` generated in `:core:network`.
- The Gradle configuration for `:core:network` resolves the key from either:
  - environment variable `RAWG_API_KEY`, or
  - Gradle property `RAWG_API_KEY`.
- Build configuration fails fast when the key is missing (empty or blank).

Logging policy per build type:
- OkHttp logging is enabled at BODY level for debug builds and disabled for non-debug builds.
- Because `RawgApiKeyInterceptor` runs before `HttpLoggingInterceptor`, debug logs may include the full URL including the key.

### Request pipeline
Interceptors and responsibilities (in order):
- `RawgApiKeyInterceptor`: appends the RAWG API key to every request as a query parameter.
- `HttpLoggingInterceptor`: logs requests and responses in debug builds only.

Headers and query parameters conventions:
- Authentication uses query parameter `key` rather than headers.
- Endpoint-specific query parameters (for example, paging params) are modeled explicitly on Retrofit interface methods.

Timeouts and retry policy:
- No explicit timeouts, retry, caching, or backoff policies are configured in the OkHttp client. Default OkHttp behavior applies.

### Response handling and error mapping
API result contract:
- Retrofit service methods return `NetworkResponse<T>`.
- `NetworkResponse` is a sealed type with:
  - `Success(body: T)` for HTTP 2xx responses with a non-null body.
  - `Failure.NetworkError(exception: IOException)` for IO failures.
  - `Failure.OtherError(exception: Throwable?)` for non-IO failures; `exception` is `null` when the response is not representable as `Success`.

Error classification:
- IO failures are mapped to `NetworkError`.
- Non-IO throwables are mapped to `OtherError(throwable)`.
- HTTP non-2xx responses and HTTP 2xx responses with a `null` body are mapped to `OtherError(null)`. Status code and error body are not exposed through `NetworkResponse`.

Where mapping happens:
- `:core:network` owns transport-level wrapping (`NetworkResponse`) and JSON parsing (Moshi).
- `data/*/impl` owns mapping from DTOs to app-facing models and also decides how to translate network failures into domain/UI-visible failures.
- In the game list flow, network failures become Paging `LoadState.Error` via `RemoteMediator.MediatorResult.Error`, and UI maps that error to a coarse error type (IOException vs other).

### Usage conventions
Where Retrofit interfaces live:
- Retrofit interfaces are placed under `core/network/.../*/api` and exposed via Hilt `@Provides` bindings.

DTOs and mapping ownership:
- DTOs live in `:core:network` and use Moshi annotations with generated adapters.
- `data/*/impl` defines mapper interfaces and `*Impl` implementations to translate DTOs to app models.

Repository boundary rules:
- Features depend on `data/*/api` interfaces and models.
- Data implementations depend on `:core:network` and call service interfaces, not Retrofit directly.

RemoteMediator interaction (current sources):
- `RemoteMediator` requests a page using the Retrofit service.
- On `NetworkResponse.Success`, it maps DTOs to models, transforms them into database entities, and writes them transactionally along with remote keys.
- On `NetworkResponse.Failure`, it surfaces an exception through `MediatorResult.Error` (falling back to an "Unknown error" exception when none is present).

## Testing strategy
What is covered and how:
- Interceptor behavior is unit tested by mocking OkHttp chain interactions and asserting the resulting request URL.
- Paging and repository behavior is unit tested using Mockito-Kotlin, coroutines test APIs, and test drivers that encapsulate stubbing and helpers.

Where DI is bypassed or used:
- Unit tests do not construct the Hilt graph. They instantiate classes directly and supply mocked dependencies.
- No MockWebServer-based integration tests are used in current sources.

## Forbidden or avoided practices
(only if evidenced)
- Avoid passing Retrofit or OkHttp details into feature modules. Features consume repositories and UI load states rather than HTTP primitives.
- Avoid DTO-to-domain mapping in `:core:network`; mapping lives in `data/*/impl` mappers.

## Consequences
Positive:
- A single, shared client configuration (base URL, auth, logging, Moshi) reduces duplication and keeps network setup centralized.
- DTO and transport concerns stay in `:core:network`, while app model mapping stays in `data/*/impl`, matching module boundaries.
- Paging integrates cleanly by using `RemoteMediator` as the boundary between network and local persistence.

Negative:
- HTTP errors do not propagate structured information (status code and error body) through the public `NetworkResponse` contract.
- Debug logging can include secrets in request URLs because auth is query-param based and logging runs after auth injection.

Operational impact:
- Builds require `RAWG_API_KEY` at configuration time for `:core:network`, which affects local dev and CI configuration.
- Networking behavior is mostly validated by unit tests with mocks, so end-to-end serialization and Retrofit wiring issues are not covered by MockWebServer tests.

## Compliance checklist
(PR and LLM)
- [ ] Put HTTP client wiring in `core/network/.../di/*Module.kt`, not in features.
- [ ] Keep Retrofit interfaces under `core/network/.../api` and name them `*Api`.
- [ ] Keep DTOs under `core/network/.../dto` and use Moshi annotations with generated adapters.
- [ ] Inject auth via the existing interceptor mechanism (query parameter `key`) unless a new API requires a different contract.
- [ ] Make Retrofit service methods return `NetworkResponse<T>` when integrating with the existing call adapter.
- [ ] Do DTO to app model mapping in `data/*/impl` using mapper interfaces plus `*Impl` classes.
- [ ] Convert `NetworkResponse.Failure` to caller-level errors at the boundary (for example, `MediatorResult.Error`), not in `:core:network`.
- [ ] When adding new error types, ensure UI classification remains consistent (IOException vs other) where Paging load states are rendered.

## Evidence index
Networking stack wiring:
- `core/network/src/main/kotlin/io/github/onreg/core/network/di/*.kt`
- `core/network/build.gradle.kts`
- `docs/instructions/core-network.md`

Auth and logging:
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/interceptor/*.kt`

Serialization and DTO conventions:
- `core/network/src/main/kotlin/io/github/onreg/core/network/moshi/*.kt`
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/*.kt`

API definitions:
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/api/*.kt`

Response contract:
- `core/network/src/main/kotlin/io/github/onreg/core/network/retrofit/*.kt`

Paging boundary example:
- `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/**/*.kt`
- `presentation/game/src/main/kotlin/io/github/onreg/ui/game/presentation/components/list/GameList.kt`
- `feature/game/src/main/kotlin/io/github/onreg/feature/game/impl/*.kt`

Tests:
- `core/network/src/test/kotlin/io/github/onreg/core/network/rawg/interceptor/*.kt`
- `data/game/impl/src/test/kotlin/io/github/onreg/data/game/impl/**/*.kt`

## Open questions
- Should HTTP errors (non-2xx) be represented with status code and error body in the public `NetworkResponse` contract, or is the current "OtherError(null)" sufficient for current UI needs?
- Is it acceptable for debug logging to include the RAWG API key in URLs, or should the interceptor order and/or logging policy be adjusted?
- Some internal docs state "suspend calls throw on failure" for networking. Should those docs be aligned with the current `NetworkResponse<T>` behavior?
