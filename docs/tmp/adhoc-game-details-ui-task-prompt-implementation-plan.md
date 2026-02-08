# Game Details Screen UI Refresh Implementation Plan

## Overview
Refresh the Game Details screen UI to match the referenced Figma layout and the task prompt’s clarified decisions: smaller shared header (`AppHeader`), 2f banner aspect ratio with rating chip, “Game details” card layout, improved section structure (Description, Developers & Publishers, Screenshots, Movies, Series games), and complete loading/error handling with section-level failure isolation.

- Relevant ADRs:
  - ADR-001 (Application Architecture): keep feature/UI orchestration in `:feature:*`, IO/mapping in `data/*/impl`, and use existing modular boundaries.
  - ADR-003 (Networking): keep RAWG DTOs in `:core:network` and map DTO → persistence/app models in `data/*/impl`.
  - ADR-004 (Persistence): keep Room entities/DAOs in `:core:db`; expect destructive migrations on version bumps; update exported schema JSON.
  - ADR-005 (State/UI): keep UI state owned by ViewModel (`StateFlow`) and avoid navigation side effects in ViewModel; collect events lifecycle-aware.
  - ADR-008 (Testing): default to JVM unit tests; Compose UI tests run on JVM (Robolectric) using semantics/tags.
- ADR Conflicts: none identified.
- Assumptions:
  - RAWG “game details” response includes `publishers` and (for developers/publishers) an image field suitable for “logo” rendering (assumed to be `image_background`), and it is acceptable to use that as the row image.
  - The “rating chip” on the banner uses the same `Chip` styling as the game list `GameCard` rating chip, with the existing rating value already available in `GameDetailsUi`.
  - Room destructive migration is acceptable for this schema change (consistent with current `fallbackToDestructiveMigration()`).
  - Navigation is `GamesPane -> GameDetailsPane` (implemented in `app/src/main/kotlin/io/github/onreg/nextplay/MainActivity.kt`), so re-entering the screen recreates UI state unless specifically retained by the navigation stack.
- Open Questions: none (task prompt lists “(empty)”).

