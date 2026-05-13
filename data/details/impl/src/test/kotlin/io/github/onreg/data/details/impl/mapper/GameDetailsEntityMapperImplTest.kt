package io.github.onreg.data.details.impl.mapper

import io.github.onreg.core.db.company.entity.GameCompanyEntity
import io.github.onreg.core.db.company.entity.GameCompanyRoleEntity
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.details.model.GameDetailsWithPlatformsAndCompanies
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.data.details.api.model.GameCompany
import io.github.onreg.data.details.api.model.GameCompanyRole
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.game.api.model.GamePlatform
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GameDetailsEntityMapperImplTest {
    private val mapper = GameDetailsEntityMapperImpl()

    @Test
    fun `map copies fields maps roles and filters unsupported platforms`() {
        val releaseDate = Instant.parse("2020-06-01T00:00:00Z")
        val model = GameDetailsWithPlatformsAndCompanies(
            details = GameDetailsEntity(
                gameId = 42,
                title = "Game",
                imageUrl = "https://image",
                releaseDate = releaseDate,
                website = "https://site",
                rating = 4.5,
                description = "Description",
            ),
            platforms = listOf(
                PlatformEntity(GamePlatform.PC.id),
                PlatformEntity(999),
                PlatformEntity(GamePlatform.PC.id),
                PlatformEntity(GamePlatform.IOS.id),
            ),
            companies = listOf(
                GameCompanyEntity(
                    id = "Developer\u001FDev",
                    name = "Dev",
                    logoUrl = "https://dev-logo",
                    role = GameCompanyRoleEntity.Developer,
                ),
                GameCompanyEntity(
                    id = "Publisher\u001FPub",
                    name = "Pub",
                    logoUrl = null,
                    role = GameCompanyRoleEntity.Publisher,
                ),
            ),
        )

        val result = mapper.map(model)

        assertEquals(
            GameDetails(
                gameId = 42,
                title = "Game",
                imageUrl = "https://image",
                releaseDate = releaseDate,
                platforms = setOf(GamePlatform.PC, GamePlatform.IOS),
                website = "https://site",
                rating = 4.5,
                description = "Description",
                companies = listOf(
                    GameCompany(
                        name = "Dev",
                        logoUrl = "https://dev-logo",
                        role = GameCompanyRole.Developer,
                    ),
                    GameCompany(
                        name = "Pub",
                        logoUrl = null,
                        role = GameCompanyRole.Publisher,
                    ),
                ),
            ),
            result,
        )
    }
}
