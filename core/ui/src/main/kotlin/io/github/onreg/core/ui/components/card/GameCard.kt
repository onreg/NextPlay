package io.github.onreg.core.ui.components.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.onreg.core.ui.R
import io.github.onreg.core.ui.components.rating.RatingComponent
import io.github.onreg.core.ui.preview.DevicePreviews
import io.github.onreg.core.ui.theme.IconsSize
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing

/**
 * A card component that displays comprehensive game information in a horizontal layout.
 *
 * #### Colors Used:
 * - **Card Container**: Uses Material Theme's surface color with elevation
 * - **Title Text**: Uses Material Theme's onSurface color
 * - **Release Date Text**: Uses Material Theme's onSurfaceVariant color
 * - **Genre Chips**: Uses Material Theme's surface variant colors
 * - **Platform Icons**: Uses Material Theme's onSurface color
 * - **Bookmark Icon**: Uses Material Theme's primary color
 * - **Rating Component**: Uses Material Theme's primary color for background
 *
 * #### Typography:
 * - **Title**: Material Theme's titleMedium style
 * - **Release Date**: Material Theme's bodyMedium style
 * - **Genre Labels**: Material Theme's labelSmall style
 *
 * #### Shape:
 * - **Card**: Material Theme's medium shape with elevation
 * - **Cover Image**: Material Theme's small shape
 * - **Genre Chips**: Material Theme's large shape
 *
 * @param modifier The modifier to be applied to the card component
 * @param gameData The game data containing all information to display
 * @param onBookmarkClick Callback invoked when the bookmark button is clicked
 * @param onCardClicked Callback invoked when the card itself is clicked
 */
@Composable
public fun GameCard(
    modifier: Modifier = Modifier,
    gameData: GameCardUI,
    onBookmarkClick: () -> Unit = {},
    onCardClicked: () -> Unit = {}
) {
    ElevatedCard(
        onClick = onCardClicked,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.pank), // TODO: Load from imageUrl
                contentDescription = null,
                modifier = modifier
                    .size(width = 100.dp, height = 170.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop,
            )

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                GameCardHeader(
                    title = gameData.title,
                    releaseDate = gameData.releaseDate,
                    isBookmarked = gameData.isBookmarked,
                    onBookmarkClick = onBookmarkClick
                )
                GameCardGenres(genres = gameData.genres)
                GameCardFooter(
                    platforms = gameData.platforms,
                    rating = gameData.rating
                )
            }
        }
    }
}

@Composable
private fun GameCardHeader(
    modifier: Modifier = Modifier,
    title: String,
    releaseDate: String,
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        IconButton(onClick = onBookmarkClick) {
            Icon(
                painter = painterResource(
                    when (isBookmarked) {
                        true -> R.drawable.ic_bookmark_filled_24
                        false -> R.drawable.ic_bookmark_24
                    }
                ),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark"
            )
        }
    }

    Text(
        text = releaseDate,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

@Composable
private fun GameCardGenres(
    modifier: Modifier = Modifier,
    genres: List<String>
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(-(Spacing.sm))
    ) {
        genres.forEach { genre ->
            AssistChip(
                onClick = { /* TODO: Handle genre selection */ },
                label = {
                    Text(
                        text = genre,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                shape = MaterialTheme.shapes.large,
            )
        }
    }
}

@Composable
private fun GameCardFooter(
    platforms: List<Platform>,
    rating: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            platforms.forEach { platform ->
                Icon(
                    modifier = Modifier.size(IconsSize.sm),
                    painter = painterResource(platform.iconRes),
                    contentDescription = platform.name,
                )
            }
        }
        RatingComponent(text = rating)
    }
}

@Composable
@DevicePreviews
private fun GameListCardPreview() {
    NextPlayTheme {
        GameCard(
            gameData = GameCardUI(
                id = "1",
                title = "Cyberpunk 2077",
                imageUrl = "",
                releaseDate = "December 10, 2020",
                genres = listOf("Action", "RPG", "Sci-Fi", "Strategy"),
                platforms = listOf(
                    Platform("PC", R.drawable.ic_share_24),
                    Platform("PlayStation", R.drawable.ic_back_24),
                    Platform("Xbox", R.drawable.ic_share_24)
                ),
                rating = "#92",
                isBookmarked = false
            ),
            modifier = Modifier.padding(Spacing.lg)
        )
    }
}
