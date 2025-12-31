package io.github.onreg.ui.game.presentation.components.card

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.onreg.core.ui.R as CoreUiR
import io.github.onreg.core.ui.components.content.error.ContentError
import io.github.onreg.core.ui.components.content.error.ContentErrorUI
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.ui.game.presentation.R

@Composable
public fun GameCardError(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {}
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        ContentError(
            contentErrorUI = ContentErrorUI(
                iconRes = CoreUiR.drawable.ic_controller_off_24,
                titleResId = R.string.games_error_title,
                descriptionResId = R.string.games_error_description,
                actionLabelResId = R.string.retry,
            ),
            onActionClick = onRetry
        )
    }
}

@ThemePreview
@Composable
private fun GameCardErrorPreview() {
    NextPlayTheme {
        GameCardError()
    }
}
