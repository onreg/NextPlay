# ADR-002: Dependency Injection Strategy

- Status: Accepted
- Date: 2026-01-11
- Project: NextPlay

## Context
NextPlay is a multi-module Android app. Modules are split into:
- "core" foundations (network, db, Android utilities, UI primitives)
- "data" contracts (`data/*/api`) and implementations (`data/*/impl`)
- "presentation" UI components and UI mappers
- "feature" orchestration (Compose panes and ViewModels)
- ":app" as the composition root

The codebase uses dependency injection to wire cross-module dependencies (network, database, repositories, and UI mappers) while preserving the API/impl split.

## Decision
- Use Hilt (Dagger) as the single DI mechanism across modules.
- Treat `:app` as the DI root and composition boundary via `@HiltAndroidApp` and `@AndroidEntryPoint`.
- Install infrastructure and data implementation bindings into the application-wide graph (SingletonComponent).
- Install presentation-only bindings (UI mappers) into the ViewModelComponent to keep resource-aware mapping close to UI lifecycle and avoid leaking Android resource access into data/infra.
- Provide feature state holders as `@HiltViewModel` and retrieve them from Compose using `hiltViewModel()`.

## Overview
### DI framework and graph overview
Hilt is initialized from the application entry point and used to create the runtime object graph. Modules provide:
- singleton lifetime infrastructure (Room database, DAOs, OkHttp, Retrofit, API clients)
- singleton or factory lifetime data implementation wiring (repositories, paging config, RemoteMediator)
- ViewModel lifetime presentation wiring (UI mappers)

Component usage observed in source:
- `SingletonComponent` for core and data implementation modules.
- `ViewModelComponent` for presentation modules.
- `@HiltViewModel` for feature ViewModels (Hilt generates the internal binding modules and factories).

ASCII overview (module flow into Hilt components):

```
:app
  - NextPlayApp (@HiltAndroidApp)
  - MainActivity (@AndroidEntryPoint)
       |
       v
Hilt graph
  SingletonComponent
    - core:network (OkHttp, Retrofit, GameApi)
    - core:db (Room, DAOs, TransactionProvider)
    - core:util-android (ResourcesProvider)
    - data:game:impl (GameRepositoryImpl, PagingConfig, RemoteMediator factory, DTO/entity mappers)

  ViewModelComponent
    - presentation:game (GameUiMapper)
    - presentation:platform (PlatformUiMapper)
    - feature:game (@HiltViewModel GamesPaneViewModel)

Compose UI
  - feature panes call hiltViewModel<T>() to obtain ViewModels
```

## Design and conventions
### Module composition and ownership
Where bindings live:
- `:app` owns entry points only and assembles the dependency graph by depending on features, core modules, and `data/*/impl` modules.
- `core/*` owns infrastructure wiring in `.../di/*Module.kt` installed into `SingletonComponent`.
- `data/*/impl` owns repository implementations, paging wiring, and DTO/entity mappers and binds them into `SingletonComponent`.
- `presentation/*` owns UI mappers and binds them into `ViewModelComponent`.
- `feature/*` owns `@HiltViewModel` classes and consumes injected interfaces from `data/*/api` and injected mappers from `presentation/*`.

Ownership rules (as implemented today):
- Data consumers depend on `data/*/api` interfaces; `data/*/impl` provides bindings.
- Infrastructure is created in core modules and exposed via Hilt, not constructed in features.
- UI mapping stays in presentation modules, and feature ViewModels depend on mapper interfaces.

### Binding conventions
Interfaces vs implementations:
- Contracts live in `data/*/api` as interfaces (for example, `GameRepository`).
- Implementations live in `data/*/impl` with `@Inject` constructors (for example, `GameRepositoryImpl`).
- Interface bindings are declared in a Hilt `@Module` using `@Binds`.

Mappers and stateless objects:
- Mappers are expressed as an interface plus an `*Impl` class with an `@Inject` constructor.
- Mapper bindings use `@Binds` (no custom multibindings observed).
- Some simple mappers are constructed with no dependencies and are intentionally unscoped.

