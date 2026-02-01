## Task Prompt

### Title
- Game details screen (offline-first) with paged screenshots, movies, and series

### Source links
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

### Context
- The app has a game list screen; tapping a game card should navigate to a game details screen showing more information for that game.
- The details screen must be offline-first: show cached data immediately when available, and refresh from network.
- Screenshots, movies, and series content are paginated and must be implemented offline-first using the same pattern and paging configuration as the game list (RemoteMediator + Room).
- Documentation and the example response docs are the source of truth when design differs.

### Architecture/design notes
- Gradle modules split (intended):
  - Feature modules:
    - "feature/games-list"
    - "feature/games-details"
  - Data modules (each split into api + impl):
    - "data/game-list/api" and "data/game-list/impl"
    - "data/details/api" and "data/details/impl"
    - "data/screenshots/api" and "data/screenshots/impl"
    - "data/movies/api" and "data/movies/impl"
    - "data/series/api" and "data/series/impl"
- Feature responsibilities:
  - "feature/games-list" owns the list screen UI and navigation entry point, list-only UI state (including the current bookmark toggle behavior), and emits navigation events to open details for a selected gameId.
  - "feature/games-details" owns the details screen UI and navigation entry point, composes the full details experience (details + screenshots + movies + series), owns all intent launching (website, screenshot viewer, movie player), and supports "series game tap" by pushing a new details instance for the selected gameId.
- Data layer responsibilities and boundaries:
  - Each "data/*/api" module exposes only public contracts for its capability (repository interfaces plus domain-facing models where applicable) and stays free of Android wiring concerns.
  - Each "data/*/impl" module depends on its matching api module and implements the contracts using core infrastructure (for example "core/network" for Retrofit/RAWG calls and "core/db" for Room persistence), including caching, paging orchestration, and mapping between network, database, and api-facing models.
- Dependency direction (high level):
  - Feature modules depend on "data/*/api" contracts, not "data/*/impl" implementations.
  - App/root DI composes bindings by including "data/*/impl" modules on the classpath so their DI modules bind implementations into the corresponding "data/*/api" interfaces; features receive only the api interfaces.
  - Shared UI primitives live in "core/ui" and shared persistence/network infrastructure lives in "core/db" and "core/network" (matching the existing conventions).
- Offline-first behavior (details):
  - The details screen renders from the local cache first when available, then triggers a refresh that updates the cache from network.
  - The "no cache yet" path stays explicit: UI shows a skeleton until the first cached details is available, and shows a full-screen error + Retry only when the initial fetch fails with an empty cache.
- Offline-first paging (screenshots, movies, series):
  - Each paged section uses the same Room-backed PagingSource + RemoteMediator pattern as the existing game list, with paging configuration kept consistent across list and details sections.
  - Pagination state is isolated per collection (list, screenshots, movies, series) via collection-specific remote keys, so each feed can refresh and append independently.
- Reuse rules:
  - Reuse a shared "Game" card model across list and series UI, so series items behave and render like game list cards.
  - Reuse the shared "GameEntity" table for storing game card data (id, title, image, release date, rating, platforms as applicable) for both list and series.
  - Store collection membership and ordering in collection-specific join tables:
    - Game list membership keyed by listKey (for example to support multiple lists over time).
    - Series membership keyed by parent gameId (to preserve ordering within a game details context).

### Goals
- Open game details from the game list and render the specified sections.
- Use cached game details content first, then update from network.
- Implement offline-first pagination for screenshots, movies, and series content.
- Provide correct external intents for website, screenshot viewing, and movie playback.

### Non-goals
- Implement a Share action in the header.
- Implement "Similar Games" section.
- Persist bookmarks beyond the existing game list behavior (current MVP behavior stays unchanged).
- Build a custom in-app full-screen viewer/player (system viewers/players only).

### User experience
- Entry point: user taps a game card on the game list screen.
- Navigation: push a Game Details screen for the tapped game.
- Header:
  - Back button navigates back to game list.
  - Title shows the current game title.
  - No other header actions.
- Initial state:
  - If there is cached game details data, render it immediately and start a background refresh.
  - If there is no cached game details data, show a full-screen skeleton loading state until the first data is available.
- Primary content layout (in order):
  1) Banner image
  2) Details card
     - Release date
     - Platforms (same visual treatment as game list platforms component)
     - Official Website (only if website URL is valid)
     - Bookmark toggle (same behavior and visual treatment as game list)
     - Rating shown as a number with one decimal place, using the same rating chip styling as game list
  3) Description section
     - Collapsed by default to 5 lines
     - Toggle link: "Read more" expands, "Read less" collapses
  4) Screenshots section
     - Title: "Screenshots"
     - Horizontal carousel of screenshots (paged)
  5) Movies section
     - Title: "Movies"
     - Horizontal carousel of movies (paged)
  6) Developers section
  7) Series section
     - Title: "Series"
     - Horizontal carousel of games in the series (paged)
- Interactions:
  - Official Website tap: open website URL in the system browser.
  - Screenshot tap: open the tapped image in the system image viewer (single item).
  - Movie tap: open the tapped movie in the system video player (single item), using the best available quality URL.
  - Series game tap: open a new Game Details screen for that game (push onto back stack).
- Errors and offline behavior:
  - If there is no cached game details data and the initial network fetch fails: show a full-screen error state with a Retry action.
  - For screenshots/movies/series: when offline and there is no cached content for a section, hide that section entirely (including its title).

### Functional requirements
1) Game list to details navigation
   1. Tapping a game card navigates to the game details screen for that game ID.
   2. Back button returns to the game list screen.
2) Offline-first game details data
   1. On opening game details, render cached data first when available.
   2. Always refresh from network after showing cached data.
   3. When no cached data exists, show a full-screen skeleton loader until data is available.
   4. When no cached data exists and network fails, show full-screen error with Retry.
