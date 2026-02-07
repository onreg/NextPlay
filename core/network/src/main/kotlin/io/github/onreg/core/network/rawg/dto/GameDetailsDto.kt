package io.github.onreg.core.network.rawg.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
public data class GameDetailsDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val title: String?,
    @Json(name = "background_image") val imageUrl: String?,
    @Json(name = "released") val releaseDate: Instant?,
    @Json(name = "platforms") val platforms: List<PlatformWrapperDto> = emptyList(),
    @Json(name = "parent_platforms") val parentPlatforms: List<PlatformWrapperDto> = emptyList(),
    @Json(name = "website") val website: String?,
    @Json(name = "rating") val rating: Double?,
    @Json(name = "description") val description: String?,
    @Json(name = "developers") val developers: List<DeveloperDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
public data class DeveloperDto(
    @Json(name = "name") val name: String?,
)
