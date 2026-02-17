package io.github.onreg.feature.game.details.impl.mapper

import io.github.onreg.feature.game.details.impl.model.ContentState
import io.github.onreg.feature.game.details.impl.model.GameDetailsInternalState
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.model.GameDetailsUi
import javax.inject.Inject

internal interface GameDetailsStateMapper {
    fun map(
        localState: GameDetailsInternalState,
        details: GameDetailsUi?,
    ): GameDetailsState
}

internal class GameDetailsStateMapperImpl
    @Inject
    constructor() : GameDetailsStateMapper {
        override fun map(
            localState: GameDetailsInternalState,
            details: GameDetailsUi?,
        ): GameDetailsState = when {
            details != null -> GameDetailsState.Ready(
                details = details.copy(isBookmarked = localState.isBookmarked),
            )

            localState.contentState is ContentState.Loading -> GameDetailsState.Loading

            localState.contentState is ContentState.Error -> GameDetailsState.Error

            else -> error("Unsupported state combination: $localState, $details")
        }
    }
