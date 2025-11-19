package io.github.onreg.data.game.impl.mapper

import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.data.game.api.Game

public interface GameEntityMapper {
    public fun map(model: Game): GameEntity
    public fun map(model: GameEntity): Game
}

public class GameEntityMapperImpl : GameEntityMapper {
    override fun map(model: Game): GameEntity = GameEntity(
        id = model.id,
        name = model.name,
        backgroundImage = model.backgroundImage,
        released = model.released,
        rating = model.rating,
        platforms = model.platforms
    )

    override fun map(model: GameEntity): Game = Game(
        id = model.id,
        name = model.name,
        rating = model.rating,
        backgroundImage = model.backgroundImage,
        released = model.released,
        platforms = model.platforms,
    )
}