package io.github.onreg.data.details.impl.mapper

import io.github.onreg.core.network.rawg.dto.GameDetailsDto
import io.github.onreg.data.details.api.model.Developer
import io.github.onreg.data.details.api.model.GameDetails
import javax.inject.Inject

public interface GameDetailsDtoMapper {
    public fun map(dto: GameDetailsDto): GameDetails
}

public class GameDetailsDtoMapperImpl
    @Inject
    constructor() : GameDetailsDtoMapper {
        override fun map(dto: GameDetailsDto): GameDetails = GameDetails(
            id = dto.id,
            name = dto.title.orEmpty(),
            bannerImageUrl = dto.bannerImageUrl ?: dto.imageUrl,
            releaseDate = dto.releaseDate,
            websiteUrl = dto.websiteUrl,
            rating = dto.rating,
            descriptionHtml = dto.descriptionHtml,
            developers = dto.developers.map { developer ->
                Developer(
                    id = developer.id,
                    name = developer.name.orEmpty(),
                )
            },
        )
    }
