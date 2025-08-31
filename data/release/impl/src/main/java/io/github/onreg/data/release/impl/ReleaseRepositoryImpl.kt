package io.github.onreg.data.release.impl

import io.github.onreg.data.release.api.Release
import io.github.onreg.data.release.api.ReleaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ReleaseRepositoryImpl @Inject constructor() : ReleaseRepository {
    override fun getReleases(): Flow<List<Release>> {
        return flowOf(
            listOf(
                Release(
                    image = "https://picsum.photos/seed/1/200/300",
                    title = "Cyberpunk 2077",
                    releaseDate = "2020-12-10",
                    genres = setOf("RPG"),
                    platforms = setOf("PC", "PlayStation", "Xbox"),
                    rating = "4"
                ),
                Release(
                    image = "https://picsum.photos/seed/2/200/300",
                    title = "The Witcher 3: Wild Hunt",
                    releaseDate = "2015-05-19",
                    genres = setOf("RPG"),
                    platforms = setOf("PC", "PlayStation", "Xbox", "Nintendo Switch"),
                    rating = "4"
                ),
                Release(
                    image = "https://picsum.photos/seed/3/200/300",
                    title = "Red Dead Redemption 2",
                    releaseDate = "2018-10-26",
                    genres = setOf("Action-adventure"),
                    platforms = setOf("PC", "PlayStation", "Xbox"),
                    rating = "4"
                ),
                Release(
                    image = "https://picsum.photos/seed/4/200/300",
                    title = "Grand Theft Auto V",
                    releaseDate = "2013-09-17",
                    genres = setOf("Action-adventure"),
                    platforms = setOf("PC", "PlayStation", "Xbox"),
                    rating = "4"
                ),
                Release(
                    image = "https://picsum.photos/seed/5/200/300",
                    title = "Minecraft",
                    releaseDate = "2011-11-18",
                    genres = setOf("Sandbox", "Survival"),
                    platforms = setOf("PC", "PlayStation", "Xbox", "Nintendo Switch", "Mobile"),
                    rating = "2"
                ),
                Release(
                    image = "https://picsum.photos/seed/6/200/300",
                    title = "The Legend of Zelda: Breath of the Wild",
                    releaseDate = "2017-03-03",
                    genres = setOf("Action-adventure"),
                    platforms = setOf("Nintendo Switch", "Wii U"),
                    rating = "2"
                ),
                Release(
                    image = "https://picsum.photos/seed/7/200/300",
                    title = "Overwatch 2",
                    releaseDate = "2022-10-04",
                    genres = setOf("First-person shooter"),
                    platforms = setOf("PC", "PlayStation", "Xbox", "Nintendo Switch"),
                    rating = "3"
                ),
                Release(
                    image = "https://picsum.photos/seed/8/200/300",
                    title = "Fortnite",
                    releaseDate = "2017-07-25",
                    genres = setOf("Battle royale"),
                    platforms = setOf("PC", "PlayStation", "Xbox", "Nintendo Switch", "Mobile"),
                    rating = "3"
                ),
                Release(
                    image = "https://picsum.photos/seed/9/200/300",
                    title = "Animal Crossing: New Horizons",
                    releaseDate = "2020-03-20",
                    genres = setOf("Social simulation"),
                    platforms = setOf("Nintendo Switch"),
                    rating = "1"
                ),
                Release(
                    image = "https://picsum.photos/seed/10/200/300",
                    title = "Stardew Valley",
                    releaseDate = "2016-02-26",
                    genres = setOf("Simulation RPG"),
                    platforms = setOf("PC", "PlayStation", "Xbox", "Nintendo Switch", "Mobile"),
                    rating = "2"
                ),
                Release(
                    image = "https://picsum.photos/seed/11/200/300",
                    title = "Hades",
                    releaseDate = "2020-09-17",
                    genres = setOf("Roguelike", "Action RPG"),
                    platforms = setOf("PC", "Nintendo Switch", "PlayStation", "Xbox"),
                    rating = "3"
                ),
                Release(
                    image = "https://picsum.photos/seed/12/200/300",
                    title = "Elden Ring",
                    releaseDate = "2022-02-25",
                    genres = setOf("Action RPG"),
                    platforms = setOf("PC", "PlayStation", "Xbox"),
                    rating = "4"
                ),
                Release(
                    image = "httpsum.photos/seed/13/200/300",
                    title = "God of War Ragnarök",
                    releaseDate = "2022-11-09",
                    genres = setOf("Action-adventure"),
                    platforms = setOf("PlayStation"),
                    rating = "4"
                ),
                Release(
                    image = "https://picsum.photos/seed/14/200/300",
                    title = "Spider-Man: Miles Morales",
                    releaseDate = "2020-11-12",
                    genres = setOf("Action-adventure"),
                    platforms = setOf("PlayStation"),
                    rating = "3"
                ),
                Release(
                    image = "httpsum.photos/seed/15/200/300",
                    title = "Horizon Forbidden West",
                    releaseDate = "2022-02-18",
                    genres = setOf("Action RPG"),
                    platforms = setOf("PlayStation"),
                    rating = "3"
                ),
                Release(
                    image = "https://picsum.photos/seed/16/200/300",
                    title = "Super Mario Odyssey",
                    releaseDate = "2017-10-27",
                    genres = setOf("Platformer"),
                    platforms = setOf("Nintendo Switch"),
                    rating = "2"
                ),
                Release(
                    image = "httpsum.photos/seed/17/200/300",
                    title = "Valorant",
                    releaseDate = "2020-06-02",
                    genres = setOf("First-person shooter"),
                    platforms = setOf("PC"),
                    rating = "3"
                ),
                Release(
                    image = "https://picsum.photos/seed/18/200/300",
                    title = "League of Legends",
                    releaseDate = "2009-10-27",
                    genres = setOf("MOBA"),
                    platforms = setOf("PC"),
                    rating = "3"
                ),
                Release(
                    image = "https://picsum.photos/seed/19/200/300",
                    title = "Among Us",
                    releaseDate = "2018-06-15",
                    genres = setOf("Social deduction"),
                    platforms = setOf("PC", "Mobile", "Nintendo Switch", "PlayStation", "Xbox"),
                    rating = "2"
                ),
                Release(
                    image = "https://picsum.photos/seed/20/200/300",
                    title = "Genshin Impact",
                    releaseDate = "2020-09-28",
                    genres = setOf("Action RPG"),
                    platforms = setOf("PC", "PlayStation", "Mobile"),
                    rating = "3"
                )
            )
        )
    }
}