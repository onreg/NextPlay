package io.github.onreg.core.network.rawg.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class PaginatedResponseDto<T>(
    @Json(name = "count") val count: Int,
    @Json(name = "next") val next: String?,
    @Json(name = "previous") val previous: String?,
    @Json(name = "results") val results: List<T> = emptyList()
)
