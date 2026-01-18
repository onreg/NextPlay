package io.github.onreg.data.game.impl.mapper

import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PlatformDto
import io.github.onreg.core.network.rawg.dto.PlatformWrapperDto
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GameDtoMapperTest {
    private val mapper: GameDtoMapper = GameDtoMapperImpl()

    @Test
    fun `should map dto to game model`() {
        val dto = GameDto(
            id = 1,
            title = "Title",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
            rating = 4.5,
            platforms = listOf(PlatformWrapperDto(PlatformDto(GamePlatform.PC.id))),
        )

        val expected = Game(
            id = 1,
            title = "Title",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
            rating = 4.5,
            platforms = setOf(GamePlatform.PC),
        )

        val actual = mapper.map(dto)

        assertEquals(expected, actual)
    }

    @Test
    fun `should fallback no non nullable values when dto properties are nullable`() {
        val dto = GameDto(
            id = 2,
            title = null,
            imageUrl = null,
            releaseDate = null,
            rating = null,
            platforms = emptyList(),
        )

        val expected = Game(
            id = 2,
            title = "",
            imageUrl = "",
            releaseDate = null,
            rating = 0.0,
            platforms = emptySet(),
        )

        val actual = mapper.map(dto)

        assertEquals(expected, actual)
    }

    @Test
    fun `should map platforms and ignore null entries`() {
        val dto = GameDto(
            id = 3,
            title = "Platforms",
            imageUrl = "image",
            releaseDate = null,
            rating = 3.0,
            platforms = listOf(
                PlatformWrapperDto(platform = null),
                PlatformWrapperDto(platform = PlatformDto(GamePlatform.XBOX_ONE.id)),
                PlatformWrapperDto(platform = PlatformDto(999)),
            ),
        )

        val expected = Game(
            id = 3,
            title = "Platforms",
            imageUrl = "image",
            releaseDate = null,
            rating = 3.0,
            platforms = setOf(GamePlatform.XBOX_ONE),
        )

        val actual = mapper.map(dto)

        assertEquals(expected, actual)
    }
}
