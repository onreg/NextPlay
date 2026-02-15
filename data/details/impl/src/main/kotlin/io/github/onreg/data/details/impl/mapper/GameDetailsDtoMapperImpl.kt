package io.github.onreg.data.details.impl.mapper

import io.github.onreg.core.db.company.entity.GameCompanyEntity
import io.github.onreg.core.db.company.entity.GameCompanyRoleEntity
import io.github.onreg.core.db.details.entity.GameDetailsCompanyCrossRef
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.details.entity.GameDetailsPlatformCrossRef
import io.github.onreg.core.db.details.model.GameDetailsInsertionBundle
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.network.rawg.dto.GameCompanyDto
import io.github.onreg.core.network.rawg.dto.GameDetailsDto
import javax.inject.Inject

private const val COMPANY_ID_SEPARATOR: String = "\u001F"

public interface GameDetailsDtoMapper {
    public fun map(dto: GameDetailsDto): GameDetailsInsertionBundle
}

public class GameDetailsDtoMapperImpl
    @Inject
    constructor() : GameDetailsDtoMapper {
        override fun map(dto: GameDetailsDto): GameDetailsInsertionBundle {
            val details = GameDetailsEntity(
                gameId = dto.id,
                title = dto.title.orEmpty(),
                imageUrl = dto.imageUrl.orEmpty(),
                releaseDate = dto.releaseDate,
                website = dto.website,
                rating = dto.rating ?: 0.0,
                description = dto.description.orEmpty(),
            )
            val platforms = dto.parentPlatforms.takeIf { it.isNotEmpty() } ?: dto.platforms
            val platformIds = platforms.mapNotNull { it.platform?.id }.distinct()
            val platformEntities = platformIds.map(::PlatformEntity)
            val platformCrossRefs = platformIds.map { platformId ->
                GameDetailsPlatformCrossRef(gameId = dto.id, platformId = platformId)
            }

            val companies = linkedMapOf<String, GameCompanyEntity>()
            val companyCrossRefs = linkedSetOf<GameDetailsCompanyCrossRef>()
            addCompanies(
                gameId = dto.id,
                role = GameCompanyRoleEntity.Developer,
                rawCompanies = dto.developers,
                companies = companies,
                companyCrossRefs = companyCrossRefs,
            )
            addCompanies(
                gameId = dto.id,
                role = GameCompanyRoleEntity.Publisher,
                rawCompanies = dto.publishers,
                companies = companies,
                companyCrossRefs = companyCrossRefs,
            )

            return GameDetailsInsertionBundle(
                details = details,
                platforms = platformEntities,
                platformCrossRefs = platformCrossRefs,
                companies = companies.values.toList(),
                companyCrossRefs = companyCrossRefs.toList(),
            )
        }

        private fun companyId(
            role: GameCompanyRoleEntity,
            name: String,
        ): String = "${role.name}$COMPANY_ID_SEPARATOR$name"

        private fun addCompanies(
            gameId: Int,
            role: GameCompanyRoleEntity,
            rawCompanies: List<GameCompanyDto>,
            companies: MutableMap<String, GameCompanyEntity>,
            companyCrossRefs: MutableSet<GameDetailsCompanyCrossRef>,
        ) {
            rawCompanies.forEach { rawCompany ->
                val name = rawCompany.name?.trim().orEmpty()
                if (name.isEmpty()) {
                    return@forEach
                }

                val companyId = companyId(role, name)
                companies[companyId] = GameCompanyEntity(
                    id = companyId,
                    name = name,
                    logoUrl = rawCompany.logoUrl?.trim()?.takeIf { it.isNotEmpty() },
                    role = role,
                )
                companyCrossRefs += GameDetailsCompanyCrossRef(
                    gameId = gameId,
                    companyId = companyId,
                )
            }
        }
    }
