package io.github.onreg.feature.game.list.impl.pane

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.onreg.core.ui.components.content.info.ContentInfo
import io.github.onreg.core.ui.components.content.info.ContentInfoUI
import io.github.onreg.core.ui.preview.TabletThemePreview
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.runtime.flow.collectWithLifecycle
import io.github.onreg.core.ui.runtime.paging.ErrorType
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.feature.game.list.impl.GamesPaneViewModel
import io.github.onreg.feature.game.list.impl.R
import io.github.onreg.feature.game.list.impl.model.GamesPaneEvent
import io.github.onreg.feature.game.list.impl.model.GamesPaneListEvent
import io.github.onreg.feature.game.list.impl.test.GamesPaneTestTags
import io.github.onreg.ui.game.list.presentation.components.card.GameCardError
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.game.list.presentation.components.list.GameList
import io.github.onreg.ui.game.list.presentation.components.list.test.GameListTestData
import kotlinx.coroutines.flow.Flow
import io.github.onreg.core.ui.R as CoreUiR
import io.github.onreg.ui.game.list.presentation.R as PresentationR

@Composable
public fun GamesPane(
    modifier: Modifier = Modifier,
    isLargeScreen: Boolean = false,
    onOpenGameDetails: (Int) -> Unit,
) {
    val viewModel = hiltViewModel<GamesPaneViewModel>()

    val pagingState = viewModel.pagingState.collectAsLazyPagingItems()

    GamesPaneScreen(
        modifier = modifier.fillMaxSize(),
        isLargeScreen = isLargeScreen,
        pagingState = pagingState,
        onRefreshClicked = viewModel::onRefreshClicked,
        onRetryClicked = viewModel::onRetryClicked,
        onBookMarkClicked = viewModel::onBookMarkClicked,
        onCardClicked = viewModel::onCardClicked,
    )

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            is GamesPaneEvent.GoToDetails -> onOpenGameDetails(event.gameId)
        }
    }

    viewModel.pagingEvents.collectWithLifecycle { event ->
        when (event) {
            GamesPaneListEvent.Retry -> pagingState.retry()
            GamesPaneListEvent.Refresh -> pagingState.refresh()
        }
    }
}

@Composable
internal fun GamesPaneScreen(
    modifier: Modifier = Modifier,
    isLargeScreen: Boolean = false,
    pagingState: LazyPagingItems<GameCardUI>,
    onRefreshClicked: () -> Unit = {},
    onRetryClicked: () -> Unit = {},
    onBookMarkClicked: (Int) -> Unit = {},
    onCardClicked: (Int) -> Unit = {},
) {
    ContentComponent(
        modifier = modifier,
        isLargeScreen = isLargeScreen,
        pagingState = pagingState,
        onRefreshClicked = onRefreshClicked,
        onPageRetryClicked = onRetryClicked,
        onBookMarkClicked = onBookMarkClicked,
        onCardClicked = onCardClicked,
    )
}

@Composable
private fun ContentComponent(
    modifier: Modifier = Modifier,
    isLargeScreen: Boolean = false,
    pagingState: LazyPagingItems<GameCardUI>,
    onRefreshClicked: () -> Unit,
    onPageRetryClicked: () -> Unit,
    onBookMarkClicked: (Int) -> Unit,
    onCardClicked: (Int) -> Unit,
) {
    GameList(
        modifier = modifier,
        lazyPagingItems = pagingState,
        columns = if (isLargeScreen) 4 else 1,
        onRefresh = onRefreshClicked,
        onRetry = onPageRetryClicked,
        onBookmarkClicked = onBookMarkClicked,
        onCardClicked = onCardClicked,
        onError = { errorType ->
            ErrorComponent(
                modifier = modifier,
                errorType = errorType,
                onRetry = onPageRetryClicked,
            )
        },
        onEmpty = { EmptyComponent(modifier) },
    )
}

@Composable
private fun ErrorComponent(
    modifier: Modifier = Modifier,
    errorType: ErrorType,
    onRetry: () -> Unit,
) {
    Box(
        modifier = modifier.testTag(GamesPaneTestTags.TAG_COMPONENT_ERROR),
        contentAlignment = Alignment.Center,
    ) {
        GameCardError(
            errorType = errorType,
            onRetry = onRetry,
        )
    }
}

@Composable
private fun EmptyComponent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.testTag(GamesPaneTestTags.TAG_COMPONENT_EMPTY),
        contentAlignment = Alignment.Center,
    ) {
        ContentInfo(
            contentInfoUI = ContentInfoUI(
                iconRes = CoreUiR.drawable.ic_controller_24,
                titleResId = PresentationR.string.games_empty_title,
                descriptionResId = R.string.games_empty_description,
            ),
        )
    }
}

@Composable
@ThemePreview
private fun ErrorPreview() {
    GamesPanePreview(
        pagingState = GameListTestData.errorState,
    )
}

@Composable
@ThemePreview
private fun NetworkErrorPreview() {
    GamesPanePreview(
        pagingState = GameListTestData.networkErrorState,
    )
}

@Composable
@ThemePreview
private fun EmptyPreview() {
    GamesPanePreview(
        pagingState = GameListTestData.emptyState,
    )
}

@Composable
@ThemePreview
private fun LoadedPreview() {
    GamesPanePreview(
        pagingState = GameListTestData.loadedState,
    )
}

@Composable
@TabletThemePreview
private fun ErrorTabletPreview() {
    GamesPanePreview(
        isLargeScreen = true,
        pagingState = GameListTestData.errorState,
    )
}

@Composable
@TabletThemePreview
private fun NetworkErrorTabletPreview() {
    GamesPanePreview(
        isLargeScreen = true,
        pagingState = GameListTestData.networkErrorState,
    )
}

@Composable
@TabletThemePreview
private fun EmptyTabletPreview() {
    GamesPanePreview(
        isLargeScreen = true,
        pagingState = GameListTestData.emptyState,
    )
}

@Composable
@TabletThemePreview
private fun LoadedTabletPreview() {
    GamesPanePreview(
        isLargeScreen = true,
        pagingState = GameListTestData.loadedState,
    )
}

@Composable
private fun GamesPanePreview(
    isLargeScreen: Boolean = false,
    pagingState: Flow<PagingData<GameCardUI>>,
) {
    NextPlayTheme {
        val lazyPagingItems = pagingState.collectAsLazyPagingItems()
        Scaffold { paddingValues ->
            GamesPaneScreen(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                isLargeScreen = isLargeScreen,
                pagingState = lazyPagingItems,
            )
        }
    }
}
