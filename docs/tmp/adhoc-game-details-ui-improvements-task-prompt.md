## Task Prompt

### Title
- Game details UI improvements and state restoration fixes

### Source links
- Task Brief: docs/tmp/game-details-ui-improvements.md
- Jira: none
- Figma: none
- Other:
  - Visual reference for styling and formatting:
    - presentation/game-list/src/main/kotlin/io/github/onreg/ui/game/list/presentation/components/card/GameCard.kt
    - presentation/game-list/src/main/kotlin/io/github/onreg/ui/game/list/presentation/mapper/GameUiMapper.kt

### Context
- The "Game details" screen needs UI updates for consistency with the "list of games" UI.
- Several components should match the "list of games" styling: banner aspect ratio, platforms presentation, rating chip styling, date formatting, media overlays, and text colors.
- Tappable text actions ("Official Website", "Read more/less") need consistent behavior and larger hit targets.
- Back navigation from a series game details flow currently causes missing media sections and incorrect series count.
- Terminology note: the Task Brief mentions "job details"; treat it as "game details".

### Goals
- Make the game details screen visually consistent with the list of games.
- Improve usability of text actions by using a dedicated text-only button component.
- Apply consistent spacing rules across the details screen.
- Fix back-navigation state restoration issues when navigating from a series game details.

### Non-goals
- No new backend/API fields or contract changes.
- No redesign beyond the explicit UI changes listed in the Task Brief.
- No changes to the behavior or appearance of the main game list screen (it remains the reference).

### User experience
- User opens a game details screen.
- A standard Material 3 small top app bar is shown (not oversized).
- The banner image is edge-to-edge (no left/top/right padding) and uses the same 2f aspect ratio as the list game card image.
- Release date, platforms, and the "Official Website" action are shown directly in the screen content without card containers, arranged as a vertical list (one row per field).
- Platforms are displayed using the same presentation as in the list game card (icons, size, spacing).
- The game description is collapsible and starts collapsed when expandable; the action uses a text-only button ("Read more" / "Read less").
- Screenshots and video thumbnails are larger than current.
- Video thumbnails show a play icon overlay similar to the list-of-games overlay behavior.
- Text colors match the list-of-games styling (avoid white text styling in content areas where it differs from the list).
- When the user opens game details from a series item and presses back, the previous details screen instance is restored with screenshots and movies visible and the series count unchanged.

### Functional requirements
1. App bar:
   1. Use a standard Material 3 small top app bar height and behavior on the game details screen.
2. Banner image:
   1. Remove left, top, and right padding from the banner image (edge-to-edge horizontally).
   2. Keep the banner image aspect ratio at 2f, matching the list game card behavior (height derived from width/aspect ratio).
   3. Increase perceived banner prominence by relying on the edge-to-edge layout and aspect ratio rather than additional padding.
3. Meta info section (no card container):
   1. Show release date, platforms, and website action directly in the details content without a card container.
   2. Layout is a vertical list with one row per field.
4. Platforms:
   1. Match platform presentation from the list game card: show platform icons with the same sizing and spacing behavior as the reference.
5. Text-only actions:
   1. Add a design system "TextButton" component that is text-only with ripple and no background/container.
   2. "TextButton" is enabled-only (no disabled variant required).
   3. Use a Material 3 text button style internally (no container/background).
   4. Use "TextButton" for:
      1. "Official Website" action
      2. "Read more" / "Read less" actions
6. "Official Website" tap target:
   1. Ensure the tappable area is not small; the button must meet Material minimum touch target expectations.
7. Description expand/collapse:
   1. Expand/collapse the game description text.
   2. Default state is collapsed (when the content is long enough to be expandable).
8. Game series section:
   1. Increase series game item width by 50% relative to the current implementation.
   2. Decrease rating chip size.
   3. Rating chip color must match the list game card rating chip behavior.
   4. Do not show a bookmark control/icon on series game items.
   5. Date format in series items must match the list game card date formatting.
   6. Decrease the game title text size for series items.
9. Content paddings and spacing:
   1. Details screen outer horizontal padding: 16.dp.
   2. Details screen outer vertical padding: 16.dp.
   3. Vertical spacing between adjacent elements: 8.dp.
   4. Vertical spacing between sections: 16.dp.
10. Media thumbnails:
   1. Increase screenshot thumbnails size.
   2. Increase movie/video thumbnails size.
   3. Apply the same magnitude of increase as the series item width change unless a component constraint prevents it.
11. Video thumbnail overlay:
   1. Video thumbnails must display a play icon overlay, consistent with the list-of-games overlay behavior.
12. Back navigation bug fixes (series -> details -> back):
   1. After opening a game details screen from a series item and navigating back, screenshots and movies must be shown (not missing).
   2. After navigating back, the series count and series list content must not change unexpectedly.
   3. Back returns to the same details instance (state preserved).

### Data and mapping
- Release date formatting:
  - Must match list game card mapping behavior (as defined by the list mapper reference).
  - Expected format example: "MMM d, yyyy" (e.g., "Feb 8, 2026") and the same timezone assumptions used by the list.
- Platforms mapping:
  - Reuse the same platform-to-UI mapping and icon set used by the list game card.
- No new persisted fields or migrations are expected.

### API/contracts (if applicable)
- No API changes expected.

### Analytics/observability (if applicable)
- None required.

### Rollout
- No feature flag required.

### Acceptance criteria
- AC1: Game details uses a Material 3 small top app bar (not oversized).
- AC2: Banner image is edge-to-edge (no left/top/right padding) and uses a 2f aspect ratio.
- AC3: Release date, platforms, and "Official Website" are displayed directly in the content without a card container and arranged as a vertical list (one row per field).
- AC4: Platforms on details match the list game card presentation (icons, size, spacing).
- AC5: A reusable design system "TextButton" exists with no background and a ripple effect, and is used for "Official Website" and "Read more/less".
- AC6: "Official Website" has a sufficiently large tap target (meets Material minimum touch target expectations).
- AC7: Description is expandable/collapsible; default is collapsed when expandable; actions use "TextButton".
- AC8: Series game items are 50% wider than current, do not show bookmark control, have smaller title text, smaller rating chip, and chip color matches the list.
- AC9: Series game item date format matches the list game card date format.
- AC10: Details screen content padding and spacing match: 16.dp outer (vertical and horizontal), 8.dp between elements, 16.dp between sections.
- AC11: Screenshot and video thumbnails are larger than current; video thumbnails include a play icon overlay.
- AC12: After navigating to a series game details and pressing back, screenshots and movies are visible and the series count does not change; state is preserved.

### Test plan (high-level)
- Unit tests:
  - Date formatting logic used on details and series items matches the list mapper behavior (including empty/missing date).
  - Series item UI model mapping matches requirements (no bookmark, chip size config, title text size config, width multiplier).
  - State restoration logic for series navigation does not drop media sections and does not mutate series count unexpectedly.
- UI tests:
  - Game details layout: app bar size, banner aspect ratio, meta info without card, correct spacing.
  - Actions: "Official Website" and "Read more/less" use "TextButton" and have expected tap targets.
  - Media: larger thumbnails; play icon overlay visible on video thumbnails.
  - Navigation: open details from series item -> back restores previous details instance with screenshots/movies and stable series count.

### Open questions
- None

