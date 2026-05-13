package io.github.onreg.feature.game.details.impl.pane.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.onreg.core.ui.components.image.DynamicAsyncImage
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.runtime.paging.PagedListState
import io.github.onreg.core.ui.runtime.paging.resolveState
import io.github.onreg.core.ui.theme.MediaSectionTokens
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.feature.game.details.impl.R
import io.github.onreg.feature.game.details.impl.model.ScreenshotUI
import io.github.onreg.feature.game.details.impl.test.GameDetailsPaneTestTags
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData
import kotlinx.coroutines.flow.Flow

@Composable
internal fun ScreenshotsComponent(
    modifier: Modifier = Modifier,
    pagingState: PagedListState<ScreenshotUI>,
    onScreenshotClicked: (String) -> Unit,
) {

    when (pagingState) {
        PagedListState.Loading -> {
            LoadingMediaSection(modifier = modifier)
        }

        is PagedListState.Loaded -> {
            Column(
                modifier = modifier.testTag(GameDetailsPaneTestTags.GAME_DETAILS_SCREENSHOTS_SECTION),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                Text(
                    text = stringResource(R.string.screenshots_section_title),
                    modifier = Modifier.padding(horizontal = Spacing.lg),
                    style = MaterialTheme.typography.titleMedium,
                )
                LazyRow(
                    contentPadding = PaddingValues(Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    val lazyPagingItems = pagingState.items
                    items(
                        count = lazyPagingItems.itemCount,
                        key = lazyPagingItems.itemKey { screenshot ->
                            screenshot.id
                        },
                    ) { index ->
                        val screenshot = lazyPagingItems[index] ?: return@items
                        Card(
                            onClick = { onScreenshotClicked(screenshot.imageUrl) },
                            modifier = Modifier
                                .testTag(
                                    GameDetailsPaneTestTags.GAME_DETAILS_SCREENSHOT_PREFIX.plus(
                                        screenshot.id,
                                    ),
                                )
                                .width(MediaSectionTokens.itemWidthPhone)
                                .aspectRatio(MediaSectionTokens.aspectRatio16x9),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            DynamicAsyncImage(
                                modifier = Modifier.fillMaxSize(),
                                imageUrl = screenshot.imageUrl,
                            )
                        }
                    }
                }
            }
        }

        PagedListState.Empty -> Unit

        is PagedListState.Error -> Unit
    }
}

@Composable
@ThemePreview
private fun LoadedPreview() {
    ScreenshotsComponentPreview(
        screenshots = GameDetailsTestData.screenshots,
    )
}

@Composable
@ThemePreview
private fun LoadingPreview() {
    ScreenshotsComponentPreview(
        screenshots = GameDetailsTestData.loadingScreenshots,
    )
}

@Composable
private fun ScreenshotsComponentPreview(
    screenshots: Flow<PagingData<ScreenshotUI>>,
) {
    NextPlayTheme {
        val screenshotsItems = screenshots.collectAsLazyPagingItems()
        Surface {
            ScreenshotsComponent(
                pagingState = screenshotsItems.resolveState(),
                onScreenshotClicked = {},
            )
        }
    }
}
