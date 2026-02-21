package io.github.onreg.ui.game.list.presentation.components.list.test

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.platform.model.PlatformUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.io.IOException
import io.github.onreg.core.ui.R as CoreUiR

public object GameListTestData {
    public val emptyItems: List<GameCardUI> = emptyList()
    public val twoItems: List<GameCardUI> = generateGameCards(2)
    public val eightItems: List<GameCardUI> = generateGameCards(8)

    public val emptyState: Flow<PagingData<GameCardUI>> = buildPagingFlow(emptyItems)
    public val loadingState: Flow<PagingData<GameCardUI>> =
        buildPagingFlow(emptyItems, LoadState.Loading)

    public val networkErrorState: Flow<PagingData<GameCardUI>> =
        buildPagingFlow(emptyItems, LoadState.Error(IOException("Preview error")))

    public val errorState: Flow<PagingData<GameCardUI>> =
        buildPagingFlow(emptyItems, LoadState.Error(IllegalStateException("Preview error")))

    public val loadedState: Flow<PagingData<GameCardUI>> = buildPagingFlow(eightItems)

    public val nextPageLoadingState: Flow<PagingData<GameCardUI>> =
        buildPagingFlow(twoItems, append = LoadState.Loading)

    public val nextPageErrorState: Flow<PagingData<GameCardUI>> =
        buildPagingFlow(twoItems, append = LoadState.Error(IllegalStateException("Preview error")))

    public val nextPageNetworkErrorState: Flow<PagingData<GameCardUI>> =
        buildPagingFlow(twoItems, append = LoadState.Error(IOException("Preview error")))

    public val refreshingState: Flow<PagingData<GameCardUI>> =
        buildPagingFlow(eightItems, refresh = LoadState.Loading)

    public val nextPageLoadingLargeState: Flow<PagingData<GameCardUI>> =
        buildPagingFlow(eightItems, append = LoadState.Loading)

    public val nextPageErrorLargeState: Flow<PagingData<GameCardUI>> =
        buildPagingFlow(
            eightItems,
            append = LoadState.Error(IllegalStateException("Preview error")),
        )

    public val nextPageNetworkErrorLargeState: Flow<PagingData<GameCardUI>> =
        buildPagingFlow(
            eightItems,
            append = LoadState.Error(IOException("Preview error")),
        )

    private fun <T : Any> buildPagingFlow(
        items: List<T>,
        refresh: LoadState = LoadState.NotLoading(false),
        append: LoadState = LoadState.NotLoading(false),
    ): Flow<PagingData<T>> = flowOf(
        PagingData.from(
            items,
            sourceLoadStates = LoadStates(
                refresh = refresh,
                append = append,
                prepend = LoadState.NotLoading(false),
            ),
        ),
    )

    public fun generateGameCards(count: Int): List<GameCardUI> = List(count) { index ->
        val id = (index + 1).toString()
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