3) Details card content
   1. Show release date.
   2. Show platforms using the same UI pattern as the game list.
   3. Show official website row only when the website URL is valid; tapping opens system browser.
   4. Show bookmark toggle using the same behavior as the game list (ViewModel-backed, no new persistence).
   5. Show rating using the RAWG "rating" field formatted to one decimal place.
4) Description expand/collapse
   1. Collapsed by default to 5 lines.
   2. Tapping "Read more" expands the full description.
   3. Tapping "Read less" collapses back to 5 lines.
5) Screenshots section
   1. Fetch screenshots from `/games/{id}/screenshots`.
   2. Implement pagination and offline-first caching using RemoteMediator + Room, with the same paging configuration as the game list.
   3. Tapping a screenshot opens that single image in the system image viewer.
6) Movies section
   1. Fetch movies from `/games/{id}/movies`.
   2. Implement pagination and offline-first caching using RemoteMediator + Room, with the same paging configuration as the game list.
   3. Tapping a movie opens that single movie in the system video player.
   4. Best available quality selection:
      - If the movie "data" map contains a "max" key, use its URL.
      - Otherwise, use the URL for the highest numeric resolution key present (for example "480").
7) Developers section
   1. Show developers as provided by the game details response.
8) Series section
   1. Fetch series games from `/games/{id}/game-series` (source of truth: docs/api/game_series.md).
   2. Implement pagination and offline-first caching using RemoteMediator + Room, with the same paging configuration as the game list.
   3. Tapping a series game opens game details for that game in a new details screen pushed onto the back stack.

### Data and mapping
- Game details (primary screen content)
  - `id`: Int, non-null
  - `name`: String, non-null
  - `background_image`: String URL, nullable (banner)
  - `released`: String (YYYY-MM-DD), nullable (display as date)
  - `platforms`: List, nullable/empty (display platforms)
  - `website`: String URL, nullable/blank/invalid (if invalid: hide website row)
  - `rating`: Double, nullable (display with one decimal place if present)
  - `description`: String (HTML), nullable (display as formatted text; collapsed/expanded behavior applies)
  - `developers`: List, nullable/empty (display developers section; hide if empty)
- Screenshots item
  - `id`: Int, non-null
  - `image`: String URL, non-null (open in system image viewer)
  - `width`/`height`: Int, nullable (optional)
- Movie item
  - `id`: Int, non-null
  - `name`: String, nullable
  - `preview`: String URL, nullable (thumbnail)
  - `data`: Map<String, String>, non-null (quality URLs)
  - Quality URL mapping: "max" preferred; else highest numeric key.
- Series game item
  - Use the same subset as game list cards (id, name, released, background image, rating, platforms as applicable).
- Bookmark state
  - Boolean derived from existing game list bookmark state rules (ViewModel-backed, no additional persistence in this task).

### API/contracts (if applicable)
- Game details: `GET /games/{id}`
- Screenshots: `GET /games/{id}/screenshots?page=<n>`
- Movies: `GET /games/{id}/movies?page=<n>`
- Series: `GET /games/{id}/game-series?page=<n>`
- Requests include the RAWG API key in the same manner as existing network calls in the app.
- Pagination fields follow the RAWG pattern: `count`, `next`, `previous`, `results`.

### Analytics/observability (if applicable)
- None.

### Rollout
- No feature flag required.

### Acceptance criteria
- Tapping a game card on the game list opens a game details screen for that game.
- Back button returns to the game list.
- If cached game details exists, it is displayed immediately, then refreshed from network.
- If no cached game details exists, a full-screen skeleton loading state is shown until initial data arrives.
- If no cached game details exists and the initial fetch fails, a full-screen error state with Retry is shown; Retry triggers a refetch.
- Details card shows release date, platforms, rating (one decimal), and bookmark toggle with the same visual treatment and behavior as the game list.
- Official Website row is shown only when the website URL is valid; tapping opens the system browser to that URL.
- Description is collapsed to 5 lines by default and toggles correctly between "Read more" and "Read less".
- Screenshots section:
  - Is titled "Screenshots" and uses a paged carousel.
  - Is fetched from `/games/{id}/screenshots` with offline-first pagination and caching.
  - Tapping a screenshot opens the system image viewer for that single image.
- Movies section:
  - Is titled "Movies" and uses a paged carousel.
  - Is fetched from `/games/{id}/movies` with offline-first pagination and caching.
  - Tapping a movie opens the system video player for that single movie.
  - Movie URL selection uses "max" when present, otherwise the highest numeric quality key.
- Developers section is shown using data from the game details response (and hidden when empty).
- Series section:
  - Is titled "Series" and uses a paged carousel.
  - Is fetched from `/games/{id}/game-series` with offline-first pagination and caching.
  - Tapping a series game opens a new game details screen pushed onto the back stack.
- When offline and a paged section has no cached content, that section is hidden entirely.
- No Share action and no "Similar Games" section is implemented.

### Test plan (high-level)
- Unit tests:
  - Movie quality URL selection logic ("max" preferred, else highest numeric).
  - Description collapse/expand state and label switching behavior.
  - Repository behavior for offline-first refresh (cache-first, then network update) at least for the game details path.
- Integration tests:
  - RemoteMediator + Room pagination for screenshots, movies, and series uses the same paging configuration as game list and correctly persists pages.
- UI tests:
  - Navigation from list to details and back.
  - Skeleton shown when no cache; error + Retry shown on initial failure.
  - Website link launches a browser intent when valid; hidden when invalid.
  - Screenshot and movie taps launch appropriate system intents.
  - Series game tap opens a new details instance on the back stack.

### Open questions
- None.