## Files to Modify
- `core/ui/src/main/kotlin/io/github/onreg/core/ui/components/header/AppHeader.kt` - add support for dynamic title strings (not only `titleResId`) so Game Details can display the game name.
- `core/ui/src/main/kotlin/io/github/onreg/core/ui/components/header/AppHeaderUI.kt` - extend UI model to support dynamic title.
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPane.kt` - replace current top app bar with `AppHeader`, refactor to an internal testable screen, and update screen structure per spec.
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneSections.kt` - implement the new banner + rating chip, “Game details” card layout, Description section behavior, Developers & Publishers list UI, and section headers/labels.
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneHelpers.kt` - update “Read more/less” copy casing, line limit constants, and section visibility helpers to match requirements (hide on error; show per-section loading UI when appropriate).
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/ui/model/GameDetailsUi.kt` - adjust UI model to support nullable release date, official website label visibility, rating chip model, and developers/publishers row data.
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/ui/mapper/GameDetailsUiMapper.kt` - map new API model fields into UI structures (release date optional, rating chip, developers/publishers list with roles).
- `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/GameDetailsViewModel.kt` - ensure state handling supports initial shimmer, section-level loading, and “main failure shows full-screen error”; keep bookmark behavior unchanged.
- `data/details/api/src/main/kotlin/io/github/onreg/data/details/api/model/GameDetails.kt` - extend contracts to include publishers and structured “company entries” needed by UI (name, optional logo URL, role).
- `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDetailsDto.kt` - extend DTO to parse publishers and image fields for developers/publishers (per ADR-003).
- `core/db/src/main/kotlin/io/github/onreg/core/db/details/entity/GameDetailsEntity.kt` - persist developers/publishers data for offline cache (per ADR-004).
- `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt` - bump Room version and ensure schema export updates.
- `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/mapper/impl/GameDetailsDtoMapperImpl.kt` - map extended DTO fields into the updated entity.
- `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/mapper/impl/GameDetailsEntityMapperImpl.kt` - map updated entity into the updated `data/details/api` model.
- `feature/game-details/src/test/kotlin/io/github/onreg/feature/game/details/impl/GameDetailsViewModelTest.kt` - expand unit tests for state behavior and section visibility rules.
- `data/details/impl/src/test/kotlin/io/github/onreg/data/details/impl/GameDetailsRepositoryImplTest.kt` - update tests to reflect new DTO/entity/model fields.
- `core/db/schemas/io.github.onreg.core.db.NextPlayDatabase/4.json` (expected) - Room schema export for the new DB version.

## New Files to Create
- `data/details/api/src/main/kotlin/io/github/onreg/data/details/api/model/GameCompany.kt` - public API model for developers/publishers entries and role typing.
- `feature/game-details/src/test/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsScreenTest.kt` - Compose UI JVM tests for the new screen structure, labels, and interactions (Robolectric).

## Implementation Steps

### Step 1: Extend shared `AppHeader` to support dynamic titles
- Where:
  - `core/ui/src/main/kotlin/io/github/onreg/core/ui/components/header/AppHeader.kt`
  - `core/ui/src/main/kotlin/io/github/onreg/core/ui/components/header/AppHeaderUI.kt`
- What:
  - Update `AppHeaderUI` to support either a string resource title or a dynamic string title (for Game Details).
  - Update `AppHeader` to render the correct title source without requiring a resource.
- Why:
  - AC1/AC2 require using the shared `AppHeader`, but the Game Details title is dynamic (game name).
- How:
  - Planned public structures (syntactically valid Kotlin):
    ```kotlin
    public sealed interface AppHeaderTitle {
        public data class Res(val titleResId: Int) : AppHeaderTitle
        public data class Text(val value: String) : AppHeaderTitle
    }

    public data class AppHeaderUI(
        val title: AppHeaderTitle,
        val navigationItem: AppHeaderMenu? = null,
        val menuItems: List<AppHeaderMenu>? = null,
    )
    ```
- Outcome:
  - `AppHeader` can be reused on Game Details with a dynamic title string while preserving existing resource-based usage.

### Step 2: Refactor Game Details pane into a testable internal screen surface
- Where: `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPane.kt`
- What:
  - Keep `GameDetailsPane(...)` as the public entry point (Hilt ViewModel, paging collection, navigation callbacks).
  - Extract the UI into an `internal` screen composable (similar to `feature/game-list` patterns) that accepts state + paging items as parameters.
  - Switch event collection to the project’s lifecycle-aware flow collector helper (to align with ADR-005 patterns used in `GamesPane`).
- Why:
  - Enables Compose UI tests (ADR-008) without relying on Hilt test setup.
  - Keeps responsibilities clean: pane wires ViewModel, screen renders UI.
- How:
  - Planned signatures (commented for Kotlin validity per rules):
    ```kotlin
    // @Composable
    // internal fun GameDetailsScreen(
    //     state: GameDetailsState,
    //     details: GameDetailsUi,
    //     screenshots: LazyPagingItems<Screenshot>,
    //     movies: LazyPagingItems<Movie>,
    //     series: LazyPagingItems<Game>,
    //     onBackClicked: () -> Unit,
    //     onWebsiteClicked: () -> Unit,
    //     onBookmarkClicked: () -> Unit,
    //     onDescriptionToggle: () -> Unit,
    // ): Unit
    ```
- Outcome:
  - Game Details UI can be rendered in tests with deterministic inputs and semantics tags.

### Step 3: Replace current top app bar with `AppHeader` (back + dynamic game title only)
- Where: `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPane.kt`
- What:
  - Replace `CenterAlignedTopAppBar` usage with `AppHeader` configured with:
    - Back button only
    - Title = game name
    - No share/menu actions
- Why:
  - AC1/AC2.
- How:
  - Use `AppHeaderUI(navigationItem = ..., title = AppHeaderTitle.Text(details.title))`.
  - Ensure the header height matches the shared component sizing by not applying extra padding/height modifiers.
- Outcome:
  - Header matches the shared sizing and only contains the required elements.

### Step 4: Implement banner with 2f aspect ratio and rating chip overlay
- Where: `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneSections.kt`
- What:
  - Replace the fixed-height banner with a screen-width `aspectRatio(2f)` banner image.
  - Add a rating chip aligned top-right, using the same `Chip` styling as `GameCardImage` in `presentation/game-list`.
- Why:
  - AC3/AC4; ensures consistent visual treatment with the game list.
- How:
  - Update `GameDetailsUi` to expose a `ChipUI` rating model:
    ```kotlin
    internal data class GameDetailsUi(
        val gameId: Int,
        val title: String,
        val imageUrl: String,
        val ratingChip: ChipUI,
        val releaseDate: String?,
        val platforms: Set<PlatformUI>,
        val officialWebsiteUrl: String?,
        val description: String,
        val companies: List<GameCompanyUi>,
    )
    ```
- Outcome:
  - Banner matches aspect ratio requirement and displays rating chip as designed.

### Step 5: Rebuild the “Game details” card layout (no genres)
- Where: `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneSections.kt`
- What:
  - Replace the current card metadata (which repeats the title and shows “Released:” and “Rating:” text rows) with the required layout:
    - Left: optional release date row, platform icons row, optional “Official Website” link (label fixed as “Official Website”)
    - Right/top: bookmark icon button (keep behavior unchanged)
  - Ensure genres are not displayed.
- Why:
  - AC5/AC6.
- How:
  - Make `releaseDate` nullable in `GameDetailsUi` so the UI can omit the row when missing.
  - Render website link label exactly “Official Website” and open `officialWebsiteUrl` on click.
- Outcome:
  - Card structure matches spec and hides missing rows correctly.

### Step 6: Implement “Description” section with overflow-aware “Read more/less”
- Where:
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneSections.kt`
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneHelpers.kt`
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/model/GameDetailsState.kt` (if state is kept in ViewModel)
- What:
  - Add a section header label exactly “Description”.
  - Default collapsed state to 6 lines.
  - Show “Read more” only when the collapsed text overflows.
  - Toggle expanded state and switch button label to “Read less”.
  - Ensure description expansion resets to collapsed when leaving and returning to the screen.
