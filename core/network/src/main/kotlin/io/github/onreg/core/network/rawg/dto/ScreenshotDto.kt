package io.github.onreg.core.network.rawg.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class ScreenshotDto(
    @Json(name = "id") val id: Int,
    @Json(name = "image") val imageUrl: String?,
    @Json(name = "width") val width: Int?,
    @Json(name = "height") val height: Int?,
    @Json(name = "is_deleted") val isDeleted: Boolean?,
)
