# ADR-001: Application Architecture

- Status: Accepted
- Date: 2026-01-11
- Project: NextPlay

## Context
This repository is organized as a multi-module Android app with a deliberate separation between "core" foundations, "data" contracts and implementations, and "feature" orchestration with reusable "presentation" UI. The codebase is already following an architectural pattern, but that pattern is implicit in module structure and DI wiring rather than documented as a decision.

This ADR documents the current, dominant architecture as it exists today (as-is) to:
- Make module boundaries and dependency direction explicit.
- Reduce accidental cross-layer coupling as more features are added.
- Provide a concrete reference flow using one representative feature: `:feature:game`.

Key constraints observed:
- Multi-module Gradle build with an included `build-logic` build that centralizes convention plugins.
- Hilt is the DI mechanism across modules.
- Coroutines and Flow are the primary concurrency and state model.
- Paging 3 is used for end-to-end list flows with offline cache (Room + RemoteMediator).
- Network access requires an API key injected at build time for the RAWG API client.

## Decision
The codebase implements a feature-first, layered modular architecture:
- "App" is the composition root (application + activity) and owns navigation wiring.
- "Feature" modules own screen orchestration and state holders (ViewModels) and compose UI from "presentation" components.
- "Presentation" modules own reusable Compose components, UI models, and UI mappers (including access to Android resources).
- "Data" is split into contracts (`data/*/api`) and implementations (`data/*/impl`), with implementations providing side effects (network/db) and binding interfaces via Hilt.
- "Core" modules provide cross-cutting capabilities (UI primitives, networking, persistence, Android utilities) and are depended on by higher layers.

The dominant data flow is one-way at the feature boundary: UI events -> ViewModel intents/reducers -> repository Flow -> mapped UI state -> Compose rendering, with one-off UI actions modeled as event flows.

## Overview
### Architectural overview
At a high level, the project is a modular Android app where `:app` composes feature flows, feature modules orchestrate UI and state, and data implementations provide side-effecting IO behind API interfaces.

Dependency direction (arrows point from dependers to dependencies):

```
 (Gradle only)
 build-logic (conventions)

 :app -> :feature:*
 :app -> :data:*:impl
 :app -> :core:*

 :feature:* -> :presentation:*
 :feature:* -> :data:*:api

 :presentation:* -> :core:*
 :presentation:* -> :data:*:api (optional)

 :data:*:impl -> :data:*:api
 :data:*:impl -> :core:network
 :data:*:impl -> :core:db

 :core:* -> external libraries only
```

Notes:
- `data/*/impl` depends on `core/network` and `core/db` to perform IO.
- `feature/*` depends on `data/*/api` (interfaces and models), not `data/*/impl`.
- `:app` depends on `data/*/impl` to bring implementations into the DI graph.

### Typical feature data flow (generic, based on `:feature:game`)
1) A top-level Compose host (in `:app`) renders a feature entry composable for the current route.
2) The feature entry composable obtains its ViewModel via Hilt and starts collecting state flows.
3) A user interaction triggers a ViewModel "onX" handler that updates local state and/or emits a one-off UI event.
4) The ViewModel requests a repository stream from a `data/*/api` interface and combines it with local UI state to produce UI-facing state.
5) The repository implementation (from `data/*/impl`) exposes a Flow backed by Paging.
6) Paging loads cached entities from Room via a DAO paging source and synchronizes with the network via a RemoteMediator.
7) Network responses are returned via core network result types and persisted inside a transaction (mapping happens in `data/*/impl`); mapped entities are then emitted through the paging flow.
8) UI mappers translate API models into UI models (including formatting and resource-based mapping).
9) Compose components render list/content/error states based on the current paging and screen state, and one-off events drive navigation actions.

## Design and conventions
### Layers and responsibilities
#### `:app` (application assembly)
Responsibilities:
- Define the application entry point and DI root (`@HiltAndroidApp`).
- Host top-level Compose content and define navigation graph.
- Assemble the installable experience by depending on features, data implementations, and core foundations.