- Why:
  - AC7 and UX requirement about reset-on-return.
- How:
  - Update constants and copy casing:
    ```kotlin
    internal const val COLLAPSED_DESCRIPTION_MAX_LINES: Int = 6

    internal fun readMoreText(isExpanded: Boolean): String =
        if (isExpanded) "Read less" else "Read more"
    ```
  - Implement overflow detection in Compose using `onTextLayout` and `TextLayoutResult.hasVisualOverflow`.
  - Reset behavior:
    - Option A (preferred): keep expansion state as composable local state (resets naturally when the composable is recreated).
    - Option B: if kept in `GameDetailsState`, ensure `initialize(gameId)` resets `isDescriptionExpanded` even when re-entering the same game id route instance.
- Outcome:
  - Description matches copy/line limit and only shows the toggle when needed.

### Step 7: Add Developers & Publishers support to data contracts and persistence (publishers + logos + roles)
- Where:
  - `core/network/src/main/kotlin/io/github/onreg/core/network/rawg/dto/GameDetailsDto.kt`
  - `data/details/api/src/main/kotlin/io/github/onreg/data/details/api/model/GameDetails.kt`
  - `data/details/api/src/main/kotlin/io/github/onreg/data/details/api/model/GameCompany.kt` (new)
  - `core/db/src/main/kotlin/io/github/onreg/core/db/details/entity/GameDetailsEntity.kt`
  - `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/mapper/impl/GameDetailsDtoMapperImpl.kt`
  - `data/details/impl/src/main/kotlin/io/github/onreg/data/details/impl/mapper/impl/GameDetailsEntityMapperImpl.kt`
  - `core/db/src/main/kotlin/io/github/onreg/core/db/NextPlayDatabase.kt` + schema export
- What:
  - Parse and cache both developers and publishers from RAWG game details response.
  - Persist them for offline usage.
  - Expose them as a structured list to the feature layer with explicit role typing.
- Why:
  - AC8 requires a combined “Developers & Publishers” list with logo, name, and role label; current contracts only provide developer names.
