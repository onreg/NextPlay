package io.github.onreg.core.ui.format

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

public object ReleaseDateFormatter {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

    public fun format(releaseDate: Instant?): String = releaseDate
        ?.atZone(ZoneOffset.UTC)
        ?.format(formatter)
        .orEmpty()
}
