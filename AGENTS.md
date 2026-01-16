## Working agreements

- Run terminal commands via Android Studio (`mcp__android_studio`), not direct shell.
- Use verification reports to fix code checks issues (Lint, Detekt, Ktlint, Unit tests) instead of
  terminal output.
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

- Use the IntelliJ MCP integration (`mcp__android_studio` server) for all file inspection, edits,
  searches, and run shell commands.
- For investigating problems with specific files, use `mcp__android_studio__get_file_problems` to
  inspect file errors and warnings.
- Use the shell bridge (`functions.shell`) only when you must run a CLI command; always set
  `workdir` to the project root.
- Use the GitHub automations (`mcp__github`/`mcp__github_personal` servers) for any repository
  interaction such as issues, branches, commits, or pull requests—never manipulate Git directly via
  shell.
- Use the Atlassian MCP integration (`mcp__atlassian` server) for every Jira or Confluence lookup or
  update instead of hitting the APIs manually.
- Use the DeepWiki MCP integration (`mcp__deepwiki` server) whenever you need framework or library
  guidance; do not rely on ad-hoc internet searches for that information.
- Use the Figma MCP integration (`mcp__figma` server) for all design context, assets, and
  measurements rather than guessing UI details.
- Use the Maven Deps MCP integration (`mcp__maven_deps` server) to confirm dependency versions and
  availability instead of checking Maven Central manually.

## Build, Test, and Development Commands

- `./gradlew assembleDebug` compiles the dev flavour for local installs.
- `./gradlew codeQuality` runs detekt and ktlint across the whole repo.
- `./gradlew testDebugUnitTest` runs unit tests.
-

`./gradlew :app:testDebugUnitTest --tests "com.reedcouk.jobs.feature.profile.languages.data.LanguageRepositoryTest"`
runs a specific test class.

## Verification reports locations

- Unit Tests: `app/build/reports/tests/testDevDebugUnitTest/index.html`
- Lint: `app/build/reports/lint-results-devDebug.html`
- Detekt (txt): `build/reports/detekt/detekt.txt`
- Ktlint (txt): `build/reports/ktlint/ktlint.txt`

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
- Do not leave comments in source files. Code must be self-explanatory through clear naming, tests,
  and docs.

## Unit tests Guidelines

- All public methods should be tested
- Do not create manual stubs or fakes, use `mockito-kotlin` for mocking dependencies and
  verification.
- Do not use initialization methods (`@Before`). Initialize everything in class properties.
    - For additional setup, use `mockito-kotlin` DSL in property initialization, e.g.:
        ```kotlin
            private val getNewJobsCountUseCase: GetNewJobsCountUseCase = mock {
                onBlocking { getNewJobsCount() } doReturn GetNewJobsCountResult.NoRecentSearches
            }
        ```
- If the test class has a JUnit `@Rule` and the class under test must be initialized after the rule,
  declare the class under test with `by lazy { ... }` (since we don’t use `@Before`).
- When you need to use any Mockito matcher in a `verify(...)` call, use matchers for all arguments (
  use `eq(...)` to check specific arguments).
- Prefer verifying exact arguments (e.g. `verify(api).sendEvent(expectedRequest)`) instead of
  capturing with `argumentCaptor` when the expected request can be constructed upfront. Use captors
  only when the argument can’t be reasonably constructed or needs partial/dynamic assertions.
    - Example:
        ```kotlin
            whenever(api.sendEvent(JobEventRequest(listOf(dto1, dto2)))).thenReturn(NetworkResponse.Success(Unit))
            repository.sendEvent(listOf(event1, event2))
            verify(api).sendEvent(JobEventRequest(listOf(dto1, dto2)))
        ```
- Prefer comparing objects with `assertEquals` when possible instead of asserting individual fields
  repeatedly.
- All unit tests must use a test driver/DSL abstraction to reduce boilerplate;
    - For tests without UI:
      `feature/game/src/test/kotlin/io/github/onreg/feature/game/impl/GamesViewModelTest.kt`
      `feature/game/src/test/kotlin/io/github/onreg/feature/game/impl/GamesViewModelTestDriver.kt`
    - For compose tests:
      `presentation/game/src/test/kotlin/io/github/onreg/ui/game/presentation/components/list/GameListTest.kt`
      `presentation/game/src/test/kotlin/io/github/onreg/ui/game/presentation/components/list/GameListTestDriver.kt`