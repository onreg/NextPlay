package io.github.onreg.ui.game.presentation.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import io.github.onreg.core.ui.animation.shimmer
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.IconsSize
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing

@Composable
public fun GameCardLoading(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val cardWidthPx = with(density) { maxWidth.toPx() }
        val titleHeight = with(density) {
            MaterialTheme.typography.titleMedium.fontSize
                .toDp()
        }
        val bodyHeight = with(density) {
            MaterialTheme.typography.bodyMedium.fontSize
                .toDp()
        }
        val shimmerModifier = Modifier.shimmer(cardWidthPx)
        ElevatedCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .aspectRatio(2f)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .then(shimmerModifier),
                )
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(titleHeight)
                            .clip(MaterialTheme.shapes.small)
                            .then(shimmerModifier),
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = Spacing.sm)
                            .fillMaxWidth(0.72f)
                            .height(bodyHeight)
                            .clip(MaterialTheme.shapes.small)
                            .then(shimmerModifier),
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = Spacing.sm)
                            .fillMaxWidth(0.45f)
                            .height(bodyHeight)
                            .clip(MaterialTheme.shapes.small)
                            .then(shimmerModifier),
                    )

                    FlowRow(
                        modifier = Modifier.padding(top = Spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .size(IconsSize.sm)
                                    .clip(MaterialTheme.shapes.small)
                                    .then(shimmerModifier),
                            )
                        }
                    }
                }
            }
        }
    }
}

@ThemePreview
@Composable
private fun GameCardLoadingPreview() {
    NextPlayTheme {
        GameCardLoading()
    }
}
