package io.github.onreg.data.movies.impl.quality

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class MovieQualitySelectorTest {
    private val selector: MovieQualitySelector = MovieQualitySelectorImpl()

    @Test
    fun `should prefer max when available`() {
        val result = selector.bestUrl(
            mapOf(
                "480" to "low",
                "max" to "high",
            ),
        )

        assertEquals("high", result)
    }

    @Test
    fun `should choose highest numeric quality when max missing`() {
        val result = selector.bestUrl(
            mapOf(
                "360" to "low",
                "720" to "high",
                "480" to "mid",
            ),
        )

        assertEquals("high", result)
    }

    @Test
    fun `should ignore non numeric keys when selecting`() {
        val result = selector.bestUrl(
            mapOf(
                "auto" to "auto",
                "1080" to "best",
                "480" to "mid",
            ),
        )

        assertEquals("best", result)
    }

    @Test
    fun `should return null when no values available`() {
        val result = selector.bestUrl(emptyMap())

        assertNull(result)
    }
}
