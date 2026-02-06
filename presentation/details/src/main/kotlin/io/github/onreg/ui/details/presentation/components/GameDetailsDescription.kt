package io.github.onreg.ui.details.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.text.HtmlCompat
import io.github.onreg.ui.details.presentation.R

private const val COLLAPSED_LINES = 5

@Composable
public fun GameDetailsDescription(
    modifier: Modifier = Modifier,
    descriptionHtml: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    val description = HtmlCompat
        .fromHtml(
            descriptionHtml,
            HtmlCompat.FROM_HTML_MODE_COMPACT,
        ).toString()
    Text(
        modifier = modifier,
        text = description,
        maxLines = if (isExpanded) Int.MAX_VALUE else COLLAPSED_LINES,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium,
    )
    TextButton(onClick = onToggle) {
        Text(
            text = stringResource(
                if (isExpanded) R.string.details_read_less else R.string.details_read_more,
            ),
        )
    }
}
