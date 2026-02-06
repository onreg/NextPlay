package io.github.onreg.core.network.rawg.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
public data class GameDetailsDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val title: String?,
    @Json(name = "background_image") val imageUrl: String?,
    @Json(name = "background_image_additional") val bannerImageUrl: String?,
    @Json(name = "released") val releaseDate: Instant?,
    @Json(name = "website") val websiteUrl: String?,
    @Json(name = "rating") val rating: Double?,
    @Json(name = "description") val descriptionHtml: String?,
    @Json(name = "parent_platforms") val platforms: List<PlatformWrapperDto> = emptyList(),
    @Json(name = "developers") val developers: List<DeveloperDto> = emptyList(),
)
