package io.github.onreg.data.details.impl.mapper

import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.network.rawg.dto.GameDetailsDto

public interface GameDetailsDtoMapper {
    public fun map(dto: GameDetailsDto): GameDetailsEntity
}
