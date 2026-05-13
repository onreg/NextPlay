package io.github.onreg.data.details.impl.mapper

import io.github.onreg.core.db.company.entity.GameCompanyRoleEntity
import io.github.onreg.core.db.details.model.GameDetailsWithPlatformsAndCompanies
import io.github.onreg.data.details.api.model.GameCompany
import io.github.onreg.data.details.api.model.GameCompanyRole
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.game.api.model.GamePlatform
import javax.inject.Inject

public interface GameDetailsEntityMapper {
    public fun map(model: GameDetailsWithPlatformsAndCompanies): GameDetails
}

public class GameDetailsEntityMapperImpl
    @Inject
    constructor() : GameDetailsEntityMapper {
        override fun map(model: GameDetailsWithPlatformsAndCompanies): GameDetails = GameDetails(
            gameId = model.details.gameId,
            title = model.details.title,
            imageUrl = model.details.imageUrl,
            releaseDate = model.details.releaseDate,
            platforms = model.platforms
                .mapNotNull { platform -> GamePlatform.fromId(platform.id) }
                .toSet(),
            website = model.details.website,
            rating = model.details.rating,
            description = model.details.description,
            companies = model.companies.map { company ->
                GameCompany(
                    name = company.name,
                    logoUrl = company.logoUrl,
                    role = mapRole(company.role),
                )
            },
        )

        private fun mapRole(role: GameCompanyRoleEntity): GameCompanyRole = when (role) {
            GameCompanyRoleEntity.Developer -> GameCompanyRole.Developer
            GameCompanyRoleEntity.Publisher -> GameCompanyRole.Publisher
        }
    }