- How:
  - New public contract model:
    ```kotlin
    package io.github.onreg.data.details.api.model

    public enum class GameCompanyRole { Developer, Publisher }

    public data class GameCompany(
        val name: String,
        val logoUrl: String?,
        val role: GameCompanyRole,
    )
    ```
  - Update `GameDetails`:
    ```kotlin
    public data class GameDetails(
        val gameId: Int,
        val title: String,
        val imageUrl: String,
        val releaseDate: Instant?,
        val platforms: Set<GamePlatform>,
        val website: String?,
        val rating: Double,
        val description: String,
        val companies: List<GameCompany>,
    )
    ```
  - DTO changes (assumptions about RAWG fields must be validated against actual API shape during implementation):
    ```kotlin
    @JsonClass(generateAdapter = true)
    public data class CompanyDto(
        @Json(name = "name") val name: String?,
        @Json(name = "image_background") val logoUrl: String?,
    )
    ```
  - Persistence:
    - Add two new columns to `GameDetailsEntity` for developers/publishers “name + logo” pairs (serialized as a delimiter-based string or another lightweight encoding).
    - Bump `NextPlayDatabase` version (e.g., 3 → 4) and ensure Room schema export updates under `core/db/schemas/...`.
- Outcome:
  - Feature layer can render a correct ordered list of developer and publisher rows with role labels and logos/placeholders.

