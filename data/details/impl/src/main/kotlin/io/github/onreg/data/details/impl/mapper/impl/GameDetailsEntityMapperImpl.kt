package io.github.onreg.data.details.impl.mapper.impl

import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.data.details.api.model.GameCompany
import io.github.onreg.data.details.api.model.GameCompanyRole
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.details.impl.mapper.GameDetailsEntityMapper
import io.github.onreg.data.game.list.api.model.GamePlatform
import javax.inject.Inject

private const val FIELD_SEPARATOR: String = "\u001F"
private const val ITEM_SEPARATOR: String = "\u001E"

public class GameDetailsEntityMapperImpl
    @Inject
    constructor() : GameDetailsEntityMapper {
        override fun map(entity: GameDetailsEntity): GameDetails {
            val platforms = entity.platformIds
                .split(',')
                .mapNotNull { it.toIntOrNull() }
                .mapNotNull(GamePlatform::fromId)
                .toSet()

            val developers = decodeCompanies(entity.developers, GameCompanyRole.Developer)
            val publishers = decodeCompanies(entity.publishers, GameCompanyRole.Publisher)

            return GameDetails(
                gameId = entity.gameId,
                title = entity.title,
                imageUrl = entity.imageUrl,
                releaseDate = entity.releaseDate,
                platforms = platforms,
                website = entity.website,
                rating = entity.rating,
                description = entity.description,
                companies = developers + publishers,
            )
        }

        private fun decodeCompanies(
            source: String,
            role: GameCompanyRole,
        ): List<GameCompany> = source
            .split(ITEM_SEPARATOR)
            .mapNotNull { item ->
                val values = item.split(FIELD_SEPARATOR, limit = 2)
                val name = values.getOrNull(0)?.trim().orEmpty()
                if (name.isBlank()) {
                    null
                } else {
                    GameCompany(
                        name = name,
                        logoUrl = values.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() },
                        role = role,
                    )
                }
            }
    }
