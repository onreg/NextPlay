package io.github.onreg.data.details.impl.mapper

import io.github.onreg.core.db.details.entity.DeveloperEntity
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.details.model.GameDetailsWithDevelopers
import io.github.onreg.data.details.api.model.Developer
import io.github.onreg.data.details.api.model.GameDetails
import javax.inject.Inject

public interface GameDetailsEntityMapper {
    public fun mapToEntity(details: GameDetails): GameDetailsEntity

    public fun mapToDevelopers(details: GameDetails): List<DeveloperEntity>

    public fun map(model: GameDetailsWithDevelopers): GameDetails
}

public class GameDetailsEntityMapperImpl
    @Inject
    constructor() : GameDetailsEntityMapper {
        override fun mapToEntity(details: GameDetails): GameDetailsEntity = GameDetailsEntity(
            gameId = details.id,
            name = details.name,
            bannerImageUrl = details.bannerImageUrl,
            releaseDate = details.releaseDate,
            websiteUrl = details.websiteUrl,
            rating = details.rating,
            descriptionHtml = details.descriptionHtml,
        )

        override fun mapToDevelopers(details: GameDetails): List<DeveloperEntity> =
            details.developers.map { developer ->
                DeveloperEntity(
                    id = developer.id,
                    name = developer.name,
                )
            }

        override fun map(model: GameDetailsWithDevelopers): GameDetails = GameDetails(
            id = model.details.gameId,
            name = model.details.name,
            bannerImageUrl = model.details.bannerImageUrl,
            releaseDate = model.details.releaseDate,
            websiteUrl = model.details.websiteUrl,
            rating = model.details.rating,
            descriptionHtml = model.details.descriptionHtml,
            developers = model.developers.map { developer ->
                Developer(
                    id = developer.id,
                    name = developer.name,
                )
            },
        )
    }