### Step 8: Render the “Developers & Publishers” section UI (ordered, role-labeled, placeholder for missing logos)
- Where:
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneSections.kt`
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/ui/model/GameDetailsUi.kt`
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/ui/mapper/GameDetailsUiMapper.kt`
- What:
  - Add a section header exactly “Developers & Publishers”.
  - Display a vertical list of rows:
    - logo (use `DynamicAsyncImage` so missing images show the established placeholder icon)
    - name
    - role label (“Developer” / “Publisher”)
  - Ensure ordering developers first, then publishers.
- Why:
  - AC8.
- How:
  - Feature UI model for rows:
    ```kotlin
    internal enum class GameCompanyRoleUi(val label: String) {
        Developer("Developer"),
        Publisher("Publisher"),
    }

    internal data class GameCompanyUi(
        val name: String,
        val logoUrl: String?,
        val role: GameCompanyRoleUi,
    )
    ```
- Outcome:
  - Section matches spec, handles missing logos gracefully, and preserves ordering rules.

### Step 9: Update Screenshots and Movies sections for per-section loading and “hide on section failure”
- Where:
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPane.kt`
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneSections.kt`
  - `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneHelpers.kt`
- What:
  - Ensure each carousel section:
    - Shows a section loading UI while its paging refresh is loading (when itemCount == 0).
    - Hides the entire section when its paging refresh is `LoadState.Error` (regardless of exception type).
    - Does not block other screen content if it fails.
- Why:
  - AC9/AC10 and AC13/AC14.
- How:
  - Replace `shouldShowSection(...)` with a helper that can distinguish:
    - `ShowContent` (items available)
    - `ShowLoading` (loading, no items yet)
    - `Hide` (error, no items)
  - Add lightweight shimmer placeholders for thumbnails (use `Modifier.shimmer(width)` from `core/ui`).
- Outcome:
  - Media sections match loading/error requirements and remain consistent in thumbnail sizing.

### Step 10: Update “Series games” section label and card sizing/aspect ratio
- Where: `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPaneSections.kt`
- What:
  - Rename section header to exactly “Series games”.
  - Use horizontally scrolling list.
  - Ensure series game cards use the same 2f image aspect ratio styling as the main game list `GameCard` (reuse `GameCard` where feasible).
- Why:
  - AC11 and prompt clarification (no “Similar games”).
- How:
  - Prefer reusing `GameCard` from `presentation/game-list` with a fixed width modifier and rely on its internal `aspectRatio(2f)` for the image.
- Outcome:
  - Series list matches required label and consistent card visuals.

### Step 11: Implement initial full-screen loading shimmer and full-screen error with retry
- Where: `feature/game-details/src/main/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsPane.kt`
- What:
  - Replace the empty loading box with a shimmer layout that covers major components when `details == null` and initial load is in progress.
  - Replace the minimal error UI with a full-screen error state with a retry action when `details == null` and initial load fails.
- Why:
  - AC12/AC14.
- How:
  - Reuse existing shimmer primitives (`core/ui/.../Shimmer.kt`) and match loading placeholder structure to the intended screen hierarchy (header, banner, card, sections).
  - For error UI, prefer an existing reusable content component if available; otherwise create a small screen-local error component with a clearly labeled retry.
- Outcome:
  - Screen has complete “no cache” loading and full-screen failure handling per spec.

### Step 12: Update/extend automated tests per ADR-008
- Where:
  - `feature/game-details/src/test/kotlin/io/github/onreg/feature/game/details/impl/GameDetailsViewModelTest.kt`
  - `feature/game-details/src/test/kotlin/io/github/onreg/feature/game/details/impl/pane/GameDetailsScreenTest.kt` (new)
  - `data/details/impl/src/test/kotlin/io/github/onreg/data/details/impl/GameDetailsRepositoryImplTest.kt`
- What:
  - Unit tests:
    - Developers & Publishers ordering and role labeling (mapper-level tests for deterministic ordering).
    - Section visibility rules (hide section on error, show loading placeholders when loading).
    - Description toggle copy and line limit constants (pure function tests).
  - Compose UI tests (JVM/Robolectric):
    - Loaded state shows required labels: “Description”, “Developers & Publishers”, “Screenshots”, “Movies”, “Series games”.
    - Header uses `AppHeader` with back button and game title only (no share).
    - Description toggle shows “Read more” only for overflowing long text and toggles to “Read less”.
    - Developers & Publishers list renders rows with role labels and placeholders when logo is missing.
    - Full-screen error renders on main failure and retry triggers refresh callback (screen-level test with injected callbacks).
- Why:
  - Task prompt’s test plan and ADR-008 expectations for UI/behavior changes.
- How:
  - Use existing testing utilities in `testing/unit` (coroutines test, flow helpers).
  - Add/extend semantics tags in `feature/game-details` where necessary to make UI assertions stable.
- Outcome:
  - The UI refresh is guarded by targeted unit tests and Compose UI tests.

### Step 13: Prepare design references (Figma)
- Why:
  - Ensure implementation and verification compare against the intended layout and states from the referenced design node.
- How:
  - Use `mcp__figma` to fetch node `2:3111` from `nov1xXgQhkBdxSAiZA3x2E/Rawg.io` and capture reference notes for:
    - Header sizing and alignment
    - Banner image aspect ratio and chip placement
    - Card spacing/padding and section hierarchy
    - Carousel item sizing for screenshots/movies
    - “Series games” card sizing/aspect ratio expectations
- Outcome:
  - A concrete checklist of measurements/states is ready for the final design verification step.

## Design Verification (Figma)
- Frame/node:
  - `Rawg.io` file, node `2:3111` (Game details screen).
- Verify:
  - Header: uses shared `AppHeader` sizing; only back + centered title.
  - Banner: 2f aspect ratio; rating chip visually matches the game list chip and sits top-right.
  - “Game details” card: content layout (release date, platforms, official website link, bookmark placement).
  - Sections order and titles exactly:
    - “Description”
    - “Developers & Publishers”
    - “Screenshots”
    - “Movies”
    - “Series games”
  - Thumbnail sizes: Screenshots and Movies item sizes match.
  - Loading/error states behavior matches prompt (even if not explicitly drawn in Figma).

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
- Fix: Apply fixes based on test reports, then re-run until green.

### Step N-2: Build an installable artifact
- Command to run:
  ```bash
  ./gradlew :app:assembleDebug
  ```
- Outcome: Debug APK is available at `app/build/outputs/apk/debug/app-debug.apk`.

### Step N-1: Install + launch on emulator
- How:
  - Use `mcp__mobile-mcp__mobile_list_available_devices` to select an Android emulator.
  - Install the APK using `mcp__mobile-mcp__mobile_install_app` with:
    - `path = /Users/vkorzun/Projects/NextPlay/app/build/outputs/apk/debug/app-debug.apk`
  - Launch the app using `mcp__mobile-mcp__mobile_launch_app` with:
    - `packageName = io.github.onreg.nextplay`

### Step N: Design verification (Figma)
- Compare the implemented UI to node `2:3111`:
  - Use `mcp__mobile-mcp__mobile_take_screenshot` to capture the rendered screen on the emulator for side-by-side comparison.
  - Validate layout, spacing, typography, and the required states (loaded, loading shimmer, full-screen error, section-level loading/hide-on-error).
- If this step fails, fix the issue and repeat this same step until it passes before proceeding.

