package io.github.onreg.data.movies.impl.mapper.impl

import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.movies.impl.mapper.MovieEntityMapper
import javax.inject.Inject

public class MovieEntityMapperImpl
    @Inject
    constructor() : MovieEntityMapper {
        override fun map(entity: MovieEntity): Movie = Movie(
            id = entity.id,
            name = entity.name,
            previewUrl = entity.previewUrl,
            videoUrl = entity.videoUrl,
        )
    }
