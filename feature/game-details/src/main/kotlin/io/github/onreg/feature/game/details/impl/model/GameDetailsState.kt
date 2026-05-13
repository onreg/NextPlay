package io.github.onreg.feature.game.details.impl.model

import io.github.onreg.core.ui.components.header.AppHeaderUi

internal sealed class GameDetailsState {

    abstract val headerUi: AppHeaderUi

    data class Loading(
        override val headerUi: AppHeaderUi
    ) : GameDetailsState()

    data class Error(
        override val headerUi: AppHeaderUi
    ) : GameDetailsState()

    data class Ready(
        val details: GameDetailsUi,
        override val headerUi: AppHeaderUi
    ) : GameDetailsState()
}
