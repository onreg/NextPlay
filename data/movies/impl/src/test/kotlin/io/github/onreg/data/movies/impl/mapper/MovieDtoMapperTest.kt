package io.github.onreg.data.movies.impl.mapper

import io.github.onreg.core.network.rawg.dto.MovieDto
import io.github.onreg.data.movies.impl.mapper.impl.MovieDtoMapperImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class MovieDtoMapperTest {
    private val mapper = MovieDtoMapperImpl()
    private val gameId = 42
    private val position = 3L

    @Test
    fun `should prefer max quality url when present`() {
        val dto = MovieDto(
            id = 1,
            name = "Trailer",
            previewUrl = null,
            data = mapOf(
                "480" to "https://video/480.mp4",
                "max" to "https://video/max.mp4",
            ),
        )

        val result = mapper.map(dto, gameId, position)

        assertEquals("https://video/max.mp4", result?.videoUrl)
    }

    @Test
    fun `should use highest numeric quality when max is absent`() {
        val dto = MovieDto(
            id = 1,
            name = "Trailer",
            previewUrl = null,
            data = mapOf(
                "360" to "https://video/360.mp4",
                "720" to "https://video/720.mp4",
                "480" to "https://video/480.mp4",
            ),
        )

        val result = mapper.map(dto, gameId, position)

        assertEquals("https://video/720.mp4", result?.videoUrl)
    }

    @Test
    fun `should return null when no playable url exists`() {
        val dto = MovieDto(
            id = 1,
            name = "Trailer",
            previewUrl = null,
            data = emptyMap(),
        )

        val result = mapper.map(dto, gameId, position)

        assertNull(result)
    }

    @Test
    fun `should fall back to numeric quality when max is blank`() {
        val dto = MovieDto(
            id = 1,
            name = "Trailer",
            previewUrl = null,
            data = mapOf(
                "max" to " ",
                "1080" to "https://video/1080.mp4",
                "720" to "https://video/720.mp4",
            ),
        )

        val result = mapper.map(dto, gameId, position)

        assertEquals("https://video/1080.mp4", result?.videoUrl)
    }

    @Test
    fun `should return null when highest numeric quality url is blank`() {
        val dto = MovieDto(
            id = 1,
            name = "Trailer",
            previewUrl = null,
            data = mapOf(
                "360" to "https://video/360.mp4",
                "720" to "",
            ),
        )

        val result = mapper.map(dto, gameId, position)

        assertNull(result)
    }

    @Test
    fun `should map dto fields into entity`() {
        val dto = MovieDto(
            id = 10,
            name = "Teaser",
            previewUrl = "https://video/preview.jpg",
            data = mapOf(
                "max" to "https://video/max.mp4",
            ),
        )

        val result = mapper.map(dto, gameId, position)

        assertEquals(10, result?.id)
        assertEquals(gameId, result?.gameId)
        assertEquals(position, result?.position)
        assertEquals("Teaser", result?.name)
        assertEquals("https://video/preview.jpg", result?.previewUrl)
    }
}
