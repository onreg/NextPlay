## Task Prompt

### Title
- Game details screen UI refresh (header, banner, sections)

### Source links
- Task Brief: docs/tmp/game-details-ui.md
- Jira: none
- Figma: https://www.figma.com/design/nov1xXgQhkBdxSAiZA3x2E/Rawg.io?node-id=2-3111&t=4Ohe64towZqrxpf5-4
- Other: none

### Context
- The current Game Details screen header is visually too large and should use the shared `AppHeader` component.
- The screen content layout should match the updated design direction: larger banner, card-based details, and clearly labeled sections.
- Media must be split into two sections: "Screenshots" and "Movies".
- Genres must not be displayed on this screen.

### Goals
- Use the shared header component while showing the current game's title in the header.
- Make the banner image more prominent and consistent with game list visuals.
- Rework the details area into a card with the required information layout and actions.
- Provide clear, labeled sections for description, developers/publishers, screenshots, movies, and series games.

### Non-goals
- No changes to data fetching, paging, navigation behavior, or business rules beyond what is required to render the new UI.
- No genre display on this screen (explicitly out of scope even if available in data).

### User experience
- Header:
  - Display the current game's title in the header, using the shared `AppHeader` styling (smaller header).
  - Back navigation remains available.
- Banner:
  - Show the game's banner image with a 2f aspect ratio (same visual ratio as the game list items).
  - Show a rating chip anchored at the top-right corner of the banner.
  - Tapping the banner opens the image as it does today.
- Details card (below banner):
  - Render as a card.
  - Left side content includes, in order:
    - Release date
    - Platforms (icons only)
    - Website button if a website URL exists (hidden if absent)
  - Bookmark action:
    - Bookmark toggle is anchored at the top-right corner of the details card.
- "Description" section:
  - Show label "Description".
  - Show description text.
  - Collapsed state shows up to 6 lines.
  - Show a button "Read more" only when the text overflows in the collapsed state.
  - Expanded state shows full text and the button becomes "Read less".
- "Developers & Publishers" section:
  - Show label "Developers & Publishers".
  - Show a single combined list:
    - Developers first, then publishers.
    - Each row shows: logo, name, and role label ("Developer" or "Publisher").
    - If logo is missing, show a generic placeholder icon consistent with the game list placeholder.
- "Screenshots" section:
  - Keep existing behavior and layout.
- "Movies" section:
  - Show label "Movies".
  - Show a horizontal list of movie thumbnails.
  - Item size matches screenshot item size.
  - Each item displays a play icon overlay (no title overlay).
  - Tapping a movie opens the video as it does today.
- "Series games" section:
  - Show label "Series games".
  - Show a horizontal list of similar games.
  - Use a compact card, but keep the image at `aspectRatio(2f)` (consistent with the game list).
  - Tapping a series game opens that game's details as it does today.
- Empty states:
  - Hide a section entirely when its content is empty (developers/publishers, screenshots, movies, series).

### Functional requirements
1. Replace the current Game Details top app bar with `AppHeader` styling while showing the current game's title text in the header.
2. Banner image uses a 2f aspect ratio and supports tap-to-open-image.
3. Display a rating chip on the banner, anchored top-right.
4. Details card:
   1. Renders as a card container.
   2. Left column shows release date, platform icons, and website action (only when URL exists).
   3. Bookmark toggle is shown at top-right of the card and reflects current bookmark state.
5. Description section:
   1. Includes "Description" label.
   2. Collapses to 6 lines by default.
   3. Shows "Read more" only when overflow occurs.
   4. Expanded state shows full text and uses "Read less" to collapse.
6. Developers & Publishers section:
   1. Includes label "Developers & Publishers".
   2. Shows combined list (developers first, then publishers).
   3. Rows show logo (or placeholder), name, and role label.
7. Screenshots section remains present and unchanged in behavior.
8. Movies section:
   1. Includes label "Movies".
   2. Thumbnails match screenshot item size.
   3. Shows play icon overlay only.
9. Series games section:
   1. Includes label "Series games".
   2. Horizontal list of similar games.
   3. Cards are compact and use image aspect ratio 2f.
10. Do not show game genres anywhere on this screen.

### Data and mapping
- Game title:
  - Type: String
  - Nullability: non-null for rendering; if missing, fall back to an empty string and keep layout stable.
- Release date:
  - Type: String (formatted for display)
  - Nullability: optional; if missing, hide the row or show an empty value consistently.
- Platforms:
  - Type: Set/list of platforms
  - Display: icons only
  - Empty: hide the platforms row.
- Website:
  - Type: URL string
  - Visibility: show website button only if present and non-blank.
- Rating:
  - Type: numeric or formatted string value already used on the screen
  - Display: chip on banner (top-right).
- Developers/publishers:
  - Type: two lists or a combined list
  - Mapping: render as one list with role label derived from source list ("Developer" vs "Publisher").
  - Logo: use placeholder when missing.
- Screenshots/movies/series:
  - Type: paged lists as currently used
  - Empty: hide the whole section.

### API/contracts (if applicable)
- None. Use existing contracts.

### Analytics/observability (if applicable)
- None requested.

### Rollout
- No feature flag requested.

### Acceptance criteria
- Header uses the shared `AppHeader` styling and shows the current game's title.
- Banner image is larger and uses 2f aspect ratio; tapping still opens the image.
- Rating is shown as a chip on the banner top-right.
- Details information is shown inside a card with:
  - Left side: release date, platforms (icons only), and website button (only if URL exists)
  - Bookmark button at the card top-right
- "Description" section has:
  - "Description" label
  - Collapsed text (6 lines) and "Read more" only when overflowing
  - Expanded state with "Read less"
- "Developers & Publishers" section shows a combined list with logo (or placeholder), name, and role label, ordered developers then publishers.
- "Screenshots" section remains available.
- "Movies" section exists, items match screenshot size, and show a play icon overlay (no title).
- "Series games" section exists, horizontal, compact cards with 2f image ratio, and opens game details on tap.
- Genres are not shown on the screen.
- Sections with empty data are hidden.

### Test plan (high-level)
- Unit tests:
  - Mapper/state logic for description expansion and "Read more"/"Read less" label behavior.
  - Developer/publisher list ordering and role labeling.
- UI tests:
  - Verify sections appear/hide based on data presence.
  - Verify website button visibility based on URL presence.
  - Verify banner tap and movie tap trigger the correct events.

### Open questions
- (none)
