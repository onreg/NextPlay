## Code organization (Kotlin)

This document defines lightweight rules for Kotlin code organization when adding new Kotlin source
files: interface placement, file boundaries, and naming.

For package/directory placement rules, see: `packages.md`.

If the new file is a mapper/DI/paging/schema/navigation change, also follow the specialized docs:
- Mappers: `mapper.md`
- DI: `di.md`
- Paging data layer: `paging-data.md`
- Room schema: `room-schema.md`
- App navigation host: `navigation.md`

### Interfaces

- Prefer a separate file when:
  - The interface is part of a **public contract** of a module (especially `data/<domain>/api`), and
    is expected to be consumed by other modules.
  - The interface is expected to have **multiple implementations** (now or soon), or implementations
    live in different modules.
- Prefer co-location (same `.kt` file) when:
  - The interface is `internal` (or effectively internal by module boundaries) and has **a single
    implementation**.

### Files and boundaries

- Prefer one “primary” top-level type per file.
  - Co-location is allowed for tight pairs like `interface` + `Impl` when the interface is `internal`
    and has a single implementation.
- Name files after the primary type they contain.
- Keep public API types in stable packages, and keep implementations in `impl` packages/folders when
  the module structure uses that split.

### Naming convention

- When co-locating `interface` + implementation, name the file after the interface (for example
  `GameEntityMapper.kt`) and keep the implementation name explicit (for example
  `GameEntityMapperImpl`).
- Follow these suffix conventions (matching existing code) when adding new types:
  - Room entities: `XxxEntity`, remote keys: `XxxRemoteKeysEntity`, cross refs: `XxxCrossRef`.
  - Network DTOs: `XxxDto` (`core/network/.../dto/**`).
  - Repositories: `XxxRepository` in `data/*/api`, `XxxRepositoryImpl` in `data/*/impl`.
  - Paging mediators: `XxxRemoteMediator` and factories as `XxxRemoteMediatorFactory`.
  - DI: `XxxModule` for Hilt modules under `.../di/**`.
  - Tests: `XxxTest`, `XxxTestDriver`, `XxxTestData`, `XxxTestTags`.

### UI model naming

- New UI models (types that represent UI state for a screen/component) should end with `Ui`
  (for example `GameDetailsUi`).
- Legacy types may already use `UI` suffix (for example `GameCardUI`, `ContentInfoUI`). Do not rename
  existing types as part of unrelated changes.

### Verification

Before finishing a change that adds new Kotlin source files, verify:

- New interfaces follow the separate-file vs co-location rules.
- File names match the primary types in the file.
- New UI model naming follows `...Ui` for new types, without renaming legacy `...UI` types.
- New types are placed in the correct package/directory for their layer and role (`packages.md`).
