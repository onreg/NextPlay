package io.github.onreg.data.movies.impl.mapper

import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.data.movies.api.model.Movie
import javax.inject.Inject

public interface MovieEntityMapper {
    public fun mapToEntity(
        model: Movie,
        gameId: Int,
        insertionOrder: Long,
    ): MovieEntity

    public fun map(entity: MovieEntity): Movie
}

public class MovieEntityMapperImpl
    @Inject
    constructor() : MovieEntityMapper {
        override fun mapToEntity(
            model: Movie,
            gameId: Int,
            insertionOrder: Long,
        ): MovieEntity = MovieEntity(
            id = model.id,
            gameId = gameId,
            name = model.name,
            previewUrl = model.previewUrl,
            bestVideoUrl = model.videoUrl,
            insertionOrder = insertionOrder,
        )

        override fun map(entity: MovieEntity): Movie = Movie(
            id = entity.id,
            name = entity.name,
            previewUrl = entity.previewUrl,
            videoUrl = entity.bestVideoUrl,
        )
    }
