package io.github.onreg.data.details.impl.mapper.impl

import io.github.onreg.core.db.company.entity.GameCompanyEntity
import io.github.onreg.core.db.company.entity.GameCompanyRoleEntity
import io.github.onreg.core.db.details.entity.GameDetailsCompanyCrossRef
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.details.entity.GameDetailsPlatformCrossRef
import io.github.onreg.core.db.details.model.GameDetailsInsertionBundle
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.network.rawg.dto.GameCompanyDto
import io.github.onreg.core.network.rawg.dto.GameDetailsDto
import io.github.onreg.core.network.rawg.dto.PlatformDto
import io.github.onreg.core.network.rawg.dto.PlatformWrapperDto
import io.github.onreg.data.details.impl.mapper.GameDetailsDtoMapperImpl
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GameDetailsDtoMapperImplTest {
    private val mapper = GameDetailsDtoMapperImpl()

    @Test
    fun `map uses parent platforms and deduplicates companies by role and name`() {
        val result = mapper.map(parentPlatformsDto())

        assertParentPlatformsResult(result)
    }

    @Test
    fun `map falls back to regular platforms when parent platforms are empty`() {
        val result = mapper.map(
            GameDetailsDto(
                id = 8,
                title = "Game",
                imageUrl = "https://image.dev",
                releaseDate = null,
                platforms = listOf(
                    PlatformWrapperDto(platform = PlatformDto(2)),
                    PlatformWrapperDto(platform = null),
                    PlatformWrapperDto(platform = PlatformDto(3)),
                    PlatformWrapperDto(platform = PlatformDto(2)),
                ),
                parentPlatforms = emptyList(),
                website = null,
                rating = 4.5,
                description = "Description",
                developers = emptyList(),
                publishers = emptyList(),
            ),
        )

        assertEquals(listOf(PlatformEntity(2), PlatformEntity(3)), result.platforms)
        assertEquals(
            listOf(
                GameDetailsPlatformCrossRef(gameId = 8, platformId = 2),
                GameDetailsPlatformCrossRef(gameId = 8, platformId = 3),
            ),
            result.platformCrossRefs,
        )
    }

    private fun parentPlatformsDto(): GameDetailsDto = GameDetailsDto(
        id = 101,
        title = "  ",
        imageUrl = null,
        releaseDate = null,
        platforms = listOf(
            PlatformWrapperDto(platform = PlatformDto(1)),
            PlatformWrapperDto(platform = PlatformDto(2)),
        ),
        parentPlatforms = listOf(
            PlatformWrapperDto(platform = PlatformDto(10)),
            PlatformWrapperDto(platform = PlatformDto(10)),
            PlatformWrapperDto(platform = PlatformDto(11)),
        ),
        website = "https://game.dev",
        rating = null,
        description = null,
        developers = listOf(
            GameCompanyDto(name = " Studio ", logoUrl = " "),
            GameCompanyDto(name = "Studio", logoUrl = "https://studio.dev/logo.png"),
            GameCompanyDto(name = "", logoUrl = "https://ignored.dev/logo.png"),
        ),
        publishers = listOf(
            GameCompanyDto(name = " Pub ", logoUrl = "https://pub.dev/logo.png"),
            GameCompanyDto(name = "Pub", logoUrl = ""),
        ),
    )

    private fun assertParentPlatformsResult(result: GameDetailsInsertionBundle) {
        assertEquals(
            GameDetailsEntity(
                gameId = 101,
                title = "  ",
                imageUrl = "",
                releaseDate = null,
                website = "https://game.dev",
                rating = 0.0,
                description = "",
            ),
            result.details,
        )
        assertEquals(listOf(PlatformEntity(10), PlatformEntity(11)), result.platforms)
        assertEquals(
            listOf(
                GameDetailsPlatformCrossRef(gameId = 101, platformId = 10),
                GameDetailsPlatformCrossRef(gameId = 101, platformId = 11),
            ),
            result.platformCrossRefs,
        )
        assertEquals(
            listOf(
                GameCompanyEntity(
                    id = "Developer\u001FStudio",
                    name = "Studio",
                    logoUrl = "https://studio.dev/logo.png",
                    role = GameCompanyRoleEntity.Developer,
                ),
                GameCompanyEntity(
                    id = "Publisher\u001FPub",
                    name = "Pub",
                    logoUrl = null,
                    role = GameCompanyRoleEntity.Publisher,
                ),
            ),
            result.companies,
        )
        assertEquals(
            listOf(
                GameDetailsCompanyCrossRef(gameId = 101, companyId = "Developer\u001FStudio"),
                GameDetailsCompanyCrossRef(gameId = 101, companyId = "Publisher\u001FPub"),
            ),
            result.companyCrossRefs,
        )
    }
}
