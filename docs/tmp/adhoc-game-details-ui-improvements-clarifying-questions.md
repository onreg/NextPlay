# Clarifying questions

## Inputs
- Task Brief: docs/tmp/game-details-ui-improvements.md
- Jira: none
- Figma: none

## Context summary
- The "Game details" screen needs UI polish and tighter visual consistency with the "list of games" UI.
- Layout changes include a smaller app bar, an edge-to-edge taller banner image, and removing card containers around meta info.
- Interaction changes include improving the "Official Website" tap target and using a new design system "TextButton" (no background, text + ripple).
- Several "Game series" item tweaks are requested (size, chip styling, date format, removing bookmark).
- Screen paddings and spacing rules are specified (16.dp outer padding, 8.dp between elements, 16.dp between sections).
- Media thumbnails (screenshots and videos) should be larger and video thumbnails need a play icon overlay.
- There are back-navigation bugs when opening details from a series item (missing screenshots/movies, series count changes).

## Questions (max 12, prioritized)

### Q1. What is the exact target for "standard size" app bar on the details screen?
- Type: BLOCKER
- Why it matters: "Standard size" can mean Material defaults or an app-specific component, and impacts layout, scrolling, and spacing.
- Use the platform default (Material 3 small top app bar behavior).

### Q2. Banner image: what is the exact target height (or aspect ratio) after removing left/top/right padding?
- Type: BLOCKER
- Why it matters: "Increase height" needs a concrete target to avoid arbitrary changes and to verify on different device sizes.
- Keep current aspect ration of 2f, the same as in the list of games, the banner should take the full width of the screen and height should be calculated based on the aspect ratio.

### Q3. Meta info layout: how should "Release date", "Platforms", and the website link be arranged without a card container?
- Type: BLOCKER
- Why it matters: Layout choice affects readability, wrapping for long platform lists, and accessibility.
- Vertical list with one row per field.

### Q4. Platforms styling: what exactly should "same size as in the list of games" mean?
- Type: NON-BLOCKER
- Why it matters: It could refer to typography, icon size, chip height, or all of them.
- Check how platforms are styled in the: [GameCard.kt](../../presentation/game-list/src/main/kotlin/io/github/onreg/ui/game/list/presentation/components/card/GameCard.kt)

### Q5. "Official Website" interaction: what should the tappable area and visual style be?
- Type: BLOCKER
- Why it matters: "Clickable area is small" implies accessibility/touch target requirements.
- Use M3 button component with no background.

### Q6. New design system component "TextButton": what are the required variants and states?
- Type: BLOCKER
- Why it matters: The component contract needs to be stable if it will be reused ("Official Website", "Read more/less", and potentially elsewhere).
- Single default style (enabled only)

### Q7. "Read more" / "Read less": which content should be expandable and what is the default state?
- Type: NON-BLOCKER
- Why it matters: Defines when the button appears and how much text is initially shown.
- Expand/collapse the game description; default collapsed.

### Q8. Game series item changes: which existing reference should be treated as the source of truth for styling?
- Type: BLOCKER
- Why it matters: Multiple items say "same as in the list of games" (rating chip color/size, date format, text sizes).
- [GameCard.kt](../../presentation/game-list/src/main/kotlin/io/github/onreg/ui/game/list/presentation/components/card/GameCard.kt)

### Q9. Game series: confirm exactly what "Increase the width of the series game item" means in the horizontal list.
- Type: NON-BLOCKER
- Why it matters: Width changes affect how many items peek on screen and the perceived density.
- Slightly increase width relative to current by a percentage: 50%.

### Q10. Date format: what is the exact desired format and locale behavior?
- Type: BLOCKER
- Why it matters: "Same as list of games" is ambiguous and needs a concrete pattern for consistency and tests.
- Check [GameUiMapper.kt](../../presentation/game-list/src/main/kotlin/io/github/onreg/ui/game/list/presentation/mapper/GameUiMapper.kt)

### Q11. Media thumbnails: what are the target sizes and should screenshot and video thumbnails share the same size?
- Type: NON-BLOCKER
- Why it matters: Impacts scrolling, image loading, and layout consistency.
- The same as for Game series, after Q9 changes.

### Q12. Back-navigation bug: confirm expected behavior when opening details from a series item and pressing back.
- Type: BLOCKER
- Why it matters: Determines whether the previous screen should restore UI state or fully reload and how to test it.
- Back returns to the same details instance and preserves all sections (screenshots, movies, series list and count).

## Detected ambiguities, conflicts, or gaps
- The brief references "list of games" as a styling source of truth but does not specify which exact screen/component to match.
- "Job details" is mentioned in the bug report; assume it means "game details", but this should be confirmed.
- Several items require concrete values to verify (banner height, series item width, media thumbnail sizes).
- "Text color should be the same as in the list of games, not white" does not specify which texts currently use white (e.g., over banner image vs body content).

## Proposed assumptions (to confirm)
- The "list of games" screen is the primary visual reference for colors, typography, chip styling, and overlays.
- No new data fields are introduced; changes are UI presentation and state restoration only.
- Accessibility expectations include a minimum 48.dp touch target for tappable text actions like "Official Website".

