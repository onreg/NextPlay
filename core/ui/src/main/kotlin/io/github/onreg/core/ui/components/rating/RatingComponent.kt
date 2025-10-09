package io.github.onreg.core.ui.components.rating

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.onreg.core.ui.preview.DevicePreviews
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing

/**
 * A component that displays a rating text inside a styled surface.
 *
 * #### Colors Used:
 * - **Container**: Uses Material Theme's primary color
 * - **Text**: Uses Material Theme's onPrimary color
 *
 * #### Typography:
 * - **Text**: Material Theme's labelSmall style
 *
 * #### Shape:
 * - **Surface**: Material Theme's small shape (rounded corners)
 *
 * @param modifier The modifier to be applied to the component
 * @param text The rating text to display (e.g., "#92", "8.5", "A+")
 */
@Composable
public fun RatingComponent(
    modifier: Modifier = Modifier,
    text: String,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    ) {
        Text(
            modifier = Modifier.padding(Spacing.sm),
            text = text,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
@DevicePreviews
private fun GameRatingPreview() {
    NextPlayTheme {
        RatingComponent(text = "#92")
    }
}
