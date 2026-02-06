package io.github.onreg.data.series.api

import androidx.paging.PagingData
import io.github.onreg.data.game.api.model.Game
import kotlinx.coroutines.flow.Flow

public interface GameSeriesRepository {
    public fun getSeries(parentGameId: Int): Flow<PagingData<Game>>
}
