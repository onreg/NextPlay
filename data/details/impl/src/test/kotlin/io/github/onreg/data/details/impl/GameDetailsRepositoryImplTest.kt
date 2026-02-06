package io.github.onreg.data.details.impl

import io.github.onreg.core.db.TransactionProvider
import io.github.onreg.core.db.details.dao.GameDetailsDao
import io.github.onreg.core.db.details.entity.DeveloperEntity
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.network.rawg.api.GameDetailsApi
import io.github.onreg.core.network.rawg.dto.GameDetailsDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.details.api.RefreshResult
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.details.impl.mapper.GameDetailsDtoMapper
import io.github.onreg.data.details.impl.mapper.GameDetailsEntityMapper
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GameDetailsRepositoryImplTest {
    private val api: GameDetailsApi = mock()
    private val dao: GameDetailsDao = mock()
    private val dtoMapper: GameDetailsDtoMapper = mock()
    private val entityMapper: GameDetailsEntityMapper = mock()
    private val transactionProvider = object : TransactionProvider {
        override suspend fun <T> run(block: suspend () -> T): T = block()
    }

    private val repository = GameDetailsRepositoryImpl(
        gameDetailsApi = api,
        gameDetailsDao = dao,
        dtoMapper = dtoMapper,
        entityMapper = entityMapper,
        transactionProvider = transactionProvider,
    )

    @Test
    fun `refresh should persist details on success`() = runTest {
        val dto = GameDetailsDto(
            id = 1,
            title = "Title",
            imageUrl = "image",
            bannerImageUrl = null,
            releaseDate = null,
            websiteUrl = null,
            rating = null,
            descriptionHtml = null,
            platforms = emptyList(),
            developers = emptyList(),
        )
        val details = GameDetails(
            id = 1,
            name = "Title",
            bannerImageUrl = "image",
            releaseDate = null,
            websiteUrl = null,
            rating = null,
            descriptionHtml = null,
            developers = emptyList(),
        )
        val entity = GameDetailsEntity(
            gameId = 1,
            name = "Title",
            bannerImageUrl = "image",
            releaseDate = null,
            websiteUrl = null,
            rating = null,
            descriptionHtml = null,
        )
        val developers = emptyList<DeveloperEntity>()

        api.stub {
            onBlocking { getGameDetails(1) } doReturn NetworkResponse.Success(dto)
        }
        dtoMapper.stub { on { map(dto) } doReturn details }
        entityMapper.stub { on { mapToEntity(details) } doReturn entity }
        entityMapper.stub { on { mapToDevelopers(details) } doReturn developers }

        val result = repository.refreshGameDetails(1)

        assertEquals(RefreshResult.Success, result)
        verify(dao).upsertDetails(entity)
        verify(dao).replaceDevelopers(1, developers)
    }

    @Test
    fun `refresh should return failure on error`() = runTest {
        api.stub {
            onBlocking { getGameDetails(1) } doReturn
                NetworkResponse.Failure.NetworkError(IOException("boom"))
        }

        val result = repository.refreshGameDetails(1)

        assertTrue(result is RefreshResult.Failure)
    }
}
