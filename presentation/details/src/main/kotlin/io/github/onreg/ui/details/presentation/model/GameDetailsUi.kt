package io.github.onreg.ui.details.presentation.model

public data class GameDetailsUi(
    val id: String,
    val title: String,
    val bannerImageUrl: String?,
    val releaseDate: String,
    val rating: String?,
    val websiteUrl: String?,
    val isWebsiteVisible: Boolean,
    val descriptionHtml: String?,
    val developers: List<String>,
)
