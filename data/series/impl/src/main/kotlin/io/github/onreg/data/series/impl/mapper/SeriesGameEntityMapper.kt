package io.github.onreg.data.series.impl.mapper

import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.db.series.entity.SeriesGameEntity
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import javax.inject.Inject

public data class SeriesInsertionBundle(
    val games: List<GameEntity>,
    val platforms: List<PlatformEntity>,
    val crossRefs: List<GamePlatformCrossRef>,
    val seriesEntities: List<SeriesGameEntity>,
)

public interface SeriesGameEntityMapper {
    public fun map(
        models: List<Game>,
        parentGameId: Int,
        startOrder: Long,
    ): SeriesInsertionBundle
}

public class SeriesGameEntityMapperImpl
    @Inject
    constructor() : SeriesGameEntityMapper {
        override fun map(
            models: List<Game>,
            parentGameId: Int,
            startOrder: Long,
        ): SeriesInsertionBundle {
            val games = models.map { model ->
                GameEntity(
                    id = model.id,
                    title = model.title,
                    imageUrl = model.imageUrl,
                    releaseDate = model.releaseDate,
                    rating = model.rating,
                )
            }
            val seriesEntities = models.mapIndexed { index, model ->
                SeriesGameEntity(
                    parentGameId = parentGameId,
                    gameId = model.id,
                    insertionOrder = startOrder + index,
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

            return SeriesInsertionBundle(
                games = games,
                platforms = platforms,
                crossRefs = crossRefs,
                seriesEntities = seriesEntities,
            )
        }
    }
