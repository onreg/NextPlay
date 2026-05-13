package io.github.onreg.ui.game.list.presentation.components.list.test

import androidx.paging.PagingData
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.runtime.paging.ErrorType
import io.github.onreg.core.ui.runtime.paging.appendErrorPagingFlow
import io.github.onreg.core.ui.runtime.paging.appendLoadingPagingFlow
import io.github.onreg.core.ui.runtime.paging.emptyPagingFlow
import io.github.onreg.core.ui.runtime.paging.errorPagingFlow
import io.github.onreg.core.ui.runtime.paging.loadedPagingFlow
import io.github.onreg.core.ui.runtime.paging.loadingPagingFlow
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.platform.model.PlatformUI
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import io.github.onreg.core.ui.R as CoreUiR

public object GameListTestData {
    public val emptyItems: List<GameCardUI> = emptyList()
    public val twoItems: List<GameCardUI> = generateGameCards(2)
    public val eightItems: List<GameCardUI> = generateGameCards(8)
    public val previewCard: GameCardUI = generateGameCards(1).first()
    public val networkErrorType: ErrorType = ErrorType.NETWORK
    public val errorType: ErrorType = ErrorType.OTHER

    public val emptyState: Flow<PagingData<GameCardUI>> = emptyPagingFlow()
    public val loadingState: Flow<PagingData<GameCardUI>> = loadingPagingFlow()

    public val networkErrorState: Flow<PagingData<GameCardUI>> =
        errorPagingFlow(IOException("Preview error"))

    public val errorState: Flow<PagingData<GameCardUI>> =
        errorPagingFlow(IllegalStateException("Preview error"))

    public val loadedState: Flow<PagingData<GameCardUI>> = loadedPagingFlow(eightItems)

    public val nextPageLoadingState: Flow<PagingData<GameCardUI>> =
        appendLoadingPagingFlow(twoItems)

    public val nextPageErrorState: Flow<PagingData<GameCardUI>> =
        appendErrorPagingFlow(twoItems, IllegalStateException("Preview error"))

    public val nextPageNetworkErrorState: Flow<PagingData<GameCardUI>> =
        appendErrorPagingFlow(twoItems, IOException("Preview error"))

    public val refreshingState: Flow<PagingData<GameCardUI>> =
        loadingPagingFlow(eightItems)

    public val nextPageLoadingLargeState: Flow<PagingData<GameCardUI>> =
        appendLoadingPagingFlow(eightItems)

    public val nextPageErrorLargeState: Flow<PagingData<GameCardUI>> =
        appendErrorPagingFlow(eightItems, IllegalStateException("Preview error"))

    public val nextPageNetworkErrorLargeState: Flow<PagingData<GameCardUI>> =
        appendErrorPagingFlow(eightItems, IOException("Preview error"))

    public fun generateGameCards(count: Int): List<GameCardUI> = List(count) { index ->
        val id = index + 1
        GameCardUI(
            id = id,
            title = "Game $id",
            imageUrl = "https://example.com",
            releaseDate = "2024",
            platforms = setOf(PlatformUI(name = "PC", iconRes = CoreUiR.drawable.ic_controller_24)),
            rating = ChipUI(text = "4.$id", isSelected = true),
            isBookmarked = index % 2 == 0,
        )
    }
}
