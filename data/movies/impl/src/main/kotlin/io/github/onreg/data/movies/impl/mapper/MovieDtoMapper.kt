package io.github.onreg.data.movies.impl.mapper

import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.core.network.rawg.dto.MovieDto

public interface MovieDtoMapper {
    public fun map(dto: MovieDto): MovieEntity?
}
