package io.github.onreg.data.game.impl.mapper

import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.data.game.api.Game

public interface GameDtoMapper {
    public fun map(model: GameDto): Game
    public fun map(model: Game): GameDto
}

public class GameDtoMapperImpl : GameDtoMapper {
    override fun map(model: GameDto): Game = Game(
        id = model.id,
        name = model.name,
        backgroundImage = model.backgroundImage,
        released = model.released,
        rating = model.rating,
        platforms = emptyList()
    )

    override fun map(model: Game): GameDto = GameDto(
        id = model.id,
        name = model.name,
        backgroundImage = model.backgroundImage,
        released = model.released,
        rating = model.rating,
        platforms = emptyList()
    )
}