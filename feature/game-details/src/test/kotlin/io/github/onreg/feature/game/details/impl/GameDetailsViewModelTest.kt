package io.github.onreg.feature.game.details.impl

import androidx.paging.PagingData
import io.github.onreg.data.details.api.GameDetailsRepository
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.game.list.api.model.Game
import io.github.onreg.data.game.list.api.model.GamePlatform
import io.github.onreg.data.movies.api.GameMoviesRepository
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.screenshots.api.GameScreenshotsRepository
import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.data.series.api.GameSeriesRepository
import io.github.onreg.feature.game.details.impl.ui.mapper.GameDetailsUiMapperImpl
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import io.github.onreg.ui.platform.model.PlatformUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class GameDetailsViewModelTest {
    @Test
    fun `should expose initial error when first refresh fails with no cache`() = runTest {
        val detailsFlow = MutableStateFlow<GameDetails?>(null)
        val detailsRepository = object : GameDetailsRepository {
            override fun observeGameDetails(gameId: Int): Flow<GameDetails?> = detailsFlow

            override suspend fun refreshGameDetails(gameId: Int) {
                error("boom")
            }
        }
        val screenshotsRepository = object : GameScreenshotsRepository {
            override fun getScreenshots(gameId: Int): Flow<PagingData<Screenshot>> =
                flowOf(PagingData.empty())
        }
        val moviesRepository = object : GameMoviesRepository {
            override fun getMovies(gameId: Int): Flow<PagingData<Movie>> =
                flowOf(PagingData.empty())
        }
        val seriesRepository = object : GameSeriesRepository {
            override fun getSeries(parentGameId: Int): Flow<PagingData<Game>> =
                flowOf(PagingData.empty())
        }
        val platformUiMapper = object : PlatformUiMapper {
            override fun mapName(model: Set<GamePlatform>): Set<String> = emptySet()

            override fun mapPlatform(model: Set<GamePlatform>): Set<PlatformUI> = emptySet()
        }

        val viewModel = GameDetailsViewModel(
            detailsRepository = detailsRepository,
            screenshotsRepository = screenshotsRepository,
            moviesRepository = moviesRepository,
            seriesRepository = seriesRepository,
            uiMapper = GameDetailsUiMapperImpl(platformUiMapper),
            platformUiMapper = platformUiMapper,
        )

        viewModel.initialize(gameId = 1)

        advanceUntilIdle()
        assertTrue(viewModel.state.value.isInitialError)
    }
}
