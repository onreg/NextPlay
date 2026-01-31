package io.github.onreg.data.game.impl.mapper

import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import javax.inject.Inject

public interface GameDtoMapper {
    public fun map(model: GameDto): Game
}

public class GameDtoMapperImpl
    @Inject
    constructor() : GameDtoMapper {
        override fun map(model: GameDto): Game = Game(
            id = model.id,
            title = model.title.orEmpty(),
            imageUrl = model.imageUrl.orEmpty(),
            releaseDate = model.releaseDate,
            rating = model.rating ?: 0.0,
            platforms = model.platforms
                .mapNotNull { wrapper ->
                    wrapper.platform?.id?.let { GamePlatform.fromId(it) }
                }.toSet(),
        )
    }
