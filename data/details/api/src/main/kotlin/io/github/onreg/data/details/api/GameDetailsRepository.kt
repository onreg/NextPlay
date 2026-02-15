package io.github.onreg.data.details.api

import io.github.onreg.data.details.api.model.GameDetails
import kotlinx.coroutines.flow.Flow

public interface GameDetailsRepository {
    public fun observeGameDetails(gameId: Int): Flow<GameDetails?>

    public suspend fun refreshGameDetails(gameId: Int): Result<Unit>
}
