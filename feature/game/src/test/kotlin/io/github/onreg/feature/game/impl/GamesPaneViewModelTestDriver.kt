package io.github.onreg.feature.game.impl

import androidx.paging.PagingData
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.ui.game.presentation.mapper.GameUiMapperImpl
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import io.github.onreg.ui.platform.model.PlatformUI
import kotlinx.coroutines.flow.Flow
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

internal class GamesPaneViewModelTestDriver private constructor(
    val repository: GameRepository,
    val platformUiMapper: PlatformUiMapper
) {

    val viewModel by lazy { GamesPaneViewModel(repository, GameUiMapperImpl(platformUiMapper)) }

    class Builder {
        private val repository: GameRepository = mock()
        private val platformUiMapper: PlatformUiMapper = mock()

        fun repositoryGames(flow: Flow<PagingData<Game>>): Builder = apply {
            repository.stub { on { getGames() } doReturn flow }
        }

        fun platformUiMapperMapPlatform(
            platforms: Set<GamePlatform>,
            mapped: Set<PlatformUI>
        ): Builder = apply {
            platformUiMapper.stub { on { mapPlatform(platforms) } doReturn mapped }
        }

        fun build(): GamesPaneViewModelTestDriver = GamesPaneViewModelTestDriver(
            repository = repository,
            platformUiMapper = platformUiMapper
        )
    }
}
