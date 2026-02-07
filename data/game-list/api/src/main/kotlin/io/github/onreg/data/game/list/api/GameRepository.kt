package io.github.onreg.data.game.list.api

import androidx.paging.PagingData
import io.github.onreg.data.game.list.api.model.Game
import kotlinx.coroutines.flow.Flow

public interface GameRepository {
    public fun getGames(): Flow<PagingData<Game>>
}
