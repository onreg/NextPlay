package io.github.onreg.core.network.rawg.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class GameDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "background_image") val backgroundImage: String?,
    @Json(name = "released") val released: String?,
    @Json(name = "rating") val rating: Double?,
    @Json(name = "platforms") val platforms: List<PlatformWrapperDto> = emptyList()
)

@JsonClass(generateAdapter = true)
public data class PlatformWrapperDto(
    @Json(name = "platform") val platform: PlatformDto?
)

@JsonClass(generateAdapter = true)
public data class PlatformDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?
)
