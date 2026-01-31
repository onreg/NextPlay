## Working agreements

- Use verification reports to fix code checks issues (Lint, Detekt, Ktlint, Unit tests) instead of
  terminal output.
- Do not leave comments in source files. Code must be self-explanatory through clear naming, tests,
  and docs.
- After any code change, run code checks and unit tests appropriate to the scope of the change.
    - For large/multi-file changes (e.g. implementing a whole feature), run detekt + ktlint and all
      Unit tests.
    - For small/isolated changes (e.g. changing a single class), run detekt + ktlint and only the
      single most relevant Unit test for that class (e.g. one test class), not the full suite.

## Project Structure & Module Organization

- `build-logic/convention/` centralizes Gradle convention plugins and shared build scripts for the
  entire workspace.
- `app/` assembles the installable experience by wiring together feature, core, and data modules.
- `core/ui/` offers reusable, stateless Compose components plus theme primitives.
- `core/db/` contains persistence helpers such as DAOs, entities, and adapters for local storage.
- `core/network/` owns HTTP clients, interceptors, and serialization utilities for remote calls.
- `data/*/api` modules define business logic contracts (interfaces, models, repositories) shared
  with consumers.
- `data/*/impl` modules implement the business logic behind the corresponding API contracts.
- `feature/*/` modules host production-ready screens, presenters/view models, and navigation flows.
- All modules adhere to the standard `src/main`, `src/test`, and `src/androidTest` layout; keep
  previews, assets, and fixtures near their owners to preserve clear boundaries.

## Preferred Tools

- Use the Mobile MCP integration (`mcp__mobile-mcp` server) to interact with Android emulators/iOS simulators and real devices for UI verification and
  device-level testing (taps, typing, screenshots, app lifecycle).
- Use the GitHub automations (`mcp__github`/`mcp__github_personal` servers) for any repository
  interaction such as issues, branches, commits, or pull requests—never manipulate Git directly via
  shell.
- Use the Atlassian MCP integration (`mcp__atlassian` server) for every Jira or Confluence lookup or
  update instead of hitting the APIs manually.
- Use the DeepWiki MCP integration (`mcp__deepwiki` server) whenever you need framework or library
  guidance; do not rely on ad-hoc internet searches for that information.
- Use the Figma MCP integration (`mcp__figma` server) for all design context, assets, and
  measurements rather than guessing UI details.
- Use the Figma MCP integration (`mcp__figma` server) for all design context, assets, and measurements
  rather than guessing UI details.

## Build, Test, and Development Commands

- `./gradlew assembleDebug` compiles the dev flavour for local installs.
- `./gradlew codeQuality` runs static code analysis: detekt, ktlint, and lint across the whole repo.
- `./gradlew ktlintFormat` runs ktlint formatting across the whole repo.
- `./gradlew testDebugUnitTest` runs unit tests.
- `./gradlew :app:testDebugUnitTest --tests "com.reedcouk.jobs.feature.profile.languages.data.LanguageRepositoryTest"` runs a specific test class.

## Verification reports locations

- Unit Tests: `app/build/reports/tests/testDevDebugUnitTest/index.html`
- Lint: `build/reports/lint/lint.txt`
- Detekt: `build/reports/detekt/detekt.txt`
- Ktlint: `build/reports/ktlint/ktlint.txt`
- Ktlint: `build/reports/ktlint/ktlint-format.txt`

## Coding Style & Naming Conventions

- Follow Kotlin official style with 4-space indentation; prefer expression-bodied functions when
  they improve clarity.
- Compose previews describe state only (e.g., `FilledPreview`).
- Icons follow `ic_name_size.xml`.
- User action events in ViewModels: Use `on` prefix.
    - Example: `fun onSaveClicked()`
- Boolean Variables: Use `is`, `has`, or `should` prefixes.
    - Example: `isUserLoggedIn`, `hasProfilePicture`, `shouldShowTooltip`
- Mapper Function: Must be implemented as classes (not extension functions) that implement an
  interface. Use `map` as the function name and `model` as the parameter name.
    - Example: `interface FooMapper { fun map(model: FooEntity): Foo }` and
      `class FooMapperImpl : FooMapper { override fun map(model: FooEntity): Foo = ... }`
