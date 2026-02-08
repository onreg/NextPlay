# Clarifying questions

## Inputs
- Task Brief: docs/tmp/game-details-ui.md
- Jira: none
- Figma: https://www.figma.com/design/nov1xXgQhkBdxSAiZA3x2E/Rawg.io?node-id=2-3111&t=4Ohe64towZqrxpf5-4 (not accessible right now, API returned 429 "Too Many Requests")

## Context summary
- Update the Game Details screen UI: header, banner, details card layout, and multiple sections.
- Must use `AppHeader` (`core/ui/.../AppHeader.kt`) because the current header is "too big".
- Increase banner image height.
- Details area becomes a card with a left column (release date, platforms, website link) plus bookmark (top-right) and rating (bottom-left).
- Add labeled sections: "Description" (with conditional "Read more"), "Developers & Publishers" (list with logo, name, role), "Screenshots" (keep), "Movies" (thumbnail, same size as screenshots), "Series games" (horizontal list of similar games).
- Differences vs design: do not show genres, split media into "Screenshots" and "Movies", and series cards should match the GameCard 2f aspect ratio.

## Questions (max 12, prioritized)

### Q1. What should the header title be when switching to `AppHeader`?
- Type: BLOCKER
- Why it matters: `AppHeader` currently requires a `titleResId`, but the screen today shows a dynamic title (game name).
- Rework `AppHeader` component, it should get title as `String`. Use `ResourceProvider` to get string from resources if needed (usually in UI mappers).

### Q2. What exact banner height should we target?
- Type: NON-BLOCKER
- Why it matters: "Increase the height" is subjective and affects scroll and layout balance.
- Use an aspect ratio instead of a fixed height. Should be the same as in the game list (2f) to maintain consistency across the app.

### Q3. Should the game title appear inside the details card after the redesign?
- Type: NON-BLOCKER
- Why it matters: The acceptance criteria for the card lists release date, platforms, website, bookmark, and rating, but does not mention the title.
- Title only in header

### Q4. Platforms on the details card: icons only or include text labels?
- Type: NON-BLOCKER
- Why it matters: Impacts density and alignment in the left column layout.
- Icons only

### Q5. Rating presentation in the details card: what should it look like?
- Type: NON-BLOCKER
- Why it matters: The requirement says "Rating left bottom corner of the card" but not the visual style.
- Chip (similar to `GameCard` rating chip), anchored top right of the banner, similar to game list item design.

### Q6. Website link behavior: what should happen on tap, and when should it be shown?
- Type: NON-BLOCKER
- Why it matters: Impacts empty states and consistency with other link handling.
- Always show a "Website" button if URL exists, open external browser. Hide the button if no URL is provided.

### Q7. "Description" section: collapse threshold and toggle behavior
- Type: BLOCKER
- Why it matters: Requirement says show a "Read more" button only if the description is too long.
- Collapsed: 6 lines, show "Read more" only if overflow, expanded shows full text and button becomes "Read less".

### Q8. "Developers & Publishers" section: list structure and ordering
- Type: BLOCKER
- Why it matters: Requirement specifies item fields (logo, name, role) but not grouping or ordering.
- Single combined list: developers first, then publishers, each item shows role label ("Developer" or "Publisher").

### Q9. Missing company logos: what placeholder should we show?
- Type: NON-BLOCKER
- Why it matters: Real data may not always include logos.
- Generic placeholder icon. The same as used in the game list for missing game banner.

### Q10. Movies list item content: thumbnail only, or include title/overlay?
- Type: NON-BLOCKER
- Why it matters: Acceptance criteria mentions thumbnail and size, but current UI also overlays a title.
- Thumbnail with a play icon overlay (no title).

### Q11. "Series games" section: header label and card sizing
- Type: BLOCKER
- Why it matters: Requirement explicitly calls out "Series games" label and 2f aspect ratio like `GameCard`.
- Rename to "Series games" but use a compact card while keeping the image at `aspectRatio(2f)`.

### Q12. Empty states: hide sections or show empty placeholders?
- Type: NON-BLOCKER
- Why it matters: Some games may not have developers, publishers, movies, or series.
- Hide the entire section when its list is empty.

## Detected ambiguities, conflicts, or gaps
- Requirement says "use AppHeader", but `AppHeader` API expects `titleResId`, while the current header uses the dynamic game title.
- Banner "increase the height" lacks a target value.
- Details card layout requirements specify left-side fields plus rating/bookmark placement, but do not specify where the game title should live.
- "Read more" should be conditional, but "too long" threshold is unspecified.
- Developers and publishers list needs decisions on ordering, grouping, and missing logos.
- Movies item needs decisions on whether to show any overlay content (title, play icon).
- Series section label and sizing may conflict between design and acceptance criteria and needs an explicit decision.

## Proposed assumptions (to confirm)
- Header title becomes static "Game details" to keep `AppHeader` unchanged.
- Banner height becomes 240.dp.
- Description collapsed at 5 lines with "Read more" only when overflow; expanded toggles to "Read less".
- Developers and publishers appear as one combined list with a role label per row.
- Movies show a play icon overlay, no title overlay.
- Sections with empty content are hidden.
