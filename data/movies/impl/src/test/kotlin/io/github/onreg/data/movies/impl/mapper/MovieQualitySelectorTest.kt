package io.github.onreg.data.movies.impl.mapper

import io.github.onreg.core.network.rawg.dto.MovieDto
import io.github.onreg.data.movies.impl.mapper.impl.MovieDtoMapperImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class MovieQualitySelectorTest {
    private val mapper = MovieDtoMapperImpl()

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

        val result = mapper.map(dto)

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

        val result = mapper.map(dto)

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

        val result = mapper.map(dto)

        assertNull(result)
    }
}
