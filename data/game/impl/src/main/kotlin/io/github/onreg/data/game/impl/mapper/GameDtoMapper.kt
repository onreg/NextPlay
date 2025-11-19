package io.github.onreg.data.game.impl.mapper

import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.data.game.api.Game

public interface GameDtoMapper {
    public fun map(model: GameDto): Game
}

public class GameDtoMapperImpl : GameDtoMapper {
    override fun map(model: GameDto): Game = Game(
        id = model.id,
        name = model.name.orEmpty(),
        backgroundImage = model.backgroundImage.orEmpty(),
        released = model.released.orEmpty(),
        rating = model.rating ?: 0.0,
        platforms = emptyList()
    )
}