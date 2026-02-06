package io.github.onreg.data.details.impl

import io.github.onreg.core.db.TransactionProvider
import io.github.onreg.core.db.details.dao.GameDetailsDao
import io.github.onreg.core.network.rawg.api.GameDetailsApi
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.details.api.GameDetailsRepository
import io.github.onreg.data.details.api.RefreshResult
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.details.impl.mapper.GameDetailsDtoMapper
import io.github.onreg.data.details.impl.mapper.GameDetailsEntityMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

public class GameDetailsRepositoryImpl
    @Inject
    constructor(
        private val gameDetailsApi: GameDetailsApi,
        private val gameDetailsDao: GameDetailsDao,
        private val dtoMapper: GameDetailsDtoMapper,
        private val entityMapper: GameDetailsEntityMapper,
        private val transactionProvider: TransactionProvider,
    ) : GameDetailsRepository {
        override fun observeGameDetails(gameId: Int): Flow<GameDetails?> =
            gameDetailsDao.observeGameDetails(gameId).map { model ->
                model?.let(entityMapper::map)
            }

        override suspend fun refreshGameDetails(gameId: Int): RefreshResult =
            when (val response = gameDetailsApi.getGameDetails(gameId)) {
                is NetworkResponse.Success -> {
                    persistDetails(dtoMapper.map(response.body))
                    RefreshResult.Success
                }

                is NetworkResponse.Failure -> {
                    RefreshResult.Failure(
                        response.exception ?: IllegalStateException("Unknown error"),
                    )
                }
            }

        private suspend fun persistDetails(details: GameDetails) {
            val entity = entityMapper.mapToEntity(details)
            val developers = entityMapper.mapToDevelopers(details)
            transactionProvider.run {
                gameDetailsDao.upsertDetails(entity)
                gameDetailsDao.replaceDevelopers(details.id, developers)
            }
        }
    }