Typical contents:
- `Application` subclass, activities, top-level `NavHost`.

Must not contain:
- Feature business logic beyond wiring and composition.
- Direct data access patterns that bypass repositories.

#### `:feature:*` (feature orchestration)
Responsibilities:
- Own the screen-level orchestration: state holder (ViewModel), event handling, and wiring to navigation.
- Compose UI by using "presentation" components and mapping API models into UI state via mappers.

Typical contents:
- Compose panes/screens, ViewModels, feature-specific state and event types, feature routes.

Must not contain:
- Concrete network or database access (Retrofit, DAOs, Room).
- Data implementation types (`data/*/impl`) and direct instantiation of repositories.

#### `:presentation:*` (reusable UI and UI mapping)
Responsibilities:
- Provide reusable Compose components and UI models.
- Provide UI mappers that translate API models to UI models (including resource-based mapping).

Typical contents:
- Compose components, UI models, mapper interfaces and implementations, ViewModel-scoped DI bindings.

Must not contain:
- Side-effecting IO (network/db).
- Feature orchestration (navigation graph ownership, screen-level ViewModels).

#### `:data:*:api` (data contracts)
Responsibilities:
- Define stable business contracts (repository interfaces) and shared models for consumers.

Typical contents:
- Repository interfaces, API-level models used across feature/presentation layers.

Must not contain:
- Android framework dependencies that force UI/runtime coupling.
- Implementations that perform IO.

#### `:data:*:impl` (data implementations)
Responsibilities:
- Implement repository interfaces behind `data/*/api`.
- Own side effects: networking, database, caching, paging mediation.
- Bind implementations and mappers into the DI graph (typically singleton-scoped).

Typical contents:
- Repository implementations, RemoteMediator/Paging wiring, DTO/entity mappers, Hilt modules.

Must not contain:
- Compose UI or screen orchestration.
- Navigation concerns.

#### `:core:*` (foundations)
Responsibilities:
- Provide shared capabilities consumed across the app: UI primitives, networking stack, persistence, Android utilities.
- Own cross-cutting DI modules that expose infrastructure services.

Typical contents:
- `core/ui`: theme, reusable Compose components, runtime helpers.
- `core/network`: Retrofit/OkHttp/Moshi configuration, interceptors, response adapters, API interfaces.
- `core/db`: Room database, DAOs, entities, transaction helpers.
- `core/util-android`: lifecycle/state utilities and resource helpers.

Must not contain:
- Feature-specific business logic.
- Dependencies on `:app`, `:feature:*`, `:presentation:*`, or `:data:*`.

#### `:testing:unit` (test utilities)
Responsibilities:
- Provide shared test utilities for unit tests across modules.

Typical contents:
- Coroutine dispatcher rules, Flow and Paging helpers, common assertions/drivers.

Must not contain:
- Production code or feature implementation logic.

### Dependency rules
Allowed:
- `:app` -> `:feature:*`, `:data:*:impl`, `:core:*`
- `:feature:*` -> `:presentation:*`, `:data:*:api`, `:core:*`
- `:presentation:*` -> `:core:*`, `:data:*:api`
- `:data:*:impl` -> `:data:*:api`, `:core:network`, `:core:db`
- `:core:*` -> external libraries only (no project feature/data dependencies)

Current exceptions (if any):
- `:presentation:platform` depends on `:data:game:api` types, which makes it "shared UI for this app" rather than a fully generic platform layer.

### Placement of business logic and side effects
- Business logic (screen-level decisions and orchestration) primarily lives in feature ViewModels and feature-level state/event reducers.
- Side effects (network and database IO) live in data implementations and core infrastructure modules (RemoteMediator, DAOs, Retrofit clients).
- Mapping is layered:
  - Network DTO -> API model: in `data/*/impl` mapper classes.
  - API model -> DB entities/cross-refs: in `data/*/impl` mapper classes.
  - DB entities -> API model: in `data/*/impl` mapper classes.
  - API model -> UI model: in `presentation/*` mapper classes.
