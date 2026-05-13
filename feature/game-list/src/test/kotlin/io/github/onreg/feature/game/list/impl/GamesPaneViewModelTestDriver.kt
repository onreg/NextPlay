package io.github.onreg.feature.game.list.impl

import androidx.paging.PagingData
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.game.list.presentation.mapper.GameCardUiMapper
import kotlinx.coroutines.flow.Flow
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

internal class GamesPaneViewModelTestDriver private constructor(
    val repository: GameRepository,
    val gameCardUiMapper: GameCardUiMapper,
) {
    val viewModel by lazy { GamesPaneViewModel(repository, gameCardUiMapper) }

    class Builder {
        private val repository: GameRepository = mock()
        private val gameCardUiMapper: GameCardUiMapper = mock()

        fun repositoryGames(flow: Flow<PagingData<Game>>): Builder = apply {
            repository.stub { on { getGames() } doReturn flow }
        }

        fun gameCardUiMapperMap(
            game: Game,
            isBookmarked: Boolean,
            mapped: GameCardUI,
        ): Builder = apply {
            gameCardUiMapper.stub {
                on { map(game = game, isBookmarked = isBookmarked) } doReturn mapped
            }
        }

        fun build(): GamesPaneViewModelTestDriver = GamesPaneViewModelTestDriver(
            repository = repository,
            gameCardUiMapper = gameCardUiMapper,
        )
    }
}
