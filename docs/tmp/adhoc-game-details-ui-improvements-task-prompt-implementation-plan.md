# Game Details UI Improvements and State Restoration Fixes Implementation Plan

## Overview
Update the game details screen UI to match the styling and formatting of the game list card, introduce a reusable text-only button component, apply consistent spacing rules, and fix back-navigation state restoration issues when drilling into series items.

- Relevant ADRs:
  - ADR-001 (Application Architecture): keep changes within existing module responsibilities; prefer reusable UI in `:core:ui` and feature orchestration in `:feature:game-details`.
  - ADR-008 (Testing Strategy): add/adjust JVM unit tests (including Robolectric-backed Compose UI tests) close to the behavior being changed.
- ADR Conflicts: None identified.
- Assumptions:
  - The game details destination uses Compose Navigation back stack entries (confirmed in `app/src/main/kotlin/io/github/onreg/nextplay/MainActivity.kt`), so each details screen instance should have its own `GameDetailsViewModel`.
  - APK output path for debug builds is `app/build/outputs/apk/debug/app-debug.apk` (verify after build if Gradle output differs).
  - A new “play” vector drawable can be added under `core/ui/src/main/res/drawable/` (or an existing icon can be reused if discovered during implementation).
- Open Questions: None (Task Prompt states none).

## Files to Modify
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPane.kt` - update top app bar usage and unify screen spacing rules.
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneSections.kt` - banner edge-to-edge, meta section layout, platform icons, text colors, description toggle, media thumbnail sizing, video overlay, series item card updates.
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneHelpers.kt` - improve section visibility logic to avoid “missing” sections on back navigation; keep logic testable.
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneLoadingSections.kt` - increase placeholder thumbnail sizes to match new media sizes and series width.
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/ui/mapper/GameDetailsUiMapper.kt` - align release date formatting and timezone assumptions with the list mapper.
- `presentation/game-list/src/main/kotlin/io/github/onreg/ui/game/list/presentation/components/card/GameCard.kt` - (refactor-only) allow reuse for series items without bookmark control and with configurable sizing while keeping game list behavior unchanged.
- `presentation/game-list/src/main/kotlin/io/github/onreg/ui/game/list/presentation/mapper/GameUiMapper.kt` - reuse shared release-date formatter so details and list stay consistent.
- `feature/game-details/src/test/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsScreenTest.kt` - update/extend Compose UI tests for “TextButton”, spacing/layout expectations, and video overlay.
- `feature/game-details/src/test/kotlin/io/github/onreg/feature/game/details/impl/ui/mapper/GameDetailsUiMapperTest.kt` - assert release date formatting matches list behavior (pattern + UTC assumptions).
- `feature/game-details/src/test/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneHelpersTest.kt` - add tests for new section visibility rules (navigation regression coverage).
- `presentation/game-list/src/test/kotlin/...` (exact path to confirm during implementation) - update/add tests to cover new `GameCard` configuration without affecting list defaults.

## New Files to Create
- `core/ui/src/main/kotlin/io/github/onreg/core/ui/components/button/TextButton.kt` - reusable text-only button component (Material 3 text button style, ripple, no background).
- `core/ui/src/main/kotlin/io/github/onreg/core/ui/format/ReleaseDateFormatter.kt` - shared formatter to keep list/details/series date formatting consistent.
- `core/ui/src/main/res/drawable/ic_play_24.xml` - play icon used for video thumbnail overlay (if no existing icon is available).
- `core/ui/src/test/kotlin/io/github/onreg/core/ui/format/ReleaseDateFormatterTest.kt` - unit tests for formatter behavior (UTC + pattern).

## Implementation Steps

### Step 1: Introduce a shared release date formatter (UTC + Locale.US)
- Where:
  - `core/ui/src/main/kotlin/io/github/onreg/core/ui/format/ReleaseDateFormatter.kt`
  - `presentation/game-list/src/main/kotlin/io/github/onreg/ui/game/list/presentation/mapper/GameUiMapper.kt`
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/ui/mapper/GameDetailsUiMapper.kt`
- What:
  - Add a small formatting utility that formats `Instant?` using:
    - pattern: `MMM d, yyyy`
    - timezone: UTC
    - locale: US
  - Replace the duplicated/private formatting logic in the list mapper and the details mapper to use the shared formatter.
- Why:
  - Meets AC3/AC9 and the Task Prompt “Data and mapping” constraint: details and series items must match list card date formatting and timezone assumptions.
- How (planned public signatures):
  ```kotlin
  package io.github.onreg.core.ui.format

  import java.time.Instant

  public object ReleaseDateFormatter {
      public fun format(releaseDate: Instant?): String
  }
  ```
- Outcome:
  - List cards, details metadata, and series items can share one formatter and tests can enforce consistent output.

