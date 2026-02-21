package io.github.onreg.core.ui.components.button

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.NextPlayTheme
import androidx.compose.material3.TextButton as MaterialTextButton

@Composable
public fun TextButton(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = LocalTextStyle.current,
    onClick: () -> Unit,
) {
    MaterialTextButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(
            text = text,
            style = style,
        )
    }
}

@ThemePreview
@Composable
private fun TextButtonPreview() {
    NextPlayTheme {
        TextButton(
            text = "Preview action",
            onClick = {},
        )
    }
}
