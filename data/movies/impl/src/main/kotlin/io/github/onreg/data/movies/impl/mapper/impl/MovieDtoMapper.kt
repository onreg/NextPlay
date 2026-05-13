package io.github.onreg.data.movies.impl.mapper.impl

import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.core.network.rawg.dto.MovieDto
import javax.inject.Inject

public interface MovieDtoMapper {
    public fun map(
        dto: MovieDto,
        gameId: Int,
        position: Long,
    ): MovieEntity?
}

public class MovieDtoMapperImpl
    @Inject
    constructor() : MovieDtoMapper {
        override fun map(
            dto: MovieDto,
            gameId: Int,
            position: Long,
        ): MovieEntity? {
            val videoUrl = selectBestQualityUrl(dto.data) ?: return null
            return MovieEntity(
                id = dto.id,
                gameId = gameId,
                position = position,
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
