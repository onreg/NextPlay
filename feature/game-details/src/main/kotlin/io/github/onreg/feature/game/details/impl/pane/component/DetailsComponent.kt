package io.github.onreg.feature.game.details.impl.pane.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.onreg.core.ui.components.button.TextButton
import io.github.onreg.core.ui.components.button.IconToggleButton
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData
import io.github.onreg.ui.platform.component.PlatformsComponent
import io.github.onreg.ui.platform.model.PlatformUI
import io.github.onreg.core.ui.R as CoreUiR
import io.github.onreg.feature.game.details.impl.R as DetailsR

@Composable
internal fun DetailsComponent(
    modifier: Modifier = Modifier,
    releaseDate: String,
    platforms: Set<PlatformUI>,
    isBookmarked: Boolean,
    onWebsiteClicked: () -> Unit,
    onBookmarkClicked: () -> Unit,
) {
    Row(modifier = modifier) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier.padding(start = Spacing.lg),
                text = releaseDate,
                style = MaterialTheme.typography.bodyMedium,
            )

            PlatformsComponent(
                modifier = Modifier.padding(start = Spacing.lg, top = Spacing.sm),
                platforms = platforms,
            )

            TextButton(
                modifier = Modifier.padding(start = Spacing.xs),
                text = stringResource(DetailsR.string.official_website),
                onClick = onWebsiteClicked,
            )
        }
        IconToggleButton(
            checked = isBookmarked,
            checkedIconRes = CoreUiR.drawable.ic_bookmark_filled_24,
            uncheckedIconRes = CoreUiR.drawable.ic_bookmark_24,
            checkedContentDescriptionRes = CoreUiR.string.bookmark_remove,
            uncheckedContentDescriptionRes = CoreUiR.string.bookmark_add,
            onClicked = { onBookmarkClicked() },
        )
    }
}

@ThemePreview
@Composable
private fun DetailsComponentPreview() {
    val details = GameDetailsTestData.readyState.details
    NextPlayTheme {
        Surface {
            DetailsComponent(
                releaseDate = details.releaseDate,
                platforms = details.platforms,
                isBookmarked = details.isBookmarked,
                onWebsiteClicked = {},
                onBookmarkClicked = {},
            )
        }
    }
}
