package io.github.onreg.core.network.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal class InstantJsonAdapter {
    @ToJson
    fun toJson(value: Instant?): String? = value?.toString()

    @FromJson
    fun fromJson(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        return runCatching {
            LocalDate
                .parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
        }.getOrNull()
    }
}
