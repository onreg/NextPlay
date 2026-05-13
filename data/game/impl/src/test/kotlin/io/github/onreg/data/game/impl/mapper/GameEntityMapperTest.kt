package io.github.onreg.data.game.impl.mapper

import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.list.entity.GameListEntity
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.db.series.entity.SeriesEntity
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GameEntityMapperTest {
    private val mapper: GameEntityMapper = GameEntityMapperImpl()

    @Test
    fun `should map games to game entity`() {
        val games = listOf(
            Game(
                id = 1,
                title = "First",
                imageUrl = "image1",
                releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
                rating = 4.5,
                platforms = emptySet(),
            ),
            Game(
                id = 2,
                title = "Second",
                imageUrl = "image2",
                releaseDate = Instant.parse("2024-02-01T00:00:00Z"),
                rating = 4.0,
                platforms = emptySet(),
            ),
        )

        val expected = listOf(
            GameEntity(
                id = 1,
                title = "First",
                imageUrl = "image1",
                releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
                rating = 4.5,
            ),
            GameEntity(
                id = 2,
                title = "Second",
                imageUrl = "image2",
                releaseDate = Instant.parse("2024-02-01T00:00:00Z"),
                rating = 4.0,
            ),
        )

        val result = mapper.map(games)

        assertEquals(expected, result.games)
    }

    @Test
    fun `should map platforms to platform entity and deduplicate`() {
        val games = listOf(
            Game(
                id = 1,
                title = "First",
                imageUrl = "image1",
                releaseDate = null,
                rating = 1.0,
                platforms = setOf(GamePlatform.PC, GamePlatform.XBOX_ONE),
            ),
            Game(
                id = 2,
                title = "Second",
                imageUrl = "image2",
                releaseDate = null,
                rating = 2.0,
                platforms = setOf(GamePlatform.PC, GamePlatform.XBOX_ONE),
            ),
        )

        val expected = setOf(
            PlatformEntity(GamePlatform.PC.id),
            PlatformEntity(GamePlatform.XBOX_ONE.id),
        )

        val result = mapper.map(games)

        assertEquals(expected, result.platforms.toSet())
    }

    @Test
    fun `should map games and platforms to game platform cross ref and deduplicate`() {
        val games = listOf(
            Game(
                id = 1,
                title = "First",
                imageUrl = "image1",
                releaseDate = null,
                rating = 1.0,
                platforms = setOf(GamePlatform.PC, GamePlatform.XBOX_ONE),
            ),
            Game(
                id = 2,
                title = "Second",
                imageUrl = "image2",
                releaseDate = null,
                rating = 2.0,
                platforms = setOf(GamePlatform.PC, GamePlatform.XBOX_ONE),
            ),
        )

        val expected = setOf(
            GamePlatformCrossRef(gameId = 1, platformId = GamePlatform.PC.id),
            GamePlatformCrossRef(gameId = 1, platformId = GamePlatform.XBOX_ONE.id),
            GamePlatformCrossRef(gameId = 2, platformId = GamePlatform.PC.id),
            GamePlatformCrossRef(gameId = 2, platformId = GamePlatform.XBOX_ONE.id),
        )

        val result = mapper.map(games)

        assertEquals(expected, result.crossRefs.toSet())
    }

    @Test
    fun `should map games to game list entities with incremented positions`() {
        val games = listOf(
            Game(
                id = 1,
                title = "First",
                imageUrl = "image1",
                releaseDate = null,
                rating = 1.0,
                platforms = emptySet(),
            ),
            Game(
                id = 2,
                title = "Second",
                imageUrl = "image2",
                releaseDate = null,
                rating = 2.0,
                platforms = emptySet(),
            ),
        )

        val expected = listOf(
            GameListEntity(gameId = 1, position = 4),
            GameListEntity(gameId = 2, position = 5),
        )

        val result = mapper.mapGameListEntries(games, startPosition = 4)

        assertEquals(expected, result)
    }

    @Test
    fun `should map games to series entities with incremented positions`() {
        val games = listOf(
            Game(
                id = 1,
                title = "First",
                imageUrl = "image1",
                releaseDate = null,
                rating = 1.0,
                platforms = emptySet(),
            ),
            Game(
                id = 2,
                title = "Second",
                imageUrl = "image2",
                releaseDate = null,
                rating = 2.0,
                platforms = emptySet(),
            ),
        )

        val expected = listOf(
            SeriesEntity(gameId = 1, position = 7),
            SeriesEntity(gameId = 2, position = 8),
        )

        val result = mapper.mapSeriesEntries(games, startPosition = 7)

        assertEquals(expected, result)
    }

    @Test
    fun `should map game entity to game`() {
        val model = GameWithPlatforms(
            game = GameEntity(
                id = 3,
                title = "Stored",
                imageUrl = "image",
                releaseDate = Instant.parse("2024-03-03T00:00:00Z"),
                rating = 3.5,
            ),
            platforms = listOf(
                PlatformEntity(GamePlatform.PC.id),
                PlatformEntity(GamePlatform.ANDROID.id),
            ),
        )

        val expected = Game(
            id = 3,
            title = "Stored",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-03-03T00:00:00Z"),
            rating = 3.5,
            platforms = setOf(GamePlatform.PC, GamePlatform.ANDROID),
        )

        val actual = mapper.map(model)

        assertEquals(expected, actual)
    }
}
