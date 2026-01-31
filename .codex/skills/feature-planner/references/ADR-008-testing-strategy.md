# ADR-008: Testing Strategy

- Status: Accepted
- Date: 2026-01-12
- Project: NextPlay

## Context
NextPlay is a multi-module Android app with a clear separation between UI, feature orchestration, data contracts, and data implementations. Testing exists across several modules, with most automated coverage living in JVM unit tests.

The repository also includes build-logic convention plugins that standardize how test dependencies are wired into modules.

This ADR documents the current, "as-is" testing approach evidenced in this repository.

## Decision
The current testing strategy is:
- Default to fast JVM unit tests for business logic, mapping, repositories, and data layer orchestration.
- Use Robolectric-backed JVM tests when Android framework types or resources are needed (for example, Room, resources, or Compose UI tests on the JVM).
- Keep instrumentation tests minimal (currently limited to a template example).
- Centralize common unit test utilities and dependencies in a shared test module and apply it via convention plugins.

## Overview
### Testing pyramid and scope
- Unit tests (dominant):
  - Pure Kotlin logic: mappers, repository orchestration, paging orchestration, interceptors.
  - Room DAOs: executed as JVM tests using an in-memory database plus AndroidX test APIs.
  - Paging: validated via paging snapshot utilities and direct PagingSource load calls.
- UI tests:
  - Compose UI tests exist as JVM tests (Robolectric runner plus Compose test rule) that validate Composables via semantics tags and callbacks.
  - There is no evidence of device-level Compose UI tests beyond a basic instrumentation template.
- Integration tests (partial):
  - Some tests exercise multi-component behavior (for example, RemoteMediator logic interacting with DAOs and API abstractions) but rely on mocks rather than real Retrofit or real networking.

## Design and conventions
### Tooling and frameworks (as used in this repo)
- Test runtime and assertions:
  - JUnit 4 is used as the primary test runner API.
  - Kotlin test assertions are used in many tests alongside JUnit assertions in some cases.
- Mocking:
  - Mockito and Mockito-Kotlin are used for mocks, stubbing, and verification.
- Coroutines:
  - kotlinx-coroutines-test is used with runTest.
  - Dispatchers.Main control is provided via a custom JUnit rule using a TestDispatcher.
- Robolectric and AndroidX test:
  - Robolectric is present and used for JVM tests that require Android resources and for Compose UI tests running on the JVM.
  - AndroidX test core and the AndroidX JUnit runner are used in JVM tests that need an Android context (via ApplicationProvider).
- Compose UI testing:
  - Compose UI testing uses the Compose JUnit4 rule and semantics queries.
- Paging test utilities:
  - androidx.paging.testing asSnapshot is used to snapshot PagingData streams.

Items present in dependency catalogs but not evidenced in scanned tests should be treated as "not confirmed in use".

### Module-level strategy
- Unit-tested modules (in scanned sources):
  - :core:db covers DAO behavior with in-memory Room database tests.
  - :core:network includes unit tests for networking-related logic (for example, interceptors).
  - :data:game:impl tests mapping, repository behavior, and RemoteMediator behavior using mocks and paging snapshots.
  - :feature:game includes ViewModel tests and Compose UI tests for feature screens/panes.
  - :presentation:game includes UI mapping tests and Compose component tests.
  - :presentation:platform includes mapper tests.
- Modules with limited or no current test coverage (in scanned sources):
  - :app contains an instrumentation template test.
  - :data:game:api is a contract module and does not show unit tests in scanned sources.
  - :core:ui contains a test source set but no Kotlin test files were found in scanned sources.
- The role of :testing:unit:
  - Provides shared unit test dependencies via an API surface (coroutines-test, paging-testing, Mockito, JUnit, Kotlin test).
  - Provides small utilities used from tests, including a Main dispatcher JUnit rule, Flow observation helpers, and a PagingData snapshot helper.

### Patterns and conventions
- Test drivers/DSL usage:
  - Several test suites use a "TestDriver" or builder pattern to reduce boilerplate and make stubbing explicit.
  - Drivers often construct the subject under test lazily to keep setup declarative and compatible with JUnit rules.
  - Not all tests follow this pattern consistently (some are direct "arrange-act-assert" without a driver).
- Flow and StateFlow testing approach:
  - A custom Flow test helper collects emissions in a TestScope and provides "assertLatest" and "latestValue" style assertions, advancing the scheduler for determinism.
- Paging testing approach:
  - PagingData is validated using snapshotting (asSnapshot) to assert item lists.
  - Some tests validate PagingSource behavior by calling load directly and asserting LoadResult types and contents.
- Fake vs mock guidance (as used today):
  - Mockito-Kotlin mocks are the default for dependencies and collaborators.
  - Small in-test fakes exist for simple abstractions where a minimal implementation is used instead of mocking.

### DI and testability
- Production code uses Hilt for dependency injection, but tests do not appear to use Hilt testing facilities.
- The common pattern in tests is direct construction of the class under test with mocked dependencies (or minimal in-test fakes), avoiding DI graph setup.

