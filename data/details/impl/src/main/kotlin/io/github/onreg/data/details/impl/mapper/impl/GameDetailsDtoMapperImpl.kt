package io.github.onreg.data.details.impl.mapper.impl

import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.network.rawg.dto.GameDetailsDto
import io.github.onreg.data.details.impl.mapper.GameDetailsDtoMapper
import javax.inject.Inject

private const val FIELD_SEPARATOR: String = "\u001F"
private const val ITEM_SEPARATOR: String = "\u001E"

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
                .mapNotNull { company ->
                    company.name?.trim()?.takeIf { it.isNotEmpty() }?.let { name ->
                        name to company.logoUrl?.trim()?.takeIf { it.isNotEmpty() }
                    }
                }
            val publishers = dto.publishers
                .mapNotNull { company ->
                    company.name?.trim()?.takeIf { it.isNotEmpty() }?.let { name ->
                        name to company.logoUrl?.trim()?.takeIf { it.isNotEmpty() }
                    }
                }

            return GameDetailsEntity(
                gameId = dto.id,
                title = dto.title.orEmpty(),
                imageUrl = dto.imageUrl.orEmpty(),
                releaseDate = dto.releaseDate,
                platformIds = platformIds,
                website = dto.website,
                rating = dto.rating ?: 0.0,
                description = dto.description.orEmpty(),
                developers = encodeCompanies(developers),
                publishers = encodeCompanies(publishers),
            )
        }

        private fun encodeCompanies(companies: List<Pair<String, String?>>): String = companies
            .joinToString(ITEM_SEPARATOR) { (name, logoUrl) ->
                "$name$FIELD_SEPARATOR${logoUrl.orEmpty()}"
            }
    }
