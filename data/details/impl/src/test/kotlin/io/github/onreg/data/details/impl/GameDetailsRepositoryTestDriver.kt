package io.github.onreg.data.details.impl

import io.github.onreg.core.db.details.dao.GameDetailsDao
import io.github.onreg.core.db.details.model.GameDetailsInsertionBundle
import io.github.onreg.core.db.details.model.GameDetailsWithPlatformsAndCompanies
import io.github.onreg.core.network.rawg.api.GameDetailsApi
import io.github.onreg.core.network.rawg.dto.GameDetailsDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.details.api.GameDetailsRepository
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.details.impl.mapper.GameDetailsDtoMapper
import io.github.onreg.data.details.impl.mapper.GameDetailsEntityMapper
import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

internal class GameDetailsRepositoryTestDriver private constructor(
    val gameDetailsApi: GameDetailsApi,
    val gameDetailsDao: GameDetailsDao,
    val gameDetailsDtoMapper: GameDetailsDtoMapper,
    val gameDetailsEntityMapper: GameDetailsEntityMapper,
) : GameDetailsRepository {
    private val repository: GameDetailsRepository by lazy {
        GameDetailsRepositoryImpl(
            gameDetailsApi = gameDetailsApi,
            gameDetailsDao = gameDetailsDao,
            gameDetailsDtoMapper = gameDetailsDtoMapper,
            gameDetailsEntityMapper = gameDetailsEntityMapper,
        )
    }

    override fun observeGameDetails(gameId: Int) = repository.observeGameDetails(gameId)

    override suspend fun refreshGameDetails(gameId: Int) = repository.refreshGameDetails(gameId)

    class Builder {
        private val gameDetailsApi: GameDetailsApi = mock()
        private val gameDetailsDao: GameDetailsDao = mock()
        private val gameDetailsDtoMapper: GameDetailsDtoMapper = mock()
        private val gameDetailsEntityMapper: GameDetailsEntityMapper = mock()

        fun daoObserveGame(
            gameId: Int,
            model: GameDetailsWithPlatformsAndCompanies?,
        ): Builder = apply {
            gameDetailsDao.stub { on { observeGame(gameId) } doReturn MutableStateFlow(model) }
        }

        fun apiGetGameDetails(
            gameId: Int,
            response: NetworkResponse<GameDetailsDto>,
        ): Builder = apply {
            gameDetailsApi.stub {
                onBlocking { getGameDetails(gameId) } doReturn response
            }
        }

        fun dtoMapperMap(
            dto: GameDetailsDto,
            insertionBundle: GameDetailsInsertionBundle,
        ): Builder = apply {
            gameDetailsDtoMapper.stub { on { map(dto) } doReturn insertionBundle }
        }

        fun entityMapperMap(
            model: GameDetailsWithPlatformsAndCompanies,
            gameDetails: GameDetails,
        ): Builder = apply {
            gameDetailsEntityMapper.stub { on { map(model) } doReturn gameDetails }
        }

        fun build(): GameDetailsRepositoryTestDriver = GameDetailsRepositoryTestDriver(
            gameDetailsApi = gameDetailsApi,
            gameDetailsDao = gameDetailsDao,
            gameDetailsDtoMapper = gameDetailsDtoMapper,
            gameDetailsEntityMapper = gameDetailsEntityMapper,
        )
    }
}