- Navigation belongs to `:app` for graph ownership and to `:feature:*` for feature-local route definitions and UI event emission.
- Logging and analytics are not established as a dedicated cross-cutting layer in the current code; infrastructure logging is primarily via the HTTP logging interceptor.

## Forbidden or avoided practices
Forbidden or avoided:
- `:feature:*` -> `:data:*:impl` (features should depend on interfaces, not implementations)
- `:presentation:*` -> `:data:*:impl` (presentation is UI-only)
- Avoid: `:data:*:api` depends on Android or runtime-heavy core modules; prefer pure Kotlin contracts and keep contracts lightweight and reusable.
- `:core:*` -> `:app` / `:feature:*` / `:presentation:*` / `:data:*` (core must remain foundational)

## Consequences
Positive:
- Clear dependency direction enables feature growth without turning the app into a monolith.
- API/impl split for data allows features to depend on stable interfaces and improves testability.
- Paging + Room + RemoteMediator provides a robust offline-first list pattern.
- Convention plugins reduce per-module build boilerplate and standardize DI/test setup.

Negative:
- No explicit "domain" module means API models act as the shared business model across layers, which can increase coupling between UI and data contracts.
- Hilt is applied broadly (including non-UI/core modules), which can make DI feel ubiquitous even for purely functional code.
- Module boundaries are only enforced by convention and review; there is no explicit architectural enforcement tool.

Operational impact:
- Builds require providing the RAWG API key via environment variable or Gradle property.
- Persistence uses destructive migration fallback, which can lead to local data loss across schema changes.
- Network stack configuration and error semantics are centralized, so changes can have broad impact across features.

## Compliance checklist
(PR and LLM)
- New feature code lives under `:feature:*`; reusable UI goes to `:presentation:*`; shared foundations go to `:core:*`.
- Feature modules depend on `data/*/api` only, never on `data/*/impl`.
- Repository interfaces and shared models live in `data/*/api`; implementations and IO live in `data/*/impl`.
- ViewModels expose state as `StateFlow` and emit one-off actions as event `Flow`; Compose collects both with lifecycle awareness.
- Side effects stay out of ViewModels and UI: no direct Retrofit/OkHttp/Room/DAO usage outside data/core.
- Mapping is explicit and injectable: mapper interfaces + `*Impl` classes, bound via Hilt modules in the appropriate component scope.
- Paging list flows use a single pattern: Room paging source + RemoteMediator + repository Flow.
- Navigation graph is owned in `:app`; features emit navigation intent events and define their own route strings.
- Unit tests use shared drivers/helpers from `:testing:unit` and the feature-level test driver pattern.
- Secrets are not committed: provide required API keys through environment/Gradle properties and validate CI configuration.

## Evidence index
Modules and build logic:
- `settings.gradle.kts`
- `build.gradle.kts`
- `build-logic/**`
- `**/build.gradle.kts`
- `gradle/libs.versions.toml`

Feature example (`:feature:game`) flow:
- `app/src/main/**`
- `feature/game/src/main/**`
- `presentation/**/src/main/**`
- `data/game/**/src/main/**`

DI:
- `**/src/main/**/di/**`

Networking and persistence:
- `core/network/src/main/**`
- `core/db/src/main/**`

Tests:
- `feature/game/src/test/**`
- `testing/unit/src/main/**`

## Open questions
- Is :presentation:platform intended to be feature-agnostic long-term, or is it intentionally coupled to data/game/api types for now?
- Should the API-level models in data/*/api be treated as "domain models", or does the project want an explicit domain layer/module later?
- What is the desired policy for persistence migrations given the current use of destructive migrations?
- Is there a planned cross-cutting layer for analytics/logging beyond HTTP logging (and if so, where should it live)?
