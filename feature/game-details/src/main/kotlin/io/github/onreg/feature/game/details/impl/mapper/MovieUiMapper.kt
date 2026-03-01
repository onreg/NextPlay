package io.github.onreg.feature.game.details.impl.mapper

import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.feature.game.details.impl.model.MovieUI
import javax.inject.Inject

internal interface MovieUiMapper {
    fun map(model: Movie): MovieUI
}

internal class MovieUiMapperImpl
    @Inject
    constructor() : MovieUiMapper {
        override fun map(model: Movie): MovieUI = MovieUI(
            id = model.id,
            videoUrl = model.videoUrl,
            previewUrl = model.previewUrl.orEmpty(),
            name = model.name.orEmpty(),
        )
    }
