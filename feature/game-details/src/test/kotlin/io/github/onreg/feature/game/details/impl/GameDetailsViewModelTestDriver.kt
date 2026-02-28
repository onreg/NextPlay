package io.github.onreg.feature.game.details.impl

import androidx.paging.PagingData
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.components.header.AppHeaderUi
import io.github.onreg.core.util.android.intent.UrlOpener
import io.github.onreg.data.details.api.GameDetailsRepository
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.movies.api.GameMoviesRepository
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.screenshots.api.GameScreenshotsRepository
import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.feature.game.details.impl.mapper.GameDetailsStateMapper
import io.github.onreg.feature.game.details.impl.mapper.MovieUiMapperImpl
import io.github.onreg.feature.game.details.impl.mapper.ScreenshotUiMapperImpl
import io.github.onreg.feature.game.details.impl.model.ContentState
import io.github.onreg.feature.game.details.impl.model.DescriptionToggleUi
import io.github.onreg.feature.game.details.impl.model.GameDescriptionUi
import io.github.onreg.feature.game.details.impl.model.GameDetailsInternalState
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.model.GameDetailsUi
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.game.list.presentation.mapper.GameCardUiMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

internal class GameDetailsViewModelTestDriver private constructor(
    val gameId: Int,
    val detailsRepository: GameDetailsRepository,
    val screenshotsRepository: GameScreenshotsRepository,
    val moviesRepository: GameMoviesRepository,
    val gameRepository: GameRepository,
    val stateMapper: GameDetailsStateMapper,
    val urlOpener: UrlOpener,
    val gameCardUiMapper: GameCardUiMapper,
) {
    val viewModel by lazy {
        GameDetailsViewModel(
            gameId = gameId,
            detailsRepository = detailsRepository,
            screenshotsRepository = screenshotsRepository,
            moviesRepository = moviesRepository,
            seriesRepository = gameRepository,
            stateMapper = stateMapper,
            screenshotUiMapper = ScreenshotUiMapperImpl(),
            movieUiMapper = MovieUiMapperImpl(),
            gameCardUiMapper = gameCardUiMapper,
            urlOpener = urlOpener,
        )
    }

    class Builder {
        private var gameId: Int = 1
        private var refreshResult: Result<Unit> = Result.success(Unit)
        private var screenshotsFlow: Flow<PagingData<Screenshot>> = flowOf(PagingData.empty())
        private var moviesFlow: Flow<PagingData<Movie>> = flowOf(PagingData.empty())
        private var seriesFlow: Flow<PagingData<Game>> = flowOf(PagingData.empty())
        private val detailsFlow = MutableStateFlow<GameDetails?>(null)
        private val detailsRepository: GameDetailsRepository = mock()
        private val screenshotsRepository: GameScreenshotsRepository = mock()
        private val moviesRepository: GameMoviesRepository = mock()
        private val gameRepository: GameRepository = mock()
        private val stateMapper: GameDetailsStateMapper = mock()
        private val urlOpener: UrlOpener = mock()
        private val gameCardUiMapper: GameCardUiMapper = mock()

        init {
            detailsRepository.stub {
                on { observeGameDetails(any()) } doReturn detailsFlow
                onBlocking { refreshGameDetails(any()) } doAnswer {
                    refreshResult
                }
            }
            screenshotsRepository.stub {
                on { getScreenshots(any()) } doAnswer { screenshotsFlow }
            }
            moviesRepository.stub {
                on { getMovies(any()) } doAnswer { moviesFlow }
            }
            gameRepository.stub {
                on { getGames() } doReturn flowOf(PagingData.empty())
                on { getSeries(any()) } doAnswer { seriesFlow }
            }
            stateMapper.stub {
                on { map(gameDetails = anyOrNull(), localState = any()) } doAnswer { invocation ->
                    val gameDetails = invocation.getArgument<GameDetails?>(0)
                    val localState = invocation.getArgument<GameDetailsInternalState>(1)
                    when {
                        gameDetails != null -> GameDetailsState.Ready(
                            details = GameDetailsUi(
                                image = gameDetails.imageUrl,
                                rating = ChipUI(
                                    text = gameDetails.rating.toString(),
                                    isSelected = true,
                                ),
                                releaseDate = "",
                                platforms = emptySet(),
                                website = gameDetails.website,
                                gameDescriptionUi = GameDescriptionUi(
                                    description = gameDetails.description,
                                    isExpanded = localState.isDescriptionExpanded,
                                    descriptionToggleUi = DescriptionToggleUi(
                                        text = "",
                                        isVisible = localState.isReadMoreVisible || localState.isDescriptionExpanded,
                                    ),
                                ),
                                companies = emptyList(),
                                isBookmarked = localState.isBookmarked,
                            ),
                            headerUi = AppHeaderUi(title = gameDetails.title),
                        )

                        localState.contentState is ContentState.Error -> GameDetailsState.Error(
                            headerUi = AppHeaderUi(),
                        )

                        else -> GameDetailsState.Loading(
                            headerUi = AppHeaderUi(),
                        )
                    }
                }
            }
        }

        fun gameId(value: Int): Builder = apply {
            gameId = value
        }

        fun initialDetails(value: GameDetails?): Builder = apply {
            detailsFlow.value = value
        }

        fun refreshResult(value: Result<Unit>): Builder = apply {
            refreshResult = value
        }

        fun screenshots(flow: Flow<PagingData<Screenshot>>): Builder = apply {
            screenshotsFlow = flow
        }

        fun movies(flow: Flow<PagingData<Movie>>): Builder = apply {
            moviesFlow = flow
        }

        fun series(flow: Flow<PagingData<Game>>): Builder = apply {
            seriesFlow = flow
        }

        fun gameCardUiMapperMap(game: Game, mapped: GameCardUI): Builder = apply {
            gameCardUiMapper.stub {
                on { map(game = game, isBookmarked = false) } doReturn mapped
            }
        }

        fun build(): GameDetailsViewModelTestDriver = GameDetailsViewModelTestDriver(
            gameId = gameId,
            detailsRepository = detailsRepository,
            screenshotsRepository = screenshotsRepository,
            moviesRepository = moviesRepository,
            gameRepository = gameRepository,
            stateMapper = stateMapper,
            urlOpener = urlOpener,
            gameCardUiMapper = gameCardUiMapper
        )
    }
}