Provides bindings for third party or runtime configuration:
- Core infrastructure that cannot be constructor-injected (Room database, Retrofit setup) is created via `@Provides` functions.
- Module-specific runtime config values are provided via `@Provides` (for example, `PagingConfig`).

Qualifiers and multibindings:
- Qualifiers are used where required by Hilt (`@ApplicationContext`).
- No custom qualifiers (`@Named` or custom `@Qualifier`) were found in main sources.
- No custom multibindings (`@IntoSet`, `@IntoMap`) were found in main sources (Hilt generates internal multibindings for ViewModels).

Assisted injection:
- No `@Assisted` or assisted factory patterns were found.

### ViewModel provisioning
- Feature ViewModels are declared with `@HiltViewModel` and an `@Inject` constructor.
- Compose obtains a ViewModel from the current Hilt-backed `ViewModelStoreOwner` using `hiltViewModel<T>()`.
- The current `:app` navigation setup passes route arguments directly to composables; ViewModels do not currently use `SavedStateHandle`.

### Scopes and lifetimes
Observed scopes and their intent:
- `@Singleton` is used for:
  - network stack and clients (Moshi, OkHttpClient, Retrofit, API interfaces)
  - Room database, DAOs, and TransactionProvider
  - repository bindings (for example, `GameRepositoryImpl`)
  - some configuration objects (for example, `PagingConfig`)

Component placement as a lifetime signal:
- Infrastructure and data implementations are installed in `SingletonComponent`.
- Presentation bindings are installed in `ViewModelComponent`.

Guideline: ViewModelComponent bindings are currently unscoped by default; if stability within a single ViewModel instance is needed, consider `@ViewModelScoped`.

Factory and per-use bindings:
- Some bindings are intentionally left unscoped (for example, the `RemoteMediator` provided by `GameModule`).
- Call sites can request a fresh instance via `Provider<T>` (for example, repository creates a new RemoteMediator instance when building a Pager).

No custom scopes or custom components were found.

### Example feature deep dive (`:feature:game`)
1) `MainActivity` hosts the Compose `NavHost` and renders `GamesPane()` for the games route.
2) `GamesPane()` calls `hiltViewModel<GamesPaneViewModel>()` to obtain the feature ViewModel.
3) Hilt constructs `GamesPaneViewModel` via its `@Inject` constructor, supplying a `GameRepository` and a `GameUiMapper`.
4) `GameRepository` is an interface from `data/game/api`; Hilt binds it to `GameRepositoryImpl` from `data/game/impl` via `@Binds`.
5) `GameRepositoryImpl` builds a Paging `Pager` with a provided `PagingConfig` and a fresh `RemoteMediator` instance obtained via `Provider<RemoteMediator<...>>`.
6) `RemoteMediator` is created from the `data/game/impl` module and depends on `GameApi` (Retrofit), Room DAOs, and DTO/entity mappers, plus a `TransactionProvider`.
7) `GameUiMapper` is bound in `presentation/game` (ViewModelComponent) and uses `PlatformUiMapper` from `presentation/platform`.
8) `PlatformUiMapperImpl` depends on `ResourcesProvider` from `core/util-android`, allowing UI mapping to use Android string resources.

## Testing strategy
DI is not directly exercised via Hilt in the current unit test suite. Most DI breakages are caught at compile time by Hilt/KSP code generation rather than runtime graph validation tests.
- No `@HiltAndroidTest`, `@TestInstallIn`, or module replacement patterns were found.
- Unit tests instantiate classes directly and use `mockito-kotlin` plus test drivers to supply dependencies.
- The shared `:testing:unit` module provides test utilities (coroutines rules, paging helpers, Mockito).

## Forbidden or avoided practices
(only if evidenced)
- Avoid depending on `data/*/impl` from features: features inject `data/*/api` interfaces and rely on Hilt modules in `data/*/impl` to bind implementations.
- Avoid creating infrastructure in feature code: DB and network objects are provided from `core/*` Hilt modules.

## Consequences
Positive:
- Cross-module wiring stays declarative and centralized in `.../di` modules.
- The `data/*/api` vs `data/*/impl` split is enforced at the DI boundary: features inject interfaces.
- Infrastructure lifetimes are explicit via `SingletonComponent` and `@Singleton`.

