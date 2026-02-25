package io.github.onreg.feature.game.details.impl.mapper

import io.github.onreg.core.ui.R
import io.github.onreg.core.ui.components.header.AppHeaderMenu
import io.github.onreg.core.ui.components.header.AppHeaderUi
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.feature.game.details.impl.model.ContentState
import io.github.onreg.feature.game.details.impl.model.GameDetailsInternalState
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import javax.inject.Inject

internal interface GameDetailsStateMapper {
    fun map(
        gameDetails: GameDetails?,
        localState: GameDetailsInternalState,
    ): GameDetailsState
}

private val defaultHeaderUi = AppHeaderUi(
    navigationItem = AppHeaderMenu(
        iconResId = R.drawable.ic_back_24,
        contentDescriptionResId = R.string.back,
    ),
)

internal class GameDetailsStateMapperImpl
    @Inject
    constructor(
        private val gameDetailsUiMapper: GameDetailsUiMapper,
    ) : GameDetailsStateMapper {
        override fun map(
            gameDetails: GameDetails?,
            localState: GameDetailsInternalState,
        ): GameDetailsState = when {
            gameDetails != null -> GameDetailsState.Ready(
                details = gameDetailsUiMapper.map(
                    model = gameDetails,
                    localState = localState,
                ),
                headerUi = defaultHeaderUi.copy(
                    title = gameDetails.title,
                ),
            )

            localState.contentState is ContentState.Loading -> GameDetailsState.Loading(
                headerUi = defaultHeaderUi,
            )

            localState.contentState is ContentState.Error -> GameDetailsState.Error(
                headerUi = defaultHeaderUi,
            )

            else -> error("Unsupported state combination: $gameDetails, $localState")
        }
    }
