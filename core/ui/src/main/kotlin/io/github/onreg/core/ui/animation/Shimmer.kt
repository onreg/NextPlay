package io.github.onreg.core.ui.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.NextPlayTheme

private const val TRANSITION_LABEL = "ShimmerTransition"
private const val OFFSET_ANIMATION_LABEL = "ShimmerOffsetAnimation"
private const val SHIMMER_WIDTH_RATIO = 0.2f

@Composable
public fun Modifier.shimmer(width: Dp): Modifier = background(rememberShimmerBrush(width))

@Composable
public fun rememberShimmerBrush(width: Dp): Brush {
    val widthPx = with(LocalDensity.current) { width.toPx() }
    val shimmerWidth = (widthPx * SHIMMER_WIDTH_RATIO).coerceAtLeast(1f)
    val transition = rememberInfiniteTransition(TRANSITION_LABEL)
    val offsetX = transition.animateFloat(
        initialValue = -shimmerWidth,
        targetValue = widthPx + shimmerWidth,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = OFFSET_ANIMATION_LABEL,
    )
    val baseColor = MaterialTheme.colorScheme.surfaceContainerHigh
    return Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.5f),
            baseColor.copy(alpha = 0.2f),
            baseColor.copy(alpha = 0.5f),
        ),
        start = Offset(offsetX.value - shimmerWidth, 0f),
        end = Offset(offsetX.value + shimmerWidth, shimmerWidth),
    )
}

@ThemePreview
@Composable
private fun ShimmerPreview() {
    NextPlayTheme {
        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 100.dp)
                .shimmer(200.dp),
        )
    }
}
