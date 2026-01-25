package io.github.onreg.core.ui.components.content.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.github.onreg.core.ui.R
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.IconsSize
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing

@Composable
public fun ContentInfo(
    modifier: Modifier = Modifier,
    contentInfoUI: ContentInfoUI,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        Icon(
            painter = painterResource(contentInfoUI.iconRes),
            contentDescription = null,
            modifier = Modifier.size(IconsSize.xxl),
        )
        Text(
            text = stringResource(contentInfoUI.titleResId),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(contentInfoUI.descriptionResId),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@ThemePreview
@Composable
private fun ContentInfoPreview() {
    NextPlayTheme {
        Surface {
            ContentInfo(
                contentInfoUI = ContentInfoUI(
                    iconRes = R.drawable.ic_controller_24,
                    titleResId = R.string.preview_text,
                    descriptionResId = R.string.preview_text_long,
                ),
            )
        }
    }
}
