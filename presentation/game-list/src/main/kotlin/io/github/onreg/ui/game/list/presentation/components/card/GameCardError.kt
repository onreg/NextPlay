package io.github.onreg.ui.game.list.presentation.components.card

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.onreg.core.ui.components.content.error.ContentError
import io.github.onreg.core.ui.components.content.error.ContentErrorUI
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.runtime.paging.ErrorType
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.ui.game.list.presentation.R
import io.github.onreg.ui.game.list.presentation.components.list.test.GameListTestData
import io.github.onreg.core.ui.R as CoreUiR

@Composable
public fun GameCardError(
    modifier: Modifier = Modifier,
    errorType: ErrorType,
    onRetry: () -> Unit = {},
) {
    val (iconResId, descriptionResId) = when (errorType) {
        ErrorType.NETWORK -> {
            CoreUiR.drawable.ic_wifi_off_24 to
                CoreUiR.string.error_network_message
        }

        ErrorType.OTHER -> {
            CoreUiR.drawable.ic_controller_off_24 to
                CoreUiR.string.error_message
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        ContentError(
            contentErrorUI = ContentErrorUI(
                iconRes = iconResId,
                titleResId = R.string.games_error_title,
                descriptionResId = descriptionResId,
                actionLabelResId = R.string.retry,
            ),
            onActionClick = onRetry,
        )
    }
}

@ThemePreview
@Composable
private fun GameCardNetworkErrorPreview() {
    NextPlayTheme {
        Surface {
            GameCardError(errorType = GameListTestData.networkErrorType)
        }
    }
}

@ThemePreview
@Composable
private fun GameCardErrorPreview() {
    NextPlayTheme {
        Surface {
            GameCardError(errorType = GameListTestData.errorType)
        }
    }
}
