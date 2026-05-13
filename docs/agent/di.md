## DI rules (Hilt modules)

Applies when editing dependency injection setup (`**/di/**`, `*Module.kt`, or `@Module`/`@InstallIn` usage).

### Where bindings live

- `data/<domain>/impl/.../di/**`:
  - binds repository `impl -> api` contract;
  - binds DTO/entity mappers used by that data module;
  - installs into `SingletonComponent`.
- `presentation/<domain>/.../di/**`:
  - binds UI mappers and other view-model-scoped presentation utilities;
  - installs into `ViewModelComponent`.
- `feature/<domain>/.../impl/di/**`:
  - binds feature-internal mappers used by the feature ViewModel/pane;
  - installs into `ViewModelComponent`.
- `core/**/di/**`:
  - provides shared singletons (db, network, formatters, android utils);
  - installs into `SingletonComponent`.

### Prefer `@Binds` over `@Provides`

- Use `@Binds` for `interface -> implementation` bindings.
- Use `@Provides` only for:
  - third-party types you cannot annotate with `@Inject`;
  - runtime factories (for example `RemoteMediator` factories);
  - configuration objects (for example `PagingConfig`).
- Keep modules aligned with existing style:
  - `abstract class` module + `companion object` for `@Provides` is the default when both binds and provides are needed.

### Scoping rules

- Use `@Singleton` only for app-wide, long-lived dependencies:
  - repositories in `data/*/impl`;
  - shared `PagingConfig` when multiple consumers use it;
  - DB and network singletons.
- Do not over-scope ViewModel-scoped dependencies:
  - `ViewModelComponent` bindings typically do not need `@Singleton`.

### Runtime parameters and factories

- If an object needs runtime parameters (for example a `gameId`), provide a `fun interface` factory:
  - `public fun interface XxxFactory { fun create(id: Int): Xxx }`
  - Provide it from DI using `@Provides`.
- If there is no runtime parameter but you want lazy construction, prefer injecting `Provider<T>`.

### Verification

After completing DI changes, verify with this checklist:

- Bindings live in the correct module (`data/*/impl`, `presentation/*`, `feature/*`, `core/*`).
- `@InstallIn` matches lifetime (`SingletonComponent` for app singletons, `ViewModelComponent` for VM-scoped deps).
- Interfaces use `@Binds`; `@Provides` is used only for factories/config/third-party types.
- Runtime-parameter objects are constructed through factories (not by leaking parameters into unrelated scopes).

