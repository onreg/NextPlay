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
import androidx.compose.ui.unit.Dp
import io.github.onreg.core.ui.animation.shimmer
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.IconsSize
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing

private const val FIRST_BODY_LINE_WIDTH_FRACTION = 0.72f
private const val SECOND_BODY_LINE_WIDTH_FRACTION = 0.45f
private const val PLATFORM_ICON_PLACEHOLDER_COUNT = 4

@Composable
public fun GameCardLoading(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val titleHeight = with(density) {
            MaterialTheme.typography.titleMedium.fontSize
                .toDp()
        }
        val bodyHeight = with(density) {
            MaterialTheme.typography.bodyMedium.fontSize
                .toDp()
        }

        GameCardLoadingContent(
            width = maxWidth,
            titleHeight = titleHeight,
            bodyHeight = bodyHeight,
        )
    }
}

@Composable
private fun GameCardLoadingContent(
    width: Dp,
    titleHeight: Dp,
    bodyHeight: Dp,
) {
    ElevatedCard {
        val shimmerModifier = Modifier.shimmer(width)
        Column(modifier = Modifier.fillMaxWidth()) {
            ShimmerSurface(
                modifier = Modifier
                    .aspectRatio(2f)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small),
                shimmerModifier = shimmerModifier,
            )
            Column(modifier = Modifier.padding(Spacing.lg)) {
                ShimmerSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(titleHeight)
                        .clip(MaterialTheme.shapes.small),
                    shimmerModifier = shimmerModifier,
                )

                ShimmerSurface(
                    modifier = Modifier
                        .padding(top = Spacing.sm)
                        .fillMaxWidth(FIRST_BODY_LINE_WIDTH_FRACTION)
                        .height(bodyHeight)
                        .clip(MaterialTheme.shapes.small),
                    shimmerModifier = shimmerModifier,
                )

                ShimmerSurface(
                    modifier = Modifier
                        .padding(top = Spacing.sm)
                        .fillMaxWidth(SECOND_BODY_LINE_WIDTH_FRACTION)
                        .height(bodyHeight)
                        .clip(MaterialTheme.shapes.small),
                    shimmerModifier = shimmerModifier,
                )

                LoadingPlatforms(
                    modifier = Modifier.padding(top = Spacing.sm),
                    shimmerModifier = shimmerModifier,
                )
            }
        }
    }
}

@Composable
private fun ShimmerSurface(
    modifier: Modifier,
    shimmerModifier: Modifier,
) {
    Box(modifier = modifier.then(shimmerModifier))
}

@Composable
private fun LoadingPlatforms(
    modifier: Modifier,
    shimmerModifier: Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        repeat(PLATFORM_ICON_PLACEHOLDER_COUNT) {
            ShimmerSurface(
                modifier = Modifier
                    .size(IconsSize.sm)
                    .clip(MaterialTheme.shapes.small),
                shimmerModifier = shimmerModifier,
            )
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
