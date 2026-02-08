package io.github.onreg.data.details.impl

import io.github.onreg.core.db.details.dao.GameDetailsDao
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.network.rawg.api.GameDetailsApi
import io.github.onreg.core.network.rawg.dto.GameDetailsDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.details.api.model.GameCompany
import io.github.onreg.data.details.api.model.GameCompanyRole
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.details.impl.mapper.GameDetailsDtoMapper
import io.github.onreg.data.details.impl.mapper.GameDetailsEntityMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class GameDetailsRepositoryImplTest {
    private val gameDetailsApi: GameDetailsApi = mock()
    private val gameDetailsDao: GameDetailsDao = mock()
    private val gameDetailsDtoMapper: GameDetailsDtoMapper = mock()
    private val gameDetailsEntityMapper: GameDetailsEntityMapper = mock()

    private val repository = GameDetailsRepositoryImpl(
        gameDetailsApi = gameDetailsApi,
        gameDetailsDao = gameDetailsDao,
        gameDetailsDtoMapper = gameDetailsDtoMapper,
        gameDetailsEntityMapper = gameDetailsEntityMapper,
    )

    @Test
    fun `observeGameDetails maps cached entity`() = runTest {
        val entity = GameDetailsEntity(
            gameId = 1,
            title = "Game",
            imageUrl = "https://img",
            releaseDate = null,
            platformIds = "4",
            website = "https://example.com",
            rating = 4.0,
            description = "desc",
            developers = "Dev\u001Fhttps://dev-logo",
            publishers = "Pub\u001F",
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
        gameDetailsDao.stub { on { observe(1) } doReturn MutableStateFlow(entity) }
        gameDetailsEntityMapper.stub { on { map(entity) } doReturn expected }

        val result = repository.observeGameDetails(1).first()

        assertEquals(expected, result)
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
        val entity = GameDetailsEntity(
            gameId = 1,
            title = "Game",
            imageUrl = "https://img",
            releaseDate = null,
            platformIds = "4",
            website = "https://example.com",
            rating = 4.0,
            description = "desc",
            developers = "",
            publishers = "",
        )
        gameDetailsApi.stub {
            onBlocking {
                getGameDetails(
                    1,
                )
            } doReturn NetworkResponse.Success(dto)
        }
        gameDetailsDtoMapper.stub { on { map(dto) } doReturn entity }

        repository.refreshGameDetails(1)

        verify(gameDetailsDao).upsert(entity)
    }

    @Test
    fun `refreshGameDetails propagates failure exception`() = runTest {
        gameDetailsApi.stub {
            onBlocking { getGameDetails(1) } doReturn
                NetworkResponse.Failure.NetworkError(IOException("boom"))
        }

        assertFailsWith<IOException> {
            repository.refreshGameDetails(1)
        }
    }
}
