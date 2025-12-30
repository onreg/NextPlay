package io.github.onreg.feature.game.impl

import androidx.paging.PagingData
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.ui.game.presentation.mapper.GameUiMapper
import kotlinx.coroutines.flow.Flow
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

internal class GamesViewModelTestDriver private constructor(
    val repository: GameRepository,
    val gameUiMapper: GameUiMapper
) {

    val viewModel by lazy { GamesViewModel(repository, gameUiMapper) }

    class Builder {
        private val repository: GameRepository = mock()
        private val gameUiMapper: GameUiMapper = mock()

        fun repositoryGames(flow: Flow<PagingData<Game>>): Builder = apply {
            repository.stub { on { getGames() } doReturn flow }
        }

        fun gameUiMapperMap(
            games: PagingData<Game>,
            bookMarks: Set<String>,
            mapped: PagingData<GameCardUI>
        ): Builder = apply {
            gameUiMapper.stub { on { map(games, bookMarks) } doReturn mapped }
        }

        fun build(): GamesViewModelTestDriver = GamesViewModelTestDriver(
            repository = repository,
            gameUiMapper = gameUiMapper
        )
    }
}
