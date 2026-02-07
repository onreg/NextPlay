package io.github.onreg.data.details.impl.mapper.impl

import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.network.rawg.dto.GameDetailsDto
import io.github.onreg.data.details.impl.mapper.GameDetailsDtoMapper
import javax.inject.Inject

public class GameDetailsDtoMapperImpl
    @Inject
    constructor() : GameDetailsDtoMapper {
        override fun map(dto: GameDetailsDto): GameDetailsEntity {
            val platforms =
                if (dto.parentPlatforms.isNotEmpty()) {
                    dto.parentPlatforms
                } else {
                    dto.platforms
                }
            val platformIds = platforms.mapNotNull { it.platform?.id }.distinct().joinToString(",")
            val developers = dto.developers
                .mapNotNull {
                    it.name?.trim()
                }.filter { it.isNotEmpty() }

            return GameDetailsEntity(
                gameId = dto.id,
                title = dto.title.orEmpty(),
                imageUrl = dto.imageUrl.orEmpty(),
                releaseDate = dto.releaseDate,
                platformIds = platformIds,
                website = dto.website,
                rating = dto.rating ?: 0.0,
                description = dto.description.orEmpty(),
                developers = developers.joinToString("|"),
            )
        }
    }
