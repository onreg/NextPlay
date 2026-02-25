package io.github.onreg.feature.game.details.impl.pane.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.onreg.core.ui.components.image.DynamicAsyncImage
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.feature.game.details.impl.R
import io.github.onreg.feature.game.details.impl.model.ScreenshotUI
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData

@Composable
internal fun ScreenshotsComponent(
    modifier: Modifier = Modifier,
    screenshots: LazyPagingItems<ScreenshotUI>,
    onScreenshotClicked: (String) -> Unit,
) {
    val isLoading = screenshots.loadState.refresh is LoadState.Loading
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.screenshots_section_title),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        if (isLoading) {
            LoadingMediaSection()
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(screenshots) { screenshot ->
                    Card(
                        modifier = Modifier.clickable { onScreenshotClicked(screenshot.imageUrl) },
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        DynamicAsyncImage(
                            modifier = Modifier.size(288.dp, 162.dp),
                            imageUrl = screenshot.imageUrl,
                        )
                    }
                }
            }
        }
    }
}

@ThemePreview
@Composable
private fun LoadedStatePreview() {
    val screenshots = GameDetailsTestData.screenshots.collectAsLazyPagingItems()
    NextPlayTheme {
        Surface {
            ScreenshotsComponent(
                screenshots = screenshots,
                onScreenshotClicked = {},
            )
        }
    }
}

@ThemePreview
@Composable
private fun LoadingStatePreview() {
    val screenshots = GameDetailsTestData.screenshots.collectAsLazyPagingItems()
    NextPlayTheme {
        Surface {
            ScreenshotsComponent(
                screenshots = screenshots,
                onScreenshotClicked = {},
            )
        }
    }
}
