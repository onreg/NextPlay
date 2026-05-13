package io.github.onreg.ui.platform.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.github.onreg.core.ui.R
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.IconsSize
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.ui.platform.model.PlatformUI

@Composable
public fun PlatformsComponent(
    modifier: Modifier = Modifier,
    platforms: Set<PlatformUI>,
) {
    FlowRow(
        modifier = modifier,
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

@ThemePreview
@Composable
private fun PlatformsComponentPreview() {
    NextPlayTheme {
        Surface {
            PlatformsComponent(
                platforms = setOf(
                    PlatformUI(
                        name = "PC",
                        iconRes = R.drawable.ic_controller_24,
                    ),
                    PlatformUI(
                        name = "PlayStation",
                        iconRes = R.drawable.ic_controller_24,
                    ),
                    PlatformUI(
                        name = "Xbox",
                        iconRes = R.drawable.ic_controller_24,
                    ),
                ),
            )
        }
    }
}
