package io.github.onreg.ui.game.list.presentation.components.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.onreg.core.ui.components.button.IconToggleButton
import io.github.onreg.core.ui.components.chip.Chip
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.components.image.DynamicAsyncImage
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.ControlsSize
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.game.list.presentation.components.card.test.GameCardTestTags.GAME_CARD_ADD_BOOKMARK_BUTTON
import io.github.onreg.ui.game.list.presentation.components.list.test.GameListTestData
import io.github.onreg.ui.platform.component.PlatformsComponent
import io.github.onreg.ui.platform.model.PlatformUI
import io.github.onreg.core.ui.R as CoreUiR

@Composable
public fun GameCard(
    modifier: Modifier = Modifier,
    gameData: GameCardUI,
    onBookmarkClick: () -> Unit = {},
    onCardClicked: () -> Unit = {},
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onCardClicked,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            GameCardImage(
                imageUrl = gameData.imageUrl,
                rating = gameData.rating,
            )
            GameCardDetails(
                title = gameData.title,
                releaseDate = gameData.releaseDate,
                platforms = gameData.platforms,
                isBookmarked = gameData.isBookmarked,
                onBookmarkClick = onBookmarkClick,
            )
        }
    }
}

@Composable
private fun GameCardImage(
    imageUrl: String,
    rating: ChipUI,
) {
    Box {
        DynamicAsyncImage(
            modifier = Modifier
                .aspectRatio(2f)
                .clip(MaterialTheme.shapes.small),
            imageUrl = imageUrl,
        )

        Chip(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = Spacing.sm),
            chipUI = rating,
        )
    }
}

@Composable
private fun GameCardDetails(
    title: String,
    releaseDate: String,
    platforms: Set<PlatformUI>,
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconToggleButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .requiredSize(ControlsSize.IconButton)
                .testTag(GAME_CARD_ADD_BOOKMARK_BUTTON),
            checked = isBookmarked,
            checkedIconRes = CoreUiR.drawable.ic_bookmark_filled_24,
            uncheckedIconRes = CoreUiR.drawable.ic_bookmark_24,
            checkedContentDescriptionRes = CoreUiR.string.bookmark_remove,
            uncheckedContentDescriptionRes = CoreUiR.string.bookmark_add,
            onClicked = { onBookmarkClick() },
        )

        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = safeTitleEndPadding()),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                modifier = Modifier.padding(top = Spacing.sm),
                text = releaseDate,
                style = MaterialTheme.typography.bodyMedium,
            )

            PlatformsComponent(
                modifier = Modifier.padding(top = Spacing.sm),
                platforms = platforms,
            )
        }
    }
}

@Composable
private fun safeTitleEndPadding() = (ControlsSize.IconButton - Spacing.sm).coerceAtLeast(0.dp)

@Composable
@ThemePreview
private fun GameListCardPreview() {
    val gameData = GameListTestData.generateGameCards(1).first()

    NextPlayTheme {
        GameCard(
            modifier = Modifier,
            gameData = gameData,
        )
    }
}
