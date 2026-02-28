## Code organization (Kotlin)

This document defines lightweight rules for Kotlin code organization: interface placement, mapper
structure, and UI model naming.

### Interfaces

- Prefer a separate file when:
  - The interface is part of a **public contract** of a module (especially `data/<domain>/api`), and
    is expected to be consumed by other modules.
  - The interface is expected to have **multiple implementations** (now or soon), or implementations
    live in different modules.
- Prefer co-location (same `.kt` file) when:
  - The interface is `internal` (or effectively internal by module boundaries) and has **a single
    implementation**.

### Mapper rules

- Do not implement mappers as extension functions. Use an interface + implementation class (e.g.,
  `PlatformUiMapper` + `PlatformUiMapperImpl`) to keep mapping logic injectable and testable.
- Co-locate the mapper interface and its primary implementation in the same file when the mapper is
  `internal` and has a single implementation.

### Naming convention

- When co-locating, name the file after the interface (e.g., `GameEntityMapper.kt`) and keep the
  implementation name explicit (e.g., `GameEntityMapperImpl`).

### UI model naming

- UI models (presentation-layer types that represent UI state for a screen/component) must end with
  `Ui`, for example `GameCardUi`.
