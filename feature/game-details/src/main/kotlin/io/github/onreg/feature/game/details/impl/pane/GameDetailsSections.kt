package io.github.onreg.feature.game.details.impl.pane

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import io.github.onreg.core.ui.components.image.DynamicAsyncImage
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.feature.game.details.impl.R
import io.github.onreg.ui.details.presentation.components.DetailsSectionHeader
import io.github.onreg.ui.details.presentation.components.GameDetailsDescription
import io.github.onreg.ui.details.presentation.model.GameDetailsUi
import io.github.onreg.ui.details.presentation.model.MovieUi
import io.github.onreg.ui.details.presentation.model.ScreenshotUi
import io.github.onreg.ui.game.presentation.components.card.GameCard
import io.github.onreg.ui.game.presentation.components.card.model.GameCardUI

internal fun LazyListScope.bannerItem(detailsUi: GameDetailsUi?) {
    item {
        detailsUi?.bannerImageUrl?.let { url ->
            DynamicAsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                imageUrl = url,
            )
        }
    }
}

internal fun LazyListScope.descriptionSection(
    detailsUi: GameDetailsUi?,
    isDescriptionExpanded: Boolean,
    onToggleDescription: () -> Unit,
) {
    if (detailsUi?.descriptionHtml.isNullOrBlank()) return
    item {
        DetailsSectionHeader(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            title = stringResource(R.string.details_description),
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        GameDetailsDescription(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            descriptionHtml = detailsUi?.descriptionHtml.orEmpty(),
            isExpanded = isDescriptionExpanded,
            onToggle = onToggleDescription,
        )
    }
}

internal fun LazyListScope.developersSection(detailsUi: GameDetailsUi?) {
    if (detailsUi?.developers?.isNotEmpty() != true) return
    item {
        DetailsSectionHeader(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            title = stringResource(R.string.details_developers),
        )
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg),
        ) {
            detailsUi.developers.forEach { developer ->
                Text(text = developer, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

internal fun LazyListScope.screenshotsSection(
    items: LazyPagingItems<ScreenshotUi>,
    onScreenshotClick: (String) -> Unit,
) {
    if (!shouldShowPagingSection(items)) return
    item {
        DetailsSectionHeader(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            title = stringResource(R.string.details_screenshots),
        )
    }
    item {
        ScreenshotCarousel(
            items = items,
            onClick = onScreenshotClick,
        )
    }
}

internal fun LazyListScope.moviesSection(
    items: LazyPagingItems<MovieUi>,
    onMovieClick: (String) -> Unit,
) {
    if (!shouldShowPagingSection(items)) return
    item {
        DetailsSectionHeader(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            title = stringResource(R.string.details_movies),
        )
    }
    item {
        MovieCarousel(
            items = items,
            onClick = onMovieClick,
        )
    }
}

internal fun LazyListScope.seriesSection(
    items: LazyPagingItems<GameCardUI>,
    onSeriesClick: (Int) -> Unit,
    onBookmarkClick: (String) -> Unit,
) {
    if (!shouldShowPagingSection(items)) return
    item {
        DetailsSectionHeader(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            title = stringResource(R.string.details_series),
        )
    }
    item {
        SeriesCarousel(
            items = items,
            onSeriesClick = onSeriesClick,
            onBookmarkClick = onBookmarkClick,
        )
    }
}

@Composable
internal fun DetailsCard(
    detailsUi: GameDetailsUi?,
    onWebsiteClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
) {
    if (detailsUi == null) return
    Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
        Text(
            text = detailsUi.releaseDate,
            style = MaterialTheme.typography.bodyMedium,
        )
        detailsUi.rating?.let { rating ->
            Text(
                modifier = Modifier.padding(top = Spacing.xs),
                text = stringResource(R.string.details_rating, rating),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        val websiteUrl = detailsUi.websiteUrl
        if (detailsUi.isWebsiteVisible && websiteUrl != null) {
            Button(
                modifier = Modifier.padding(top = Spacing.sm),
                onClick = { onWebsiteClick(websiteUrl) },
            ) {
                Text(text = stringResource(R.string.details_website))
            }
        }
        Button(
            modifier = Modifier.padding(top = Spacing.sm),
            onClick = { onBookmarkClick(detailsUi.id) },
        ) {
            Text(text = stringResource(R.string.details_bookmark))
        }
    }
}

@Composable
internal fun ScreenshotCarousel(
    items: LazyPagingItems<ScreenshotUi>,
    onClick: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        contentPadding = PaddingValues(horizontal = Spacing.lg),
    ) {
        items(items.itemCount) { index ->
            val item = items[index] ?: return@items
            DynamicAsyncImage(
                modifier = Modifier
                    .size(200.dp)
                    .clickable { onClick(item.imageUrl) },
                imageUrl = item.imageUrl,
            )
        }
    }
}

@Composable
internal fun MovieCarousel(
    items: LazyPagingItems<MovieUi>,
    onClick: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        contentPadding = PaddingValues(horizontal = Spacing.lg),
    ) {
        items(items.itemCount) { index ->
            val item = items[index] ?: return@items
            Column(
                modifier = Modifier
                    .size(width = 220.dp, height = 180.dp)
                    .clickable { onClick(item.videoUrl) },
            ) {
                item.previewUrl?.let { url ->
                    DynamicAsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        imageUrl = url,
                    )
                }
                Text(
                    modifier = Modifier.padding(top = Spacing.xs),
                    text = item.name.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
internal fun SeriesCarousel(
    items: LazyPagingItems<GameCardUI>,
    onSeriesClick: (Int) -> Unit,
    onBookmarkClick: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        contentPadding = PaddingValues(horizontal = Spacing.lg),
    ) {
        items(items.itemCount) { index ->
            val item = items[index] ?: return@items
            GameCard(
                modifier = Modifier.size(width = 260.dp, height = 280.dp),
                gameData = item,
                onBookmarkClick = { onBookmarkClick(item.id) },
                onCardClicked = { onSeriesClick(item.id.toInt()) },
            )
        }
    }
}

internal fun <T : Any> shouldShowPagingSection(items: LazyPagingItems<T>): Boolean {
    val isEmpty = items.itemCount == 0
    val isError = items.loadState.refresh is LoadState.Error
    return !(isEmpty && isError)
}
