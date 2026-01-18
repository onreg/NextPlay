package io.github.onreg.data.game.impl.mapper

import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GameEntityMapperTest {
    private val mapper: GameEntityMapper = GameEntityMapperImpl()

    @Test
    fun `should map games to game entity with incremented order`() {
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
                insertionOrder = 5,
            ),
            GameEntity(
                id = 2,
                title = "Second",
                imageUrl = "image2",
                releaseDate = Instant.parse("2024-02-01T00:00:00Z"),
                rating = 4.0,
                insertionOrder = 6,
            ),
        )

        val result = mapper.map(games, startOrder = 5)

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

        val result = mapper.map(games, startOrder = 0)

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

        val result = mapper.map(games, startOrder = 0)

        assertEquals(expected, result.crossRefs.toSet())
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
                insertionOrder = 10,
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
