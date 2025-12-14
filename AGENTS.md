# Repository Guidelines

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

## Build, Test, and Development Commands

- `./gradlew assembleDebug` – compile the debug APK with the convention-managed SDK + Compose
  stack.
- `./gradlew connectedDebugAndroidTest` – execute instrumentation/Compose UI suites on an attached
  device via `AndroidJUnitRunner`.
- To update project dependencies, run `./gradlew versionCatalogUpdate`, then build the project to
  verify. If build errors appear, try other version combinations. To check other dependency versions
  use `mcp__playwright`.

- You should run these commands with escalated permissions.
- For running commands set timeout to 5 minutes if it not enough then increase it accordingly.

## Preferred Tools

- Use the IntelliJ MCP integration (`mcp__android_studio` server) for all file inspection, edits,
  searches, and run commands before reaching for shell commands.
- For investigating problems with specific files, use `mcp__android_studio__get_file_problems` to
  inspect file errors and warnings.
- Use the shell bridge (`functions.shell`) only when you must run a CLI command; always set
  `workdir` to the project root.
- Use the GitHub automations (`mcp__github`/`mcp__github_personal` servers) for any repository
  interaction such as issues, branches, commits, or pull requests—never manipulate Git directly via
  shell.
- Use the Atlassian MCP integration (`mcp__atlassian` server) for every Jira or Confluence lookup or
  update instead of hitting the APIs manually.
- Use the Playwright MCP integration (`mcp__playwright` server) for every web lookup—never open
  browsers
  or external search manually.
- Use the DeepWiki MCP integration (`mcp__deepwiki` server) whenever you need framework or library
  guidance; do not rely on ad-hoc internet searches for that information.
- Use the Figma MCP integration (`mcp__figma` server) for all design context, assets, and
  measurements
  rather than guessing UI details.

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
  interface. Use `map` as the function name and `model` as
  the parameter name.
    - Example: `interface FooMapper { fun map(model: FooEntity): Foo }` and
      `class FooMapperImpl : FooMapper { override fun map(model: FooEntity): Foo = ... }`
- Do not leave comments in source files. Code must be self-explanatory through clear naming, tests,
  and docs.
- Do not add empty line at the end of the file.

## Testing Guidelines

Place JVM tests in `src/test/kotlin` and rely on JUnit4 plus kotlinx-coroutines-test. Compose or
instrumentation suites live in `src/androidTest/kotlin` and should mirror the screen/component
name (`GameCardTest`, `AppHeaderScreenshotTest`). Every feature change needs at least one unit test
and, for behavioral UI work, an accompanying instrumentation or screenshot check. Keep fixtures
module-scoped via `Fake*` helpers or `src/test/resources` data to avoid cross-module leakage.

## Commit & Pull Request Guidelines

Follow the existing history: imperative, component-focused subject lines (“Refactors GameCard
component…”) kept under ~72 characters. PRs must include a short summary, linked issue/discussion,
screenshots or recordings for UI changes, and the command/output of the tests you ran. Rebase on
`main`, avoid force-pushing after review starts, and flag configuration or migration follow-up
directly in the description.

## Security & Configuration Tips

Keep secrets (API keys, endpoints) in git-ignored `local.properties` or Gradle-managed env
files—never commit them. Convention plugins pin Java/Kotlin 17 with minSdk 24 and target/compile 36,
so align your Android Studio JDK with those values. Update `proguard-rules.pro` whenever you add
libraries that perform reflection or dynamic loading, and document runtime permissions or network
domains in module READMEs.
