package io.github.onreg.feature.game.impl.test

import io.github.onreg.core.ui.components.list.GameListTestData
import io.github.onreg.feature.game.impl.model.GamePaneState

internal object GamePaneTestData {
    val errorPaneState: GamePaneState = GamePaneState.Error
    val readyPaneState: GamePaneState = GamePaneState.Ready
    val emptyState = GameListTestData.emptyState
    val loadingState = GameListTestData.loadingState
    val pagingErrorState = GameListTestData.pagingErrorState
    val loadedState = GameListTestData.loadedState
}
