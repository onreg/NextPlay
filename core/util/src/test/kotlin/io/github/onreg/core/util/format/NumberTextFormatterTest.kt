package io.github.onreg.core.util.format

import kotlin.test.Test
import kotlin.test.assertEquals

internal class NumberTextFormatterTest {
    private val formatter = NumberTextFormatterImpl()

    @Test
    fun `should trim zero fraction when enabled`() {
        val formatted = formatter.format(value = 2.0)

        assertEquals("2", formatted)
    }

    @Test
    fun `should keep non zero fraction`() {
        val formatted = formatter.format(value = 2.1)

        assertEquals("2.1", formatted)
    }

    @Test
    fun `should round down with configured fraction digits`() {
        val formatted = formatter.format(value = 2.05)

        assertEquals("2", formatted)
    }
}
