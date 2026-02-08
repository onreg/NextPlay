package io.github.onreg.core.ui.format

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

internal class ReleaseDateFormatterTest {
    @Test
    fun `format should use utc date pattern`() {
        val formatted = ReleaseDateFormatter.format(Instant.parse("2024-01-05T23:59:59Z"))

        assertEquals("Jan 5, 2024", formatted)
    }

    @Test
    fun `format should return empty string for null`() {
        val formatted = ReleaseDateFormatter.format(null)

        assertEquals("", formatted)
    }
}
