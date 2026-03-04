package io.github.onreg.core.util.format

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class InstantTextFormatterTest {
    private val formatter = InstantTextFormatterImpl()

    @Test
    fun `should format instant with utc zone and pattern`() {
        val formatted = formatter.format(instant = Instant.parse("2024-01-05T23:59:59Z"))

        assertEquals("Jan 5, 2024", formatted)
    }
}