### Step 2: Add design system `TextButton` component (text-only, ripple, min touch target)
- Where: `core/ui/src/main/kotlin/io/github/onreg/core/ui/components/button/TextButton.kt`
- What:
  - Create a reusable text-only button wrapper around Material 3 `TextButton`.
  - Ensure it meets Material minimum touch target expectations (48.dp) via modifier enforcement.
  - Provide a simple API for label text and click handling.
- Why:
  - Meets AC5/AC6/AC7: “Official Website” and “Read more/less” must use a consistent, accessible text-only button component.
- How (planned public signatures):
  ```kotlin
  package io.github.onreg.core.ui.components.button

  import androidx.compose.runtime.Composable
  import androidx.compose.ui.Modifier

  @Composable
  public fun TextButton(
      text: String,
      onClick: () -> Unit,
      modifier: Modifier = Modifier,
  )
  ```
- Outcome:
  - Feature and presentation modules can use a single component for tappable text actions with consistent behavior and hit targets.

### Step 3: Update game details top app bar to a standard Material 3 small top app bar
- Where: `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPane.kt`
- What:
  - Replace the current header usage with a Material 3 `SmallTopAppBar` (left-aligned title) to satisfy the “small” app bar requirement.
  - Keep the same back action semantics and title text source.
- Why:
  - Meets AC1: standard Material 3 small top app bar height/behavior (not oversized).
- Outcome:
  - Game details screen app bar matches the expected Material 3 “small” variant.

### Step 4: Make the banner image edge-to-edge and aligned to list card styling
- Where: `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneSections.kt`
- What:
  - Remove left/top/right padding around the banner image.
  - Keep aspect ratio at `2f` and derive height from width.
  - Ensure chip overlay positioning remains consistent (top end) without relying on extra padding.
- Why:
  - Meets AC2 and the Task Prompt banner prominence requirement.
- Outcome:
  - Banner is edge-to-edge horizontally and matches list card aspect ratio.

### Step 5: Replace the meta info card with a vertical list of rows (release date, platforms, website)
- Where: `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneSections.kt`
- What:
  - Remove the card container in `DetailsSection`.
  - Render three rows (when present) directly in the content:
    1) Release date
    2) Platforms
    3) “Official Website” action
  - Ensure platforms use the same icon size and spacing behavior as list (`IconsSize.sm`, spacing consistent with `Spacing.sm` usage in list card).
  - Replace the clickable “Official Website” text with the new `TextButton`.
- Why:
  - Meets AC3/AC4/AC5/AC6 and aligns details metadata styling with the game list reference.
- Outcome:
  - Metadata appears as a simple vertical list with consistent platform presentation and accessible tap targets.

### Step 6: Convert description expand/collapse action to use `TextButton` and enforce default collapsed behavior
- Where:
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPane.kt`
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneSections.kt`
- What:
  - Keep the default collapsed state for expandable descriptions.
  - Use `TextButton` for “Read more” / “Read less” instead of a clickable `Text`.
  - Ensure the action only appears when the text is expandable (existing overflow detection can remain, but should be robust after navigation/back stack restoration).
- Why:
  - Meets AC5/AC7 and improves consistency/usability of text actions.
- Outcome:
  - Description toggling behavior matches requirements and uses the shared design system component.

