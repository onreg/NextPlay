# Clarifying questions

## Inputs
- Task Brief: docs/task/game_details.md
- Jira: none
- Figma:
  - Dark theme: https://www.figma.com/design/nov1xXgQhkBdxSAiZA3x2E/Rawg.io?node-id=2-3111
  - Light theme: https://www.figma.com/design/nov1xXgQhkBdxSAiZA3x2E/Rawg.io?node-id=2-3737
- Other:
  - RAWG API docs: https://api.rawg.io/docs/
  - Example responses:
    - docs/api/game_details.md
    - docs/api/game_movies.md
    - docs/api/game_screenshots.md
    - docs/api/game_series.md

## Context summary
- Add a game details screen that opens when a user taps a game card in the game list.
- The details screen includes: header with back + title, banner image, a details card (release date, platforms, website link, bookmark, rating), description, screenshots, movies, developers, and series games.
- Bookmark, platforms, and rating UI should reuse the same components as the game list.
- Website link opens in the system browser.
- Description collapses to 5 lines with a "Read more"/"Read less" toggle.
- Screenshots and movies open full-screen in system viewers; use the best available quality when multiple options exist.
- Screenshots, movies, and series are paginated and must be implemented offline-first using RemoteMediator + Room in the same way as the game list.
- When opening game details, show cached data first and then update from network.
- Documentation is the source of truth; designs might differ.

## Questions (max 12, prioritized)

### Q1. Should the UI follow the Task Brief sectioning exactly, even when Figma groups content differently (for example a combined "Media" row)?
- Type: BLOCKER
- Why it matters: Determines the screen composition and which sections must be implemented.
- A) Follow Task Brief exactly (separate "Screenshots" and "Movies" sections with their own titles).

### Q2. The Task Brief says "Platforms", but Figma shows chips that look like genres/tags. What should be displayed in the details card?
- Type: BLOCKER
- Why it matters: Impacts which data is surfaced and how existing "platforms" components are reused.
- A) Platforms only (using the same platforms component as the game list).

### Q3. Which "rating" should be shown and in what format?
- Type: BLOCKER
- Why it matters: RAWG provides multiple rating-like fields (for example "rating" vs "metacritic"); Figma shows a number that may not match the API "rating" scale.
- Use "rating" field from the API, displayed as a number with one decimal place (for example "4.5"), use `Chip` component as in game list.

### Q4. Should the header include any actions besides Back (for example Share, as shown in Figma)?
- Type: NON-BLOCKER
- Why it matters: Adds extra behavior and intent handling beyond the Task Brief.
- Back only (Task Brief).

### Q5. How should the "Official Website" link behave when the API does not provide a valid URL?
- Type: BLOCKER
- Why it matters: Defines empty/invalid data behavior and avoids broken intents.
- Hide the website row if website is null/blank/invalid.

### Q6. What is the expected bookmark behavior across app restarts and between list and details?
- Type: BLOCKER
- Why it matters: The Task Brief says "stored in the ViewModel the same as in the game list", which may imply in-memory only; this affects persistence and data consistency.
- Match current game list behavior exactly. Bookmarks are stored in the ViewModel (tmp solution for mvp), later will be persisted to local storage.

### Q7. What should the screen show when there is no cached data and the network request fails?
- Type: BLOCKER
- Why it matters: Defines the error/empty state for the primary details content.
- A) Full-screen error state with Retry. Use `ContentError` component.

### Q8. For screenshots, movies, and series pagination, should paging config (page size, prefetch distance, initial load) match the game list exactly?
- Type: BLOCKER
- Why it matters: Affects UX and data usage and ensures consistency with the existing paging implementation.
- Yes, reuse the exact same paging configuration as game list.

### Q9. Offline behavior for paged sections: what should happen when offline and there is no cached content for a section (screenshots/movies/series)?
- Type: NON-BLOCKER
- Why it matters: Defines UX consistency for secondary content when offline.
- A) Hide the section entirely.

### Q10. When a user taps a screenshot or a movie tile, should we open a single item or a gallery/playlist experience?
- Type: BLOCKER
- Why it matters: Affects intent payloads and the user experience for full-screen viewing.
- A) Open only the tapped item (single image / single video) in the system viewer/player.

### Q11. For movie playback quality selection, is "best available quality" defined as using the "max" URL when present, otherwise the highest numeric resolution key?
- Type: NON-BLOCKER
- Why it matters: Clarifies deterministic selection when multiple sources exist.
- A) Yes (prefer "max", else highest numeric).

### Q12. When a user taps a game in the series carousel, should we push a new details screen onto the back stack, or replace the current details screen?
- Type: NON-BLOCKER
- Why it matters: Impacts navigation behavior and back-stack depth.
- A) Push new details (back returns to previous details).

### Q13. What should the screen show when there is no cached data?
- Type: BLOCKER
- Why it matters: Defines the loading state for the primary details content.
- Use skeleton loaders (shimmer) for the entire screen until data is available. You can check how it is implemented in the game list screen (`GameCardLoading`).

## Detected ambiguities, conflicts, or gaps
- The Task Brief first line says "job details feature", but the rest clearly describes "game details".
The correct term is "game details".

- Endpoint naming mismatch: the Task Brief says `/games/{id}/series`, but the example response file uses `/games/{id}/game-series`.
The correct endpoint is `/games/{id}/game-series`.

- Figma shows a combined "Media" section and a "Developers & Publishers" section, while the Task Brief lists "Screenshots", "Movies", and "Developers" separately and does not mention "Publishers".
The correct is to follow the Task Brief sectioning exactly. Check Q1.

- Figma appears to show extra content ("Similar Games") and a share action not mentioned in the Task Brief.
No need to implement "Similar Games" or Share action.

- Figma copy uses "Read More" capitalization, while the Task Brief specifies "Read more"/"Read less".
Use "Read more"/"Read less" as per Task Brief.

## Proposed assumptions (to confirm)
- Implement sections required by the Task Brief; any additional sections/actions present only in Figma are out of scope unless explicitly confirmed.
- Use the example response docs under `docs/api/` as the source of truth for endpoint paths and response shapes when there is a mismatch.
- Display rating, platforms, and bookmark using the exact same UI components and data sources as the game list.
