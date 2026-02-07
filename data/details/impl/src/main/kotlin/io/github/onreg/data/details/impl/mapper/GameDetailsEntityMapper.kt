package io.github.onreg.data.details.impl.mapper

import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.data.details.api.model.GameDetails

public interface GameDetailsEntityMapper {
    public fun map(entity: GameDetailsEntity): GameDetails
}
