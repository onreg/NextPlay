Your task is to implement job details feature. In the game list if a user clicks a card, they
should be navigated to the game details screen which shows more information about the game.

Main components of the game details screen are:

1. Header:
    - Back Button.
    - Game Title.
2. Game banner image.
3. Game details card:
    - Date released.
    - Platforms.
    - Link to official website.
    - Bookmark button.
    - Rating..
4. Game description.
5. Screenshots.
    - Title "Screenshots"
    - Screenshots carousel
6. Movies.
    - Title "Movies"
    - Movies carousel
7. Developers
8. Series:
    - Title "Series"
    - Games carousel.

Functional requirements:

- By clicking on it user should be navigated back to the game list screen
- For platforms, rating and bookmark button reuse the same components as in the game list.
- Bookmarks should be stored in the ViewModel the same as in the game list.
- The link to the official website should be opened in the system browser.
- Game description should be expanded/collapsed by clicking on the "Read more"/"Read less" link. The
  max number of lines when collapsed is 5.
- When a user clicks on the screenshots or movies carousel item, it should be opened in full screen.
  Use system image viewer for screenshots and system video player for movies. The best available
  quality should be used if there are multiple options.
- When a user clicks on a game in the series carousel, the game details screen should be opened
  for that game.
- Screenshots, movies and series should be fetched from their respective endpoints:
    - `/games/{id}/screenshots`
    - `/games/{id}/movies`
    - `/games/{id}/series`
- Screenshots, movies and series should be implemented with pagination. The pagination
  should be offline first and implemented the same way as in the game list with RemoteMediator and
  Room.
- When a user clicks a game in the game list, the game details screen should be opened and
  data should be fetched from cache and then updated from the network.

Artifacts:

RAWG API documentation:
https://api.rawg.io/docs/

Example of responses:
[game_details.md](../api/game_details.md)
[game_movies.md](../api/game_movies.md)
[game_screenshots.md](../api/game_screenshots.md)
[game_series.md](../api/game_series.md)

Dark theme design:
https://www.figma.com/design/nov1xXgQhkBdxSAiZA3x2E/Rawg.io?node-id=2-3111&t=cMpkGbed1H7Gieuo-4

Light theme design:
https://www.figma.com/design/nov1xXgQhkBdxSAiZA3x2E/Rawg.io?node-id=2-3737&t=cMpkGbed1H7Gieuo-4

Important: Use this documentation as a sourth of truth. The design might differ from the
documentation.
