# Clarifying questions

## Inputs
- Task Brief: `docs/tmp/game-details-ui.md`
- Jira: none
- Figma: https://www.figma.com/design/nov1xXgQhkBdxSAiZA3x2E/Rawg.io?node-id=2-3111&t=4Ohe64towZqrxpf5-4

## Context summary
- Update the Game Details screen UI to match the provided Figma and acceptance criteria.
- Replace the current header with `AppHeader.kt` because the existing header is "too big".
- Increase banner image height.
- Rework the "Game details" block into a card with specific content placement (release date, platforms, website link, bookmark, rating).
- Ensure sections: "Description" (with conditional "Read more"), "Developers & Publishers", "Screenshots", "Movies", and "Series games" (horizontal list).
- Noted differences: no genres, media split into "Screenshots" and "Movies", series games use the same 2f aspect ratio as the list GameCard.

## Questions (max 12, prioritized)

### Q1. What is the source screen/state to implement: the single Figma frame (node 2:3111) only, or also additional variants/states?
- Type: BLOCKER
- Why it matters: Missing states (loading/error/empty) can change layout, copy, and section visibility rules.
Should be:
  - loading UI (Shimmer) for all components if there is no cache and initial loading.
  - Individual loading for carousel components like "Screenshots", "Movies", "Series" if they load separately.
  - Error state with retry for the whole screen if the main data load fails. If error occurs in a specific section (e.g., media), do not show this section.

### Q2. Header requirements beyond "use AppHeader": title placement and actions?
- Type: BLOCKER
- Why it matters: `AppHeader` configuration affects layout (centered vs start title) and available actions.
- Only back button + title (no share).

### Q3. Banner image "increase the height": what target height/aspect should be used?
- Type: BLOCKER
- Why it matters: A hard height vs aspect ratio changes how images crop and how much content fits above the fold.
- Screen-width based aspect ratio (2f) matching the game list item design for consistency.

### Q4. "Game details" card: confirm exact left-side fields and formatting.
- Type: BLOCKER
- Why it matters: The brief mentions release date, platforms, and website link; the Figma also shows genre chips and platform icons.
- Show release date (formatted like "Apr 10, 2020"), platforms (icons), and "Official Website" link only.

### Q5. Bookmark placement and behavior: is it purely visual, or should it toggle saved state?
- Type: BLOCKER
- Why it matters: Toggling bookmark changes state, accessibility, and persistence expectations.
- Pure UI placement change only; keep existing bookmark behavior.

### Q6. Rating placement and value: what does "rating" mean here, and how should it be displayed?
- Type: BLOCKER
- Why it matters: The Figma shows a small "#1" badge; acceptance criteria says "Rating left bottom corner".
- Chip (similar to `GameCard` rating chip), anchored top right of the banner, similar to game list item design.

### Q7. "Description" section: define the truncation rule and exact button copy.
- Type: BLOCKER
- Why it matters: The "too long" rule affects when the button appears and how much text is shown initially.
- Collapsed: 6 lines, show "Read more" only if overflow, expanded shows full text and button becomes "Read less".

### Q8. "Read more" expanded state: should it toggle to "Read less" and collapse again?
- Type: NON-BLOCKER
- Why it matters: Defines interaction and expected UX parity with other screens.
- Toggle expand/collapse with "Read less".

### Q9. "Developers & Publishers" list: how should role labeling and ordering work?
- Type: BLOCKER
- Why it matters: The design suggests a single merged list; acceptance criteria mentions both roles.
- Single combined list: developers first, then publishers, each item shows role label ("Developer" or "Publisher").

### Q10. Company logos: what to do when a developer/publisher logo is missing?
- Type: NON-BLOCKER
- Why it matters: Missing images are common and should not break row layout.
- Show a placeholder avatar/icon, similar to the game list placeholder for missing banners.

### Q11. Media split: confirm whether the existing "Screenshots" section remains as-is and "Movies" is added similarly.
- Type: BLOCKER
- Why it matters: The Figma frame uses a single "Media" label, but the brief requires two sections.
- Two sections with headers "Screenshots" and "Movies" and identical item sizing.

### Q12. "Series games" vs "Similar games": which list is required, and what is the card aspect ratio?
- Type: BLOCKER
- Why it matters: The design shows both "Series Games" and "Similar Games"; the brief mentions "Series games" but also says it lists similar games.
- Implement "Series games" section only, with items in the same 2f aspect ratio as the main GameCard list.

## Detected ambiguities, conflicts, or gaps
- The Figma frame uses a single "Media" section, but the brief requires separate "Screenshots" and "Movies" sections.
- The Figma shows genre chips under the game details card area, but the brief explicitly says no need to show genres.
- The Figma shows a "#1" badge; the brief asks for "rating left bottom corner" but does not define the rating source or format.
- The brief says "Series games" is a list of similar games; the Figma shows both "Series Games" and "Similar Games".
- Button copy differs in casing: brief says "Read more", Figma shows "Read More".

## Proposed assumptions (to confirm)
- Implement only the loaded UI shown in Figma node 2:3111, keeping existing behavior for loading/error/empty.
- Do not display genres anywhere on the Game Details screen.
- Use two separate media sections: "Screenshots" and "Movies".