### Step 7: Update series game items (width +50%, no bookmark, smaller rating chip, list-consistent chip color and date format)
- Where:
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneSections.kt`
  - `presentation/game-list/src/main/kotlin/io/github/onreg/ui/game/list/presentation/components/card/GameCard.kt`
- What:
  - Increase series item width by 50% (e.g., from `160.dp` to `240.dp`).
  - Ensure series items do not render any bookmark icon/control.
  - Decrease rating chip size for series items while keeping its color behavior aligned with list rating chips.
  - Decrease title text size for series items.
  - Use the shared `ReleaseDateFormatter` for series item release dates (instead of `Instant.toString()`).
- Why:
  - Meets AC8/AC9 and “match list game card behavior” constraints, while keeping the list screen unchanged.
- How (planned structural changes; exact API finalized during implementation):
  - Extend the list `GameCard` component to support a “no bookmark” variant and sizing overrides without changing defaults:
    ```kotlin
    public fun GameCard(
        modifier: Modifier = Modifier,
        gameData: GameCardUI,
        onBookmarkClick: () -> Unit = {},
        onCardClicked: () -> Unit = {},
        showBookmark: Boolean = true,
        // Optional: allow small chip/title variants for series reuse
        variant: GameCardVariant = GameCardVariant.Default,
    )

    public enum class GameCardVariant {
        Default,
        Series,
    }
    ```
- Outcome:
  - Series cards visually align with list styling, meet the width requirement, and avoid unintended bookmark UI.

### Step 8: Increase media thumbnail sizes and add a play icon overlay for video thumbnails
- Where:
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneSections.kt`
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneLoadingSections.kt`
  - `core/ui/src/main/res/drawable/ic_play_24.xml`
- What:
  - Increase screenshot and movie thumbnail sizes by the same magnitude as the series width increase (targeting +50%).
  - Update loading placeholders to match the new thumbnail sizes.
  - Add a play icon overlay on video thumbnails (center overlay) with a readable contrast treatment.
- Why:
  - Meets AC11 and improves consistency with the “media overlays” guidance from the Task Prompt.
- Outcome:
  - Media sections show larger thumbnails and videos clearly indicate playability.

### Step 9: Fix back-navigation restoration issues for media/series sections (prevent “missing sections” and unstable series content)
- Where:
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneHelpers.kt`
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/GameDetailsViewModel.kt` (only if needed after investigation)
- What:
  - Make section visibility resilient to paging lifecycle transitions when a destination is backgrounded and later restored:
    - Treat “empty + NotLoading(endOfPaginationReached = false)” as a loading state (show placeholders), not as “hide”.
    - Hide only on explicit error, or when “empty + NotLoading(endOfPaginationReached = true)” indicates a truly empty dataset.
  - If the investigation shows paging flows are being recreated and triggering refreshes that mutate series content on return:
    - Ensure each `GameDetailsViewModel` instance creates its paging flows once for its `gameId` and does not reinitialize them on recomposition/return.
- Why:
  - Meets AC12: returning from a series details screen must restore the previous details instance with screenshots/movies visible and series content stable.
- How (testable helper design):
  ```kotlin
  internal fun resolveSectionVisibility(
      itemCount: Int,
      refreshLoadState: LoadState,
  ): SectionVisibility
  ```
- Outcome:
  - Sections no longer disappear incorrectly after back navigation; series/media stability issues are addressed and regression-tested.

### Step 10: Update and add automated tests (ADR-008 compliant)
- Where:
  - `core/ui/src/test/kotlin/io/github/onreg/core/ui/format/ReleaseDateFormatterTest.kt`
  - `feature/game-details/src/test/kotlin/io/github/onreg/feature/game/details/impl/ui/mapper/GameDetailsUiMapperTest.kt`
  - `feature/game-details/src/test/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsScreenTest.kt`
  - `feature/game-details/src/test/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneHelpersTest.kt`
  - `presentation/game-list/src/test/kotlin/...` (confirm exact existing test package during implementation)
- What:
  - Formatter tests: ensure UTC + `MMM d, yyyy` formatting.
  - Details mapper tests: verify details release date matches list formatting (including null handling).
  - Compose UI tests (Robolectric):
    - Verify the app bar semantics/title remain visible and back affordance exists.
    - Verify “Official Website” and “Read more/less” are rendered as clickable button-like nodes (using `TextButton`) and remain present when expected.
    - Verify media sections show thumbnails with updated sizing and that movies include a play overlay (via content description / test tag).
  - Helpers tests:
    - Add unit tests for the new section visibility resolver to prevent regressions of “missing media sections” after navigation.
- Why:
  - Enforces acceptance criteria behavior in fast JVM tests aligned with ADR-008.
- Outcome:
  - Regression coverage exists for the most failure-prone behaviors: mapping consistency, UI toggles, and section visibility across navigation lifecycles.

### Step 11: (Optional, if needed) Run the most relevant unit tests during implementation
- Why:
  - Catch regressions early when refactoring UI and shared components.
- Command to run:
  ```bash
  ./gradlew :feature:game-details:testDebugUnitTest
  ```

## Final Verification Steps (Required)

### Step N-4: Static analysis (reports-driven)
- Command to run:
  ```bash
  ./gradlew codeQuality
  ```
- Fix: Apply fixes based on `build/reports/detekt/detekt.txt`, `build/reports/ktlint/ktlint.txt`, and `build/reports/lint/lint.txt`, then re-run until clean.

### Step N-3: Unit tests (reports-driven)
- Command to run:
  ```bash
  ./gradlew testDebugUnitTest
  ```
- Fix: Apply fixes based on unit test reports (for example `app/build/reports/tests/testDebugUnitTest/index.html` and per-module reports), then re-run until green.

### Step N-2: Build an installable artifact
- Command to run:
  ```bash
  ./gradlew :app:assembleDebug
  ```
- Outcome: A debug APK is produced (expected: `app/build/outputs/apk/debug/app-debug.apk`).

### Step N-1: Install + launch on emulator
- How:
  - Use `mcp__mobile-mcp` to list/select a device/emulator (`mobile_list_available_devices`).
  - Install the APK using `mobile_install_app` with the built APK path.
  - Launch the app using `mobile_launch_app` (package name to confirm from the installed app list if needed via `mobile_list_apps`).