Negative:
- DI behavior is harder to validate in tests because there is no dedicated Hilt test harness.
- Some bindings rely on component placement without explicit scoping (for example, ViewModelComponent bindings without `@ViewModelScoped`).

Operational impact:
- Most modules apply the Hilt convention plugin, so KSP and Hilt dependencies are part of the standard module setup.
- Generated code (KSP) is part of the build and required for DI wiring.

## Compliance checklist
(PR and LLM)
- [ ] Add new app entry points (activities) with `@AndroidEntryPoint` when they need injection.
- [ ] Keep `:app` as the DI root and do not manually construct Dagger components.
- [ ] Put infrastructure providers (Room, Retrofit, OkHttp, API clients) in `core/*` `@Module` classes installed in `SingletonComponent`.
- [ ] Put repository implementations and IO wiring in `data/*/impl`, and bind `data/*/api` interfaces via `@Binds`.
- [ ] In features, inject only `data/*/api` interfaces, not `data/*/impl` concrete classes.
- [ ] Keep UI mapping in `presentation/*` and inject mapper interfaces into feature ViewModels.
- [ ] Use `@Provides` only for objects that cannot be constructor-injected or need explicit runtime config.
- [ ] Prefer explicit scopes (`@Singleton`) for long-lived infrastructure and repositories.
- [ ] When a type should be recreated per use (for example, per Pager), keep it unscoped and inject `Provider<T>` at the call site.
- [ ] Keep qualifiers minimal; introduce a qualifier only when there are multiple bindings of the same type.
- [ ] For unit tests, continue using test drivers and Mockito; introduce Hilt test harness only if a test needs to validate the full graph.

## Evidence index
Entry points:
- `app/src/main/kotlin/io/github/onreg/nextplay/NextPlayApp.kt`
- `app/src/main/kotlin/io/github/onreg/nextplay/MainActivity.kt`

Build logic (Hilt setup):
- `build-logic/convention/src/main/kotlin/core/HiltConventionPlugin.kt`
- `build-logic/convention/src/main/kotlin/core/ApplicationConventionPlugin.kt`
- `build-logic/convention/src/main/kotlin/presets/FeatureConventionPlugin.kt`
- `build-logic/convention/src/main/kotlin/presets/NonUiConventionPlugin.kt`

Core SingletonComponent modules:
- `core/network/src/main/kotlin/io/github/onreg/core/network/di/*.kt`
- `core/db/src/main/kotlin/io/github/onreg/core/db/di/*.kt`
- `core/util-android/src/main/kotlin/io/github/onreg/core/util/android/di/*.kt`

Data API and impl bindings:
- `data/game/api/src/main/kotlin/io/github/onreg/data/game/api/GameRepository.kt`
- `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/di/GameModule.kt`
- `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/GameRepositoryImpl.kt`
- `data/game/impl/src/main/kotlin/io/github/onreg/data/game/impl/mapper/*.kt`

Presentation ViewModelComponent modules:
- `presentation/game/src/main/kotlin/io/github/onreg/ui/game/presentation/di/GamePresentationModule.kt`
- `presentation/platform/src/main/kotlin/io/github/onreg/ui/platform/di/PlatformPresentationModule.kt`

Feature example (`:feature:game`) ViewModel and Compose retrieval:
- `feature/game/src/main/kotlin/io/github/onreg/feature/game/impl/GamesPaneViewModel.kt`
- `feature/game/src/main/kotlin/io/github/onreg/feature/game/impl/pane/GamesPane.kt`

Tests (DI bypassed with direct construction and mocks):
- `feature/game/src/test/kotlin/io/github/onreg/feature/game/impl/GamesPaneViewModelTestDriver.kt`
- `data/game/impl/src/test/kotlin/io/github/onreg/data/game/impl/GameRepositoryTestDriver.kt`
- `testing/unit/build.gradle.kts`

## Open questions
- Is there an intended Hilt-based DI test strategy (for example, instrumented tests with module replacement), or is DI validation intentionally out of scope for unit tests?
- For bindings installed in `ViewModelComponent` (presentation mappers), should lifetimes remain unscoped, or should some of these be explicitly scoped for stability within a ViewModel?
