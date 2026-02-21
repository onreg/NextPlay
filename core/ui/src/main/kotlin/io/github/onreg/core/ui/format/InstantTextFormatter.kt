package io.github.onreg.core.ui.format

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

public const val DefaultInstantTextPattern: String = "MMM d, yyyy"

public interface InstantTextFormatter {
    public fun format(
        instant: Instant,
        pattern: String = DefaultInstantTextPattern,
        zoneId: ZoneId = ZoneOffset.UTC,
        locale: Locale = Locale.US,
    ): String
}

public class InstantTextFormatterImpl
@Inject
constructor() : InstantTextFormatter {
    override fun format(
        instant: Instant,
        pattern: String,
        zoneId: ZoneId,
        locale: Locale,
    ): String = instant
        .atZone(zoneId)
        .format(DateTimeFormatter.ofPattern(pattern, locale))
        .orEmpty()
}
