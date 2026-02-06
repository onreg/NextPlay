package io.github.onreg.data.series.impl.mapper

import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import javax.inject.Inject

public interface SeriesGameMapper {
    public fun map(model: GameWithPlatforms): Game
}

public class SeriesGameMapperImpl
    @Inject
    constructor() : SeriesGameMapper {
        override fun map(model: GameWithPlatforms): Game = Game(
            id = model.game.id,
            title = model.game.title,
            imageUrl = model.game.imageUrl,
            releaseDate = model.game.releaseDate,
            rating = model.game.rating,
            platforms = model.platforms
                .mapNotNull { platform -> GamePlatform.fromId(platform.id) }
                .toSet(),
        )
    }
