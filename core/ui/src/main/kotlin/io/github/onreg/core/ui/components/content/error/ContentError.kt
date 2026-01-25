package io.github.onreg.core.ui.components.content.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.onreg.core.ui.R
import io.github.onreg.core.ui.components.content.info.ContentInfo
import io.github.onreg.core.ui.components.content.info.ContentInfoUI
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing

@Composable
public fun ContentError(
    modifier: Modifier = Modifier,
    contentErrorUI: ContentErrorUI,
    onActionClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        ContentInfo(
            contentInfoUI = ContentInfoUI(
                iconRes = contentErrorUI.iconRes,
                titleResId = contentErrorUI.titleResId,
                descriptionResId = contentErrorUI.descriptionResId,
            ),
        )
        Button(onClick = onActionClick) {
            Text(text = stringResource(contentErrorUI.actionLabelResId))
        }
    }
}

@ThemePreview
@Composable
private fun ContentErrorPreview() {
    NextPlayTheme {
        Surface {
            ContentError(
                contentErrorUI = ContentErrorUI(
                    iconRes = R.drawable.ic_controller_off_24,
                    titleResId = R.string.preview_text,
                    descriptionResId = R.string.preview_text_long,
                    actionLabelResId = R.string.preview_text,
                ),
                onActionClick = {},
            )
        }
    }
}
