package io.github.onreg.data.movies.impl.mapper

import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.data.movies.api.model.Movie

public interface MovieEntityMapper {
    public fun map(entity: MovieEntity): Movie
}
