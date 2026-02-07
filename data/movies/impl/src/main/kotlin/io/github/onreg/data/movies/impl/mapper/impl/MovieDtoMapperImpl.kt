package io.github.onreg.data.movies.impl.mapper.impl

import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.core.network.rawg.dto.MovieDto
import io.github.onreg.data.movies.impl.mapper.MovieDtoMapper
import javax.inject.Inject

public class MovieDtoMapperImpl
    @Inject
    constructor() : MovieDtoMapper {
        override fun map(dto: MovieDto): MovieEntity? {
            val videoUrl = selectBestQualityUrl(dto.data) ?: return null
            return MovieEntity(
                id = dto.id,
                name = dto.name,
                previewUrl = dto.previewUrl,
                videoUrl = videoUrl,
            )
        }

        internal fun selectBestQualityUrl(data: Map<String, String>): String? {
            data["max"]?.takeIf { it.isNotBlank() }?.let { return it }

            return data
                .mapNotNull { (quality, url) -> quality.toIntOrNull()?.let { it to url } }
                .maxByOrNull { it.first }
                ?.second
                ?.takeIf { it.isNotBlank() }
        }
    }
