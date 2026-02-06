package io.github.onreg.feature.game.details.impl

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import io.github.onreg.data.details.api.GameDetailsRepository
import io.github.onreg.data.details.api.RefreshResult
import io.github.onreg.data.details.api.model.Developer
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.movies.api.GameMoviesRepository
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.screenshots.api.GameScreenshotsRepository
import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.data.series.api.GameSeriesRepository
import io.github.onreg.ui.details.presentation.mapper.GameDetailsUiMapper
import io.github.onreg.ui.details.presentation.model.GameDetailsUi
import io.github.onreg.ui.game.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.game.presentation.mapper.GameUiMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

internal class GameDetailsViewModelTestDriver private constructor(
    val repository: FakeGameDetailsRepository,
) {
    val viewModel = GameDetailsViewModel(
        savedStateHandle = SavedStateHandle(mapOf(GameDetailsViewModel.GAME_ID_KEY to "1")),
        gameDetailsRepository = repository,
        screenshotsRepository = FakeScreenshotsRepository,
        moviesRepository = FakeMoviesRepository,
        seriesRepository = FakeSeriesRepository,
        gameDetailsUiMapper = FakeGameDetailsUiMapper,
        gameUiMapper = FakeGameUiMapper,
    )

    class Builder {
        private val repository = FakeGameDetailsRepository()

        fun refreshResult(result: RefreshResult): Builder = apply {
            repository.refreshResult = result
        }

        fun details(details: GameDetails?): Builder = apply {
            repository.detailsFlow.value = details
        }

        fun build(): GameDetailsViewModelTestDriver = GameDetailsViewModelTestDriver(repository)
    }
}

internal class FakeGameDetailsRepository : GameDetailsRepository {
    val detailsFlow = MutableStateFlow<GameDetails?>(null)
    var refreshResult: RefreshResult = RefreshResult.Success
    var refreshCount = 0

    override fun observeGameDetails(gameId: Int): Flow<GameDetails?> = detailsFlow

    override suspend fun refreshGameDetails(gameId: Int): RefreshResult {
        refreshCount += 1
        return refreshResult
    }
}

internal object FakeScreenshotsRepository : GameScreenshotsRepository {
    override fun getScreenshots(gameId: Int): Flow<PagingData<Screenshot>> =
        flowOf(PagingData.empty())
}

internal object FakeMoviesRepository : GameMoviesRepository {
    override fun getMovies(gameId: Int): Flow<PagingData<Movie>> = flowOf(PagingData.empty())
}

internal object FakeSeriesRepository : GameSeriesRepository {
    override fun getSeries(parentGameId: Int): Flow<PagingData<Game>> = flowOf(PagingData.empty())
}

internal object FakeGameDetailsUiMapper : GameDetailsUiMapper {
    override fun map(details: GameDetails): GameDetailsUi = GameDetailsUi(
        id = details.id.toString(),
        title = details.name,
        bannerImageUrl = details.bannerImageUrl,
        releaseDate = details.releaseDate?.toString().orEmpty(),
        rating = details.rating?.toString(),
        websiteUrl = details.websiteUrl,
        isWebsiteVisible = details.websiteUrl != null,
        descriptionHtml = details.descriptionHtml,
        developers = details.developers.map(Developer::name),
    )
}

internal object FakeGameUiMapper : GameUiMapper {
    override fun map(
        games: PagingData<Game>,
        bookmarks: Set<String>,
    ): PagingData<GameCardUI> = PagingData.empty()
}
