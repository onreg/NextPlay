Your task is to improve UI game details page.
Acceptance criteria:
1. Header. Now it is too big. use [AppHeader.kt](core/ui/src/main/kotlin/io/github/onreg/core/ui/components/header/AppHeader.kt) component.
2. Banner image. Increase the height.
3. Game details:
   - It should be a card.
   - Left side should contain: Release data, platforms, link to web site.
   - Bookmark right top corner of the card.
   - Rating left bottom corner of the card.
4. Section "Description". It should contain label "Description" and description text, and button "Read more" if description is too long.
5. Section "Developers & Publishers". It should contain label "Developers & Publishers" and list of developers and publishers.
   - List item should contain: logo, name, role (developer or publisher).
6. Section "Screenshots". Good keep it.
7. Section movies. It should contain label "Movies" and list of movies with thumbnail and size the same as screenshots.
8. Section "Series games". It should contain label "Series games" and list of similar games with horizontal scroll.

Link to design:
https://www.figma.com/design/nov1xXgQhkBdxSAiZA3x2E/Rawg.io?node-id=2-3111&t=4Ohe64towZqrxpf5-4

There is some differences between design and acceptance criteria:
1. No need to show Genres of the game.
2. Media is divided into two sections: "Screenshots" and "Movies".
3. Series games should be in 2f aspect ratio the same as GameCard in the list of games.
