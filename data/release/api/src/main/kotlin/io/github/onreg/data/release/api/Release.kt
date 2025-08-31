package io.github.onreg.data.release.api

public data class Release(
    val image: String,
    val title: String,
    val releaseDate: String,
    val genres: Set<String>,
    val platforms: Set<String>,
    val rating: String
)
