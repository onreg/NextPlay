package io.github.onreg.data.screenshots.api

import androidx.paging.PagingData
import io.github.onreg.data.screenshots.api.model.Screenshot
import kotlinx.coroutines.flow.Flow

public interface GameScreenshotsRepository {
    public fun getScreenshots(gameId: Int): Flow<PagingData<Screenshot>>
}
