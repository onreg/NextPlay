package io.github.onreg.data.movies.impl.mapper

import io.github.onreg.core.network.rawg.dto.MovieDto
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.movies.impl.quality.MovieQualitySelector
import javax.inject.Inject

public interface MovieDtoMapper {
    public fun map(dto: MovieDto): Movie?
}

public class MovieDtoMapperImpl
    @Inject
    constructor(
        private val qualitySelector: MovieQualitySelector,
    ) : MovieDtoMapper {
        override fun map(dto: MovieDto): Movie? {
            val bestUrl = qualitySelector.bestUrl(dto.data) ?: return null
            return Movie(
                id = dto.id,
                name = dto.name,
                previewUrl = dto.previewUrl,
                videoUrl = bestUrl,
            )
        }
    }
