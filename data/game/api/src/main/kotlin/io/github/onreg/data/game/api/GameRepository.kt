package io.github.onreg.data.game.api

import androidx.paging.PagingData
import io.github.onreg.data.game.api.model.Game
import kotlinx.coroutines.flow.Flow

public interface GameRepository {
    public fun getGames(): Flow<PagingData<Game>>
}
