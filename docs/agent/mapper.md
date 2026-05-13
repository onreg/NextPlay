## Mapper rules

Applies when editing mappers (`*Mapper*.kt`, folders named `mapper`).
This covers mapping between:
- network DTOs -> domain/API models;
- database entities/relations -> domain/API models;
- domain/API models -> UI models.

### Placement and dependency direction

- Data-layer mapping lives in `data/<domain>/impl/.../mapper/**`.
  - DTO mappers convert `core/network` DTOs into `data/<domain>/api` models (or intermediate models used for persistence).
  - Entity mappers convert `core/db` entities/relations into `data/<domain>/api` models and produce insertion bundles.
- Presentation-layer mapping lives in `presentation/<domain>/.../mapper/**`.
  - UI mappers convert `data/<domain>/api` models into `...Ui` models.
- Feature-layer mapping is allowed only when it is feature-specific and does not belong in reusable `presentation/*`.

### Shape and injection

- Mappers are `interface` + `Impl` class, injected via constructor (`@Inject`).
- Keep mapping functions pure:
  - no IO;
  - no DB writes/reads;
  - no coroutine launching;
  - no mutable shared state.
- Keep mapping logic testable without Android runtime:
  - Data mappers must not depend on Android types.
  - UI mappers may depend on formatting helpers/providers that are already part of the presentation layer dependencies.

### Naming and file layout

- Use `XxxMapper` + `XxxMapperImpl` naming.
- Name the file after the interface when co-locating interface + implementation (`XxxMapper.kt`).
- UI models end with `Ui` (for example `GameDetailsUi`).

### Tests

- Add/update mapper tests under the same module’s `src/test`.
- Prefer whole-model equality assertions (`assertEquals(expected, actual)`).
- For `PagingData` mapping, snapshot to a list before asserting:
  - `Flow<PagingData<T>>` -> `androidx.paging.testing.asSnapshot()`;
  - `PagingData<T>` -> `io.github.onreg.testing.unit.paging.asSnapshot()`.

### Verification

After completing mapper changes, verify with this checklist:

- Mapping code lives in the correct layer (`data/*/impl` vs `presentation/*`).
- Mapper is an injectable interface + implementation (no extension-function mapper).
- Mapping functions stay pure (no IO, DB, or state writes).
- Mapper tests exist/are updated and assert on whole mapped models (paging uses snapshot helpers).
