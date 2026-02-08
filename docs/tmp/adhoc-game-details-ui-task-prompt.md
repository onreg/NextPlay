## Task Prompt

### Title
- Game details screen UI refresh (header, banner, sections, media split)

### Source links
- Task Brief: `docs/tmp/game-details-ui.md`
- Jira: none
- Figma: https://www.figma.com/design/nov1xXgQhkBdxSAiZA3x2E/Rawg.io?node-id=2-3111&t=4Ohe64towZqrxpf5-4 (node 2:3111)
- Other: none

### Context
- The current Game Details UI needs to better match the intended layout and component sizing from the provided Figma.
- The header is currently too tall and must be replaced with the shared `AppHeader` component.
- The banner image should be taller and consistent with the 2f aspect ratio used in the game list items.
- Media must be split into separate "Screenshots" and "Movies" sections.
- Genres must not be displayed on this screen.

### Goals
- Align the Game Details screen structure and hierarchy with the Figma layout and the clarified decisions below.
- Keep existing functional behavior (bookmarking, navigation) unless explicitly specified otherwise.
- Provide complete state handling: initial loading, per-section loading for carousels, and error/retry behaviors.

### Non-goals
- Do not display game genres anywhere on the Game Details screen.
- Do not add a "Similar games" section; only "Series games" is required.
- Do not add sharing actions in the header.

### User experience

#### Default (loaded) flow
1) User opens a game details screen.
2) Header shows a back button and the game title.
3) A banner image is displayed at the top with a 2f (width:height) aspect ratio.
4) A rating chip is shown anchored at the top-right of the banner (same visual treatment as the rating chip in the game list item design).
5) Below the banner, show a "Game details" card:
   - Left side: release date, platform icons, and "Official Website" link.
   - Right/top corner: bookmark control (keep existing bookmark behavior).
6) "Description" section:
   - Label "Description".
   - Description text:
     - Collapsed state shows up to 6 lines.
     - If the text overflows, show a button "Read more".
     - Expanded state shows the full text and the button changes to "Read less".
7) "Developers & Publishers" section:
   - Label "Developers & Publishers".
   - List items show: logo, name, role label ("Developer" or "Publisher").
   - Ordering: developers first, then publishers.
   - If a logo is missing, show a placeholder avatar/icon consistent with the app's missing-image pattern.
8) "Screenshots" section:
   - Label "Screenshots".
   - Horizontal list of screenshot thumbnails.
9) "Movies" section:
   - Label "Movies".
   - Horizontal list of movie thumbnails, using the same item size as screenshots.
10) "Series games" section:
   - Label "Series games".
   - Horizontal list of games, using the same 2f aspect ratio as the main GameCard in the games list.

#### Loading states
- Initial loading (no cache):
  - Show a loading UI (shimmer) for all major components on the screen.
- If carousel sections load separately (Screenshots, Movies, Series games):
  - Show an individual loading state for that section while it is loading.

#### Error states
- If the main game details load fails:
  - Show a full-screen error state with a retry action.
- If an error occurs in a specific section (Screenshots, Movies, Series games, Developers & Publishers):
  - Do not show that section.
  - Do not block the rest of the screen from being usable.

#### Existing users and previously saved values
- Bookmark state and behavior must remain consistent with the current app behavior; only the placement changes.
- The expanded/collapsed state of "Description" should reset to collapsed when navigating away and back (unless there is an existing persisted behavior today).

### Functional requirements
1) Header uses `AppHeader` and is smaller than the current header implementation.
2) Header includes only:
   - Back button
   - Screen title (game name)
   - No share action
3) Banner image uses a 2f aspect ratio (screen width based).
4) Rating chip:
   - Uses the same visual style as the rating chip used in the game list item design.
   - Positioned at the top-right of the banner.
5) Game details card:
   - Rendered as a card container.
   - Left side includes:
     - Release date formatted as "MMM d, yyyy" (example: "Apr 10, 2020").
     - Platform icons (no platform names text required unless already present today).
     - A link labeled "Official Website" that opens the game's website when available.
   - Bookmark control placed in the top-right corner of the card.
6) Genres must not be displayed.
7) "Description" section:
   - Title label exactly "Description".
   - Collapsed description shows 6 lines maximum.
   - Show "Read more" only when the text overflows.
   - Expanded state shows full text and the button label becomes "Read less".
