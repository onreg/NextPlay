package io.github.onreg.feature.game.details.impl.pane

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.components.image.DynamicAsyncImage
import io.github.onreg.core.ui.theme.Gray12
import io.github.onreg.core.ui.theme.Gray26
import io.github.onreg.core.ui.theme.Gray45
import io.github.onreg.core.ui.theme.Gray70
import io.github.onreg.core.ui.theme.Gray92
import io.github.onreg.core.ui.theme.Gray98
import io.github.onreg.data.game.list.api.model.Game
import io.github.onreg.data.game.list.api.model.GamePlatform
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.ui.model.GameCompanyUi
import io.github.onreg.feature.game.details.impl.ui.model.GameDetailsUi
import io.github.onreg.ui.game.list.presentation.components.card.GameCard
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.platform.model.PlatformUI
import io.github.onreg.ui.game.list.presentation.R as GameListPresentationR

@Composable
internal fun DetailsSection(
    state: GameDetailsState,
    details: GameDetailsUi,
    onWebsiteClicked: () -> Unit,
    onBookmarkClicked: () -> Unit,
) {
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = detailsCardColor()),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DetailsMetadata(
                    details = details,
                    state = state,
                    onWebsiteClicked = onWebsiteClicked,
                    onBookmarkClicked = onBookmarkClicked,
                )
            }
        }
    }
}

@Composable
internal fun DescriptionSection(
    description: String,
    isExpanded: Boolean,
    hasOverflow: Boolean,
    onDescriptionTextLayout: (Boolean) -> Unit,
    onToggleDescription: () -> Unit,
) {
    SectionHeader(title = "Description")
    val supportingTextColor = supportingTextColor()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = description,
            maxLines = if (isExpanded) Int.MAX_VALUE else COLLAPSED_DESCRIPTION_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            color = supportingTextColor,
            style = MaterialTheme.typography.bodyMedium,
            onTextLayout = { onDescriptionTextLayout(it.hasVisualOverflow) },
        )
        if (hasOverflow || isExpanded) {
            Text(
                text = readMoreText(isExpanded),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable(onClick = onToggleDescription),
            )
        }
    }
}

@Composable
internal fun DevelopersAndPublishersSection(companies: List<GameCompanyUi>) {
    SectionHeader(title = "Developers & Publishers")
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        companies.forEach { company ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DynamicAsyncImage(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    imageUrl = company.logoUrl.orEmpty(),
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = company.name,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = company.role.label,
                        color = supportingTextColor(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsMetadata(
    details: GameDetailsUi,
    state: GameDetailsState,
    onWebsiteClicked: () -> Unit,
    onBookmarkClicked: () -> Unit,
) {
    val supportingTextColor = supportingTextColor()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            details.releaseDate?.takeIf { it.isNotBlank() }?.let { releaseDate ->
                Text(
                    text = releaseDate,
                    color = supportingTextColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (details.platforms.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    details.platforms.forEach { platform ->
                        Icon(
                            painter = painterResource(platform.iconRes),
                            contentDescription = platform.name,
                        )
                    }
                }
            }
            if (details.isWebsiteVisible) {
                Text(
                    text = "Official Website",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable(onClick = onWebsiteClicked),
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        IconButton(onClick = onBookmarkClicked) {
            Icon(
                painter = painterResource(
                    if (state.isBookmarked) {
                        GameListPresentationR.drawable.ic_bookmark_filled_24
                    } else {
                        GameListPresentationR.drawable.ic_bookmark_24
                    },
                ),
                contentDescription = if (state.isBookmarked) "Remove Bookmark" else "Add Bookmark",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
internal fun ScreenshotsSection(
    screenshots: LazyPagingItems<Screenshot>,
    isLoading: Boolean,
    onScreenshotClicked: (String) -> Unit,
) {
    SectionHeader(title = "Screenshots")
    if (isLoading) {
        SectionThumbnailLoadingRow()
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(screenshots.itemSnapshotList.items) { screenshot ->
                Card(
                    modifier = Modifier.clickable { onScreenshotClicked(screenshot.imageUrl) },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    DynamicAsyncImage(
                        modifier = Modifier.size(192.dp, 108.dp),
                        imageUrl = screenshot.imageUrl,
                    )
                }
            }
        }
    }
}

@Composable
internal fun MoviesSection(
    movies: LazyPagingItems<Movie>,
    isLoading: Boolean,
    onMovieClicked: (String) -> Unit,
) {
    SectionHeader(title = "Movies")
    if (isLoading) {
        SectionThumbnailLoadingRow()
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(movies.itemSnapshotList.items) { movie ->
                Card(
                    modifier = Modifier
                        .size(width = 192.dp, height = 108.dp)
                        .clickable { onMovieClicked(movie.videoUrl) },
                    colors = CardDefaults.cardColors(containerColor = detailsCardColor()),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        DynamicAsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            imageUrl = movie.previewUrl.orEmpty(),
                        )
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = movie.name.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = supportingTextColor(),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SeriesSection(
    series: LazyPagingItems<Game>,
    isLoading: Boolean,
    onSeriesClicked: (Int) -> Unit,
    mapPlatforms: (Set<GamePlatform>) -> Set<PlatformUI>,
) {
    SectionHeader(title = "Series games")
    if (isLoading) {
        SeriesLoadingRow()
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(series.itemSnapshotList.items) { game ->
                SeriesGameCard(
                    game = game,
                    onClick = { onSeriesClicked(game.id) },
                    platforms = mapPlatforms(game.platforms),
                )
            }
        }
    }
}

@Composable
private fun SeriesGameCard(
    game: Game,
    onClick: () -> Unit,
    platforms: Set<PlatformUI>,
) {
    GameCard(
        modifier = Modifier.width(160.dp),
        gameData = GameCardUI(
            id = game.id.toString(),
            title = game.title,
            imageUrl = game.imageUrl,
            releaseDate = game.releaseDate?.toString().orEmpty(),
            platforms = platforms,
            rating = ChipUI(text = "${"%.1f".format(game.rating)}", isSelected = false),
            isBookmarked = false,
        ),
        onBookmarkClick = {},
        onCardClicked = onClick,
    )
}

@Composable
private fun SectionHeader(title: String) {
    HorizontalDivider(color = if (isSystemInDarkTheme()) Gray26 else Gray92)
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 17.dp, bottom = 12.dp),
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun supportingTextColor() = if (isSystemInDarkTheme()) Gray70 else Gray45

@Composable
private fun detailsCardColor() = if (isSystemInDarkTheme()) Gray12 else Gray98
