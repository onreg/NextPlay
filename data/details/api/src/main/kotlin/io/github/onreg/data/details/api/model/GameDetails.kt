package io.github.onreg.data.details.api.model

import java.time.Instant

public data class GameDetails(
    val id: Int,
    val name: String,
    val bannerImageUrl: String?,
    val releaseDate: Instant?,
    val websiteUrl: String?,
    val rating: Double?,
    val descriptionHtml: String?,
    val developers: List<Developer>,
)

public data class Developer(
    val id: Int,
    val name: String,
)
