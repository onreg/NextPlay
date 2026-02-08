package io.github.onreg.feature.game.details.impl.ui.mapper

import io.github.onreg.data.details.api.model.GameCompany
import io.github.onreg.data.details.api.model.GameCompanyRole
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.game.list.api.model.GamePlatform
import io.github.onreg.feature.game.details.impl.ui.model.GameCompanyRoleUi
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import io.github.onreg.ui.platform.model.PlatformUI
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GameDetailsUiMapperTest {
    private val mapper = GameDetailsUiMapperImpl(
        platformUiMapper = object : PlatformUiMapper {
            override fun mapName(model: Set<GamePlatform>): Set<String> = emptySet()

            override fun mapPlatform(model: Set<GamePlatform>): Set<PlatformUI> = emptySet()
        },
    )

    @Test
    fun `should format rating and expose valid website`() {
        val model = GameDetails(
            gameId = 1,
            title = "Game",
            imageUrl = "https://img",
            releaseDate = Instant.parse("2024-01-05T00:00:00Z"),
            platforms = setOf(GamePlatform.PC),
            website = "https://example.com",
            rating = 4.456,
            description = "desc",
            companies = emptyList(),
        )

        val result = mapper.map(model)

        assertEquals("4.5", result.ratingChip.text)
        assertTrue(result.isWebsiteVisible)
    }

    @Test
    fun `should hide invalid website`() {
        val model = GameDetails(
            gameId = 1,
            title = "Game",
            imageUrl = "https://img",
            releaseDate = null,
            platforms = emptySet(),
            website = "ftp://example.com",
            rating = 1.0,
            description = "desc",
            companies = emptyList(),
        )

        val result = mapper.map(model)

        assertFalse(result.isWebsiteVisible)
    }

    @Test
    fun `should map html description to plain text`() {
        val model = GameDetails(
            gameId = 1,
            title = "Game",
            imageUrl = "https://img",
            releaseDate = null,
            platforms = emptySet(),
            website = null,
            rating = 1.0,
            description = "<p>Portal&#39;s world &amp; puzzles</p><p>Test&nbsp;text</p>",
            companies = emptyList(),
        )

        val result = mapper.map(model)

        assertEquals("Portal's world & puzzles Test text", result.description)
    }

    @Test
    fun `should map companies with developer first order and role labels`() {
        val model = GameDetails(
            gameId = 1,
            title = "Game",
            imageUrl = "https://img",
            releaseDate = null,
            platforms = emptySet(),
            website = null,
            rating = 1.0,
            description = "desc",
            companies = listOf(
                GameCompany(
                    name = "Publisher One",
                    logoUrl = "https://pub",
                    role = GameCompanyRole.Publisher,
                ),
                GameCompany(
                    name = "Developer One",
                    logoUrl = null,
                    role = GameCompanyRole.Developer,
                ),
            ),
        )

        val result = mapper.map(model)

        assertEquals("Developer One", result.companies.first().name)
        assertEquals(GameCompanyRoleUi.Developer, result.companies.first().role)
        assertEquals("Publisher One", result.companies.last().name)
        assertEquals(GameCompanyRoleUi.Publisher, result.companies.last().role)
    }
}
