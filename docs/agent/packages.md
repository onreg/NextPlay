## Packages and directories

Applies when adding new Kotlin source files or moving code between packages.

Put new files in the package/folder that matches the layer and role of the type.
Prefer package names that mirror the directory structure.

### App host (`:app`)

- `app/src/main/kotlin/io/github/onreg/nextplay/**`
- Navigation host and routes live here (see `navigation.md`).

### Feature modules (`:feature:*`)

- `feature/<domain>/src/main/kotlin/io/github/onreg/feature/<domain>/**/impl/**`
- Common subpackages:
  - `.../impl/model/**` for screen state, events, UI models owned by the feature.
  - `.../impl/mapper/**` for feature-specific mapping.
  - `.../impl/di/**` for ViewModel-scoped bindings.
  - `.../impl/pane/**` for `*Pane`/`*Screen` wiring.
  - `.../impl/pane/component/**` for state-branch components.
  - `.../impl/test/**` (in `src/main`) for `*TestTags`/`*TestData` used by previews and tests.
- Tests live under `feature/<domain>/src/test/**` and `feature/<domain>/src/androidTest/**`.

### Presentation modules (`:presentation:*`)

- `presentation/<domain>/src/main/kotlin/io/github/onreg/ui/<domain>/presentation/**`
- Common subpackages:
  - `.../components/**` for reusable UI components.
  - `.../components/**/test/**` (in `src/main`) for component `*TestTags`/`*TestData`.
  - `.../mapper/**` for API-model -> UI-model mapping.
  - `.../di/**` for ViewModel-scoped presentation bindings.
  - `.../model/**` for reusable UI models.
- Tests live under `presentation/<domain>/src/test/**`.

### Data modules (`:data:*`)

- Contracts: `data/<domain>/api/src/main/kotlin/io/github/onreg/data/<domain>/api/**`
  - `.../model/**` for domain/API models.
  - Repository interfaces live at `.../api/*Repository.kt`.
- Implementations: `data/<domain>/impl/src/main/kotlin/io/github/onreg/data/<domain>/impl/**`
  - Repository implementation at `.../impl/*RepositoryImpl.kt`.
  - `.../impl/mapper/**` for DTO/entity mapping.
  - `.../impl/paging/**` for `RemoteMediator`/paging helpers.
  - `.../impl/di/**` for singleton bindings and factories.

### Core DB (`:core:db`)

- `core/db/src/main/kotlin/io/github/onreg/core/db/**`
- Common subpackages:
  - `.../<domain>/entity/**` for Room entities/cross refs/remote keys.
  - `.../<domain>/dao/**` for DAOs.
  - `.../<domain>/model/**` for relation models and insertion bundles.
  - `.../di/**` for database and DAO providers.

### Core Network (`:core:network`)

- `core/network/src/main/kotlin/io/github/onreg/core/network/**`
- Common subpackages:
  - `.../rawg/api/**` for Retrofit APIs.
  - `.../rawg/dto/**` for DTOs.
  - `.../di/**` for network modules/providers.

### Core UI (`:core:ui`)

- `core/ui/src/main/kotlin/io/github/onreg/core/ui/**`
- Components under `.../components/**`, theme under `.../theme/**`, previews under `.../preview/**`.

### Test support (`:testing:unit`)

- Shared test utilities live under `testing/unit/src/main/kotlin/io/github/onreg/testing/unit/**`.

### Verification

Before finishing a change that adds new Kotlin source files, verify:

- New types are placed in the correct package/directory for their layer and role.