### Persistence and networking test coverage
- Persistence (Room):
  - DAO behavior is tested using an in-memory Room database and an AndroidX-provided application context.
  - Tests explicitly allow main thread queries and close the database in teardown.
- Networking:
  - Networking behavior is primarily validated through unit tests of small components (for example, request interceptors) using mocks.
  - There is no evidence in scanned sources of end-to-end Retrofit serialization tests or MockWebServer-based tests.

### Reliability and flakiness controls
- Coroutines determinism:
  - runTest is used for coroutine-based tests.
  - A Main dispatcher JUnit rule is available and used where Dispatchers.Main must be controlled.
  - Flow testing utilities advance the scheduler to reduce race conditions.
- Compose UI tests:
  - Compose test rules are used with deterministic assertions and "runOnIdle" checks for callback verification.
- Paging determinism:
  - Paging snapshots are used to turn PagingData into stable lists for assertions.

## Consequences
- Positive:
  - Most tests run as JVM tests, which is typically faster and easier to run locally.
  - Shared utilities and convention plugins reduce per-module setup and encourage consistent patterns.
  - Paging and Compose behaviors are covered without relying on device tests.
- Negative:
  - Limited device-level coverage means issues tied to real devices, real rendering, and system integration may be missed.
  - Mock-heavy tests can miss Retrofit wiring, serialization issues, and other end-to-end behaviors.
  - Mixed assertion styles and partial adoption of test drivers can reduce consistency.
- Operational impact:
  - Robolectric and Android resource dependent tests require correct Android test configuration and can be slower than pure JVM tests.
  - Maintaining test drivers and shared helpers becomes part of ongoing test maintenance.

## Compliance checklist
(PR and LLM)
- Do:
  - Add tests in the closest module to the behavior being validated (for example, repository impl tests in :data:*:impl, UI mapper tests in :presentation:*).
  - Use the shared unit test utilities module for coroutine, Flow, and paging test helpers when applicable.
  - Prefer Mockito-Kotlin mocks and explicit stubbing and verification.
  - Use runTest for suspend functions and coroutine flows.
  - When validating PagingData, prefer snapshot-based assertions.
  - For Compose component tests on the JVM, use the Compose test rule and query nodes via test tags or stable text resources.
  - Keep setup declarative via builders or drivers when the test suite already uses them.
- Do not:
  - Introduce new ad-hoc Flow testing libraries unless there is existing usage in the repo.
  - Add device tests for logic that can be covered by JVM tests, unless the behavior is truly device-specific.
  - Mix Mockito matchers and raw arguments in the same verify call.
  - Add shared test helpers into production modules when they are test-only utilities.

## Evidence index
- docs/adr/ADR-002-dependency-injection-strategy.md
- build-logic/convention/src/main/kotlin/core/UnitTestConventionPlugin.kt
- build-logic/convention/src/main/kotlin/core/AndroidTestConventionPlugin.kt
- build-logic/convention/src/main/kotlin/presets/FeatureConventionPlugin.kt
- build-logic/convention/src/main/kotlin/presets/NonUiConventionPlugin.kt
- build-logic/convention/src/main/kotlin/presets/UiConventionPlugin.kt
- testing/unit/build.gradle.kts
- testing/unit/src/main/kotlin/io/github/onreg/testing/unit/coroutines/MainDispatcherRule.kt
- testing/unit/src/main/kotlin/io/github/onreg/testing/unit/flow/FlowTest.kt
- testing/unit/src/main/kotlin/io/github/onreg/testing/unit/flow/TestObserver.kt
- testing/unit/src/main/kotlin/io/github/onreg/testing/unit/paging/PagingSnapshot.kt
- **/src/test/**/*.kt
- **/src/androidTest/**/*.kt
- core/db/src/test/kotlin/io/github/onreg/core/db/game/dao/GameDaoTest.kt
- core/network/src/test/kotlin/io/github/onreg/core/network/rawg/interceptor/RawgApiKeyInterceptorTest.kt
- data/game/impl/src/test/kotlin/io/github/onreg/data/game/impl/paging/GameRemoteMediatorTest.kt
- feature/game/src/test/kotlin/io/github/onreg/feature/game/impl/GamesPaneViewModelTest.kt
- feature/game/src/test/kotlin/io/github/onreg/feature/game/impl/pane/GamesPaneTest.kt
- presentation/game/src/test/kotlin/io/github/onreg/ui/game/presentation/components/list/GameListTest.kt
- app/src/androidTest/kotlin/io/github/onreg/nextplay/ExampleInstrumentedTest.kt

## Open questions
- Is there an intended threshold for when a behavior should be covered by a device instrumentation test rather than a Robolectric JVM test?
- Are MockWebServer or screenshot testing tools planned for adoption, or intentionally avoided?
- Are there CI requirements for running unit tests and reporting results that should be documented alongside this strategy?
