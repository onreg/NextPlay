package io.github.onreg.feature.game.details.impl.pane.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import io.github.onreg.core.ui.components.chip.Chip
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.components.image.DynamicAsyncImage
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData

@Composable
internal fun BannerComponent(
    modifier: Modifier = Modifier,
    rating: ChipUI,
    image: String,
) {
    Box(modifier = modifier) {
        DynamicAsyncImage(
            modifier = Modifier
                .aspectRatio(2f)
                .clip(MaterialTheme.shapes.small),
            imageUrl = image,
        )
        Chip(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = Spacing.sm),
            chipUI = rating,
        )
    }
}

@ThemePreview
@Composable
private fun BannerComponentPreview() {
    val details = GameDetailsTestData.readyState.details
    NextPlayTheme {
        BannerComponent(
            modifier = Modifier.fillMaxWidth(),
            rating = details.rating,
            image = details.image,
        )
    }
}