8) "Developers & Publishers" section:
   - Title label exactly "Developers & Publishers".
   - Each list item includes:
     - Logo (or placeholder when missing)
     - Name
     - Role label: "Developer" or "Publisher"
   - Ordering: developers first, then publishers.
9) "Screenshots" section:
   - Title label exactly "Screenshots".
   - Horizontal list of thumbnails.
10) "Movies" section:
   - Title label exactly "Movies".
   - Horizontal list of thumbnails.
   - Item size matches "Screenshots" items.
11) "Series games" section:
   - Title label exactly "Series games".
   - Horizontal list of games.
   - Item aspect ratio is 2f, matching the list GameCard.
12) Section-level failure handling:
   - If a section fails to load, the section is hidden.
   - The rest of the content remains visible and usable.
13) Full-screen failure handling:
   - If main game details fail to load, show an error state with retry.
14) Loading UI:
   - Shimmer for the whole screen when initially loading with no cache.
   - Per-section loading UI for carousels when they load independently.

### Data and mapping
- Release date:
  - Type: date
  - Nullable: yes
  - Display:
    - If present: formatted as "MMM d, yyyy" (example: "Apr 10, 2020")
    - If missing: omit the release date row
- Platforms:
  - Type: list
  - Nullable: yes
  - Display:
    - Render as platform icons
    - If empty/missing: omit the platform row
- Official website:
  - Type: URL string
  - Nullable: yes
  - Display:
    - If present: show link labeled "Official Website"
    - If missing: omit the link
- Bookmark:
  - Type: boolean (existing persisted behavior)
  - Behavior: unchanged; only repositioned in the UI
- Rating:
  - Type: existing rating value used by the app for the game list rating chip
  - Display: rating chip anchored top-right of the banner

### API/contracts (if applicable)
- No API contract changes required by this task unless needed to support sections that currently cannot load.

### Analytics/observability (if applicable)
- No new analytics events required.

### Rollout
- No feature flag required unless the app currently uses one for Game Details UI.

### Acceptance criteria
- AC1: The Game Details screen uses `AppHeader` and the header height matches the shared component sizing.
- AC2: The header shows only a back button and the game title, with no share action.
- AC3: The banner image is taller and rendered with a 2f aspect ratio (screen width based).
- AC4: A rating chip is shown at the top-right of the banner and matches the styling used in the game list item rating chip.
- AC5: The "Game details" content is displayed in a card with:
  - Release date formatted as "MMM d, yyyy" when available
  - Platform icons
  - An "Official Website" link when available
  - Bookmark control at the card top-right with unchanged behavior
- AC6: Genres are not shown anywhere on the Game Details screen.
- AC7: The "Description" section:
  - Shows the label "Description"
  - Collapses to 6 lines by default
  - Shows "Read more" only when overflow occurs
  - Toggles to expanded state with "Read less"
- AC8: The "Developers & Publishers" section shows a combined list where:
  - Developers appear first, then publishers
  - Each item shows logo (or placeholder), name, and role label ("Developer" or "Publisher")
- AC9: The "Screenshots" section remains present and displays a horizontal list of screenshot thumbnails.
- AC10: The "Movies" section is present and displays a horizontal list of movie thumbnails sized the same as screenshots.
- AC11: The "Series games" section is present and displays a horizontal list of games with 2f aspect ratio cards.
- AC12: When the main game details load fails, the screen shows an error state with a retry action.
- AC13: When a specific section fails to load, that section is hidden and the rest of the screen remains usable.
- AC14: When initially loading with no cache, the screen shows a full loading shimmer; carousel sections can show individual loading UI if they load separately.

### Test plan (high-level)
- Unit tests:
  - Description expand/collapse: 6-line truncation, overflow detection, "Read more" and "Read less" toggle.
  - Developers & Publishers ordering and role labeling (developers first, then publishers).
  - Section visibility rules: hide a section when its data load fails.
- UI tests:
  - Loaded state renders all required sections with correct labels and placements (header, banner, rating chip, card, sections).
  - Initial loading shows shimmer when no cache is present.
  - Full-screen error renders on main failure and retry triggers reload.
  - Section failure hides only the failed section.
  - Description toggles between collapsed and expanded states with correct button labels.

### Open questions
- (empty)
