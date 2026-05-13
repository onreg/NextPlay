package io.github.onreg.data.details.impl

import io.github.onreg.core.db.company.entity.GameCompanyEntity
import io.github.onreg.core.db.company.entity.GameCompanyRoleEntity
import io.github.onreg.core.db.details.entity.GameDetailsCompanyCrossRef
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.details.entity.GameDetailsPlatformCrossRef
import io.github.onreg.core.db.details.model.GameDetailsInsertionBundle
import io.github.onreg.core.db.details.model.GameDetailsWithPlatformsAndCompanies
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.network.rawg.dto.GameDetailsDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.details.api.model.GameCompany
import io.github.onreg.data.details.api.model.GameCompanyRole
import io.github.onreg.data.details.api.model.GameDetails
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class GameDetailsRepositoryImplTest {
    @Test
    fun `observeGameDetails maps cached entity`() = runTest {
        val model = GameDetailsWithPlatformsAndCompanies(
            details = GameDetailsEntity(
                gameId = 1,
                title = "Game",
                imageUrl = "https://img",
                releaseDate = null,
                website = "https://example.com",
                rating = 4.0,
                description = "desc",
            ),
            platforms = listOf(PlatformEntity(4)),
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
        val expected = GameDetails(
            gameId = 1,
            title = "Game",
            imageUrl = "https://img",
            releaseDate = null,
            platforms = emptySet(),
            website = "https://example.com",
            rating = 4.0,
            description = "desc",
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
        )
        val driver = GameDetailsRepositoryTestDriver
            .Builder()
            .daoObserveGame(gameId = 1, model = model)
            .entityMapperMap(model = model, gameDetails = expected)
            .build()

        val result = driver.observeGameDetails(1).first()

        assertEquals(expected, result)
        verify(driver.gameDetailsDao).observeGame(1)
        verify(driver.gameDetailsEntityMapper).map(model)
    }

    @Test
    fun `observeGameDetails returns null when cache is empty`() = runTest {
        val driver = GameDetailsRepositoryTestDriver
            .Builder()
            .daoObserveGame(gameId = 1, model = null)
            .build()

        val result = driver.observeGameDetails(1).first()

        assertEquals(null, result)
        verifyNoInteractions(driver.gameDetailsEntityMapper)
    }

    @Test
    fun `refreshGameDetails stores mapped dto on success`() = runTest {
        val dto = GameDetailsDto(
            id = 1,
            title = "Game",
            imageUrl = "https://img",
            releaseDate = null,
            platforms = emptyList(),
            parentPlatforms = emptyList(),
            website = "https://example.com",
            rating = 4.0,
            description = "desc",
            developers = emptyList(),
            publishers = emptyList(),
        )
        val insertionBundle = GameDetailsInsertionBundle(
            details = GameDetailsEntity(
                gameId = 1,
                title = "Game",
                imageUrl = "https://img",
                releaseDate = null,
                website = "https://example.com",
                rating = 4.0,
                description = "desc",
            ),
            platforms = listOf(PlatformEntity(4)),
            platformCrossRefs = listOf(GameDetailsPlatformCrossRef(gameId = 1, platformId = 4)),
            companies = listOf(
                GameCompanyEntity(
                    id = "Developer\u001FDev",
                    name = "Dev",
                    logoUrl = "https://dev-logo",
                    role = GameCompanyRoleEntity.Developer,
                ),
            ),
            companyCrossRefs = listOf(
                GameDetailsCompanyCrossRef(gameId = 1, companyId = "Developer\u001FDev"),
            ),
        )
        val driver = GameDetailsRepositoryTestDriver
            .Builder()
            .apiGetGameDetails(gameId = 1, response = NetworkResponse.Success(dto))
            .dtoMapperMap(dto = dto, insertionBundle = insertionBundle)
            .build()

        val result = driver.refreshGameDetails(1)

        assertTrue(result.isSuccess)
        verify(driver.gameDetailsDtoMapper).map(dto)
        verify(driver.gameDetailsDao).insertGameDetails(insertionBundle)
    }

    @Test
    fun `refreshGameDetails returns api failure exception`() = runTest {
        val error = IOException("boom")
        val driver = GameDetailsRepositoryTestDriver
            .Builder()
            .apiGetGameDetails(gameId = 1, response = NetworkResponse.Failure.NetworkError(error))
            .build()

        val result = driver.refreshGameDetails(1)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        verify(driver.gameDetailsDao, never()).insertGameDetails(any())
    }

    @Test
    fun `refreshGameDetails returns fallback exception for missing failure cause`() = runTest {
        val driver = GameDetailsRepositoryTestDriver
            .Builder()
            .apiGetGameDetails(gameId = 1, response = NetworkResponse.Failure.OtherError(null))
            .build()

        val result = driver.refreshGameDetails(1)

        assertTrue(result.isFailure)
        val exception = assertIs<IllegalStateException>(result.exceptionOrNull())
        assertEquals("Game details request failed", exception.message)
        verify(driver.gameDetailsDao, never()).insertGameDetails(any())
    }
}
