package io.github.onreg.data.game.impl.mapper

import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.list.entity.GameListEntity
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.db.series.entity.SeriesEntity
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import javax.inject.Inject

public interface GameEntityMapper {
    public fun map(models: List<Game>): GameInsertionBundle

    public fun mapGameListEntries(
        models: List<Game>,
        startPosition: Long,
    ): List<GameListEntity>

    public fun mapSeriesEntries(
        models: List<Game>,
        startPosition: Long,
    ): List<SeriesEntity>

    public fun map(model: GameWithPlatforms): Game
}

public class GameEntityMapperImpl
    @Inject
    constructor() : GameEntityMapper {
        override fun map(models: List<Game>): GameInsertionBundle {
            val games = models.map { model ->
                GameEntity(
                    id = model.id,
                    title = model.title,
                    imageUrl = model.imageUrl,
                    releaseDate = model.releaseDate,
                    rating = model.rating,
                )
            }
            val platforms = models
                .flatMap(Game::platforms)
                .distinctBy(GamePlatform::id)
                .map { platform -> PlatformEntity(id = platform.id) }

            val crossRefs = models
                .flatMap { model ->
                    model.platforms.map { platform ->
                        GamePlatformCrossRef(
                            gameId = model.id,
                            platformId = platform.id,
                        )
                    }
                }.distinctBy { it.gameId to it.platformId }

            return GameInsertionBundle(
                games = games,
                platforms = platforms,
                crossRefs = crossRefs,
            )
        }

        override fun mapGameListEntries(
            models: List<Game>,
            startPosition: Long,
        ): List<GameListEntity> = models.mapIndexed { index, model ->
            GameListEntity(
                gameId = model.id,
                position = startPosition + index,
            )
        }

        override fun mapSeriesEntries(
            models: List<Game>,
            startPosition: Long,
        ): List<SeriesEntity> = models.mapIndexed { index, model ->
            SeriesEntity(
                gameId = model.id,
                position = startPosition + index,
            )
        }

        override fun map(model: GameWithPlatforms): Game = Game(
            id = model.game.id,
            title = model.game.title,
            imageUrl = model.game.imageUrl,
            releaseDate = model.game.releaseDate,
            rating = model.game.rating,
            platforms = model.platforms
                .mapNotNull { platform ->
                    GamePlatform.fromId(platform.id)
                }.toSet(),
        )
    }
