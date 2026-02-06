package io.github.onreg.core.network.rawg.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class DeveloperDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
)
