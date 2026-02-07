package io.github.onreg.data.details.impl

import io.github.onreg.core.db.details.dao.GameDetailsDao
import io.github.onreg.core.network.rawg.api.GameDetailsApi
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.details.api.GameDetailsRepository
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
        private val gameDetailsDtoMapper: GameDetailsDtoMapper,
        private val gameDetailsEntityMapper: GameDetailsEntityMapper,
    ) : GameDetailsRepository {
        override fun observeGameDetails(gameId: Int): Flow<GameDetails?> =
            gameDetailsDao.observe(gameId).map { entity ->
                entity?.let(gameDetailsEntityMapper::map)
            }

        override suspend fun refreshGameDetails(gameId: Int) {
            when (val response = gameDetailsApi.getGameDetails(gameId)) {
                is NetworkResponse.Success -> {
                    gameDetailsDao.upsert(gameDetailsDtoMapper.map(response.body))
                }

                is NetworkResponse.Failure -> {
                    throw response.exception ?: IllegalStateException("Unknown game details error")
                }
            }
        }
    }
