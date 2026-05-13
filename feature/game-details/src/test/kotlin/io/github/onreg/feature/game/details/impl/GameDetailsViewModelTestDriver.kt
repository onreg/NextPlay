package io.github.onreg.feature.game.details.impl

import androidx.paging.PagingData
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.util.android.intent.UrlOpener
import io.github.onreg.data.details.api.GameDetailsRepository
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.movies.api.GameMoviesRepository
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.screenshots.api.GameScreenshotsRepository
import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.feature.game.details.impl.mapper.GameDetailsStateMapperImpl
import io.github.onreg.feature.game.details.impl.mapper.GameDetailsUiMapper
import io.github.onreg.feature.game.details.impl.mapper.MovieUiMapper
import io.github.onreg.feature.game.details.impl.mapper.ScreenshotUiMapper
import io.github.onreg.feature.game.details.impl.model.DescriptionToggleUi
import io.github.onreg.feature.game.details.impl.model.GameDescriptionUi
import io.github.onreg.feature.game.details.impl.model.GameDetailsInternalState
import io.github.onreg.feature.game.details.impl.model.GameDetailsUi
import io.github.onreg.ui.game.list.presentation.mapper.GameCardUiMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

internal class GameDetailsViewModelTestDriver private constructor(
    val gameId: Int,
    val detailsRepository: GameDetailsRepository,
    val urlOpener: UrlOpener,
    private val screenshotsRepository: GameScreenshotsRepository,
    private val moviesRepository: GameMoviesRepository,
    private val seriesRepository: GameRepository,
) {
    val viewModel: GameDetailsViewModel by lazy {
        GameDetailsViewModel(
            gameId = gameId,
            detailsRepository = detailsRepository,
            screenshotsRepository = screenshotsRepository,
            moviesRepository = moviesRepository,
            seriesRepository = seriesRepository,
            stateMapper = GameDetailsStateMapperImpl(gameDetailsUiMapper = TestGameDetailsUiMapper),
            screenshotUiMapper = mock<ScreenshotUiMapper>(),
            movieUiMapper = mock<MovieUiMapper>(),
            gameCardUiMapper = mock<GameCardUiMapper>(),
            urlOpener = urlOpener,
        )
    }

    class Builder {
        private var gameId: Int = 1
        private val detailsRepository: GameDetailsRepository = mock()
        private val screenshotsRepository: GameScreenshotsRepository = mock()
        private val moviesRepository: GameMoviesRepository = mock()
        private val seriesRepository: GameRepository = mock()
        private val urlOpener: UrlOpener = mock()
        private var observedGameDetails: Flow<GameDetails?> = emptyFlow()
        private var refreshResult: Result<Unit> = Result.success(Unit)
        private var screenshots: Flow<PagingData<Screenshot>> = flowOf(PagingData.from(emptyList()))
        private var movies: Flow<PagingData<Movie>> = flowOf(PagingData.from(emptyList()))
        private var series: Flow<PagingData<Game>> = flowOf(PagingData.from(emptyList()))

        fun gameId(gameId: Int): Builder = apply {
            this.gameId = gameId
        }

        fun detailsRepositoryObserveGameDetails(
            observedGameDetails: Flow<GameDetails?>,
        ): Builder = apply {
            this.observedGameDetails = observedGameDetails
        }

        fun detailsRepositoryRefreshGameDetails(
            refreshResult: Result<Unit>,
        ): Builder = apply {
            this.refreshResult = refreshResult
        }

        fun build(): GameDetailsViewModelTestDriver {
            detailsRepository.stub {
                on { observeGameDetails(gameId) } doReturn observedGameDetails
                onBlocking { refreshGameDetails(gameId) } doReturn refreshResult
            }
            screenshotsRepository.stub {
                on { getScreenshots(gameId) } doReturn screenshots
            }
            moviesRepository.stub {
                on { getMovies(gameId) } doReturn movies
            }
            seriesRepository.stub {
                on { getSeries(gameId) } doReturn series
            }
            return GameDetailsViewModelTestDriver(
                gameId = gameId,
                detailsRepository = detailsRepository,
                urlOpener = urlOpener,
                screenshotsRepository = screenshotsRepository,
                moviesRepository = moviesRepository,
                seriesRepository = seriesRepository,
            )
        }
    }
}

private object TestGameDetailsUiMapper : GameDetailsUiMapper {
    private const val READ_MORE = "Read more"
    private const val READ_LESS = "Read less"

    override fun map(
        model: GameDetails,
        localState: GameDetailsInternalState,
    ): GameDetailsUi = GameDetailsUi(
        image = model.imageUrl,
        rating = ChipUI(
            text = model.rating.toString(),
            isSelected = true,
        ),
        releaseDate = "",
        platforms = emptySet(),
        website = model.website,
        gameDescriptionUi = GameDescriptionUi(
            description = model.description,
            isExpanded = localState.isDescriptionExpanded,
            descriptionToggleUi = DescriptionToggleUi(
                text = if (localState.isDescriptionExpanded) READ_LESS else READ_MORE,
                isVisible = localState.isReadMoreVisible || localState.isDescriptionExpanded,
            ),
        ),
        companies = emptyList(),
        isBookmarked = localState.isBookmarked,
    )
}
