package io.github.onreg.feature.game.details.impl.pane

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import io.github.onreg.core.ui.components.button.TextButton
import io.github.onreg.core.ui.theme.IconsSize
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.ui.platform.model.PlatformUI

@Composable
internal fun ReleaseDateRow(
    releaseDate: String?,
    supportingTextColor: Color,
) {
    releaseDate?.takeIf { it.isNotBlank() }?.let { value ->
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = value,
                color = supportingTextColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
internal fun PlatformsRow(platforms: Set<PlatformUI>) {
    if (platforms.isEmpty()) return

    Row(modifier = Modifier.fillMaxWidth()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            platforms.forEach { platform ->
                Icon(
                    modifier = Modifier.size(IconsSize.sm),
                    painter = painterResource(platform.iconRes),
                    contentDescription = platform.name,
                )
            }
        }
    }
}

@Composable
internal fun OfficialWebsiteRow(
    isWebsiteVisible: Boolean,
    onWebsiteClicked: () -> Unit,
) {
    if (!isWebsiteVisible) return

    Row(modifier = Modifier.fillMaxWidth()) {
        TextButton(
            text = "Official Website",
            onClick = onWebsiteClicked,
        )
    }
}
