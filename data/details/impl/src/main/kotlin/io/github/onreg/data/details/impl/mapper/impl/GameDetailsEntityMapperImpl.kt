package io.github.onreg.data.details.impl.mapper.impl

import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.details.impl.mapper.GameDetailsEntityMapper
import io.github.onreg.data.game.list.api.model.GamePlatform
import javax.inject.Inject

public class GameDetailsEntityMapperImpl
    @Inject
    constructor() : GameDetailsEntityMapper {
        override fun map(entity: GameDetailsEntity): GameDetails {
            val platforms = entity.platformIds
                .split(',')
                .mapNotNull { it.toIntOrNull() }
                .mapNotNull(GamePlatform::fromId)
                .toSet()

            val developers = entity.developers
                .split('|')
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            return GameDetails(
                gameId = entity.gameId,
                title = entity.title,
                imageUrl = entity.imageUrl,
                releaseDate = entity.releaseDate,
                platforms = platforms,
                website = entity.website,
                rating = entity.rating,
                description = entity.description,
                developers = developers,
            )
        }
    }
