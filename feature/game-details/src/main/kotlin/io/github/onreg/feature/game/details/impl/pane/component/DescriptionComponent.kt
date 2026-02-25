package io.github.onreg.feature.game.details.impl.pane.component

import android.R.attr.maxLines
import android.R.attr.text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.onreg.core.ui.components.button.TextButton
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.feature.game.details.impl.R
import io.github.onreg.feature.game.details.impl.model.GameDescriptionUi
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData

private const val COLLAPSED_DESCRIPTION_MAX_LINES: Int = 6

@Composable
internal fun DescriptionComponent(
    modifier: Modifier = Modifier,
    descriptionUi: GameDescriptionUi,
    onTextOverflow: (Boolean) -> Unit,
    onToggleClicked: () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.description_section_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
            text = descriptionUi.description,
            maxLines = if (descriptionUi.isExpanded) {
                Int.MAX_VALUE
            } else {
                COLLAPSED_DESCRIPTION_MAX_LINES
            },
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { onTextOverflow(it.hasVisualOverflow) },
        )
        if (descriptionUi.descriptionToggleUi.isVisible) {
            TextButton(
                modifier = Modifier.padding(start = 4.dp),
                text = descriptionUi.descriptionToggleUi.text,
                onClick = onToggleClicked,
            )
        }
    }
}

@ThemePreview
@Composable
private fun DescriptionComponentPreview() {
    val descriptionUi = GameDetailsTestData.readyState.details.gameDescriptionUi
    NextPlayTheme {
        Surface {
            DescriptionComponent(
                descriptionUi = descriptionUi,
                onTextOverflow = {},
                onToggleClicked = {},
            )
        }
    }
}
