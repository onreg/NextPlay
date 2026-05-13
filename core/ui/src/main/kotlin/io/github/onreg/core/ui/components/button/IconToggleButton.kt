package io.github.onreg.core.ui.components.button

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton as MaterialIconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.github.onreg.core.ui.R
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.NextPlayTheme

@Composable
public fun IconToggleButton(
    modifier: Modifier = Modifier,
    checked: Boolean,
    @DrawableRes checkedIconRes: Int,
    @DrawableRes uncheckedIconRes: Int,
    @StringRes checkedContentDescriptionRes: Int,
    @StringRes uncheckedContentDescriptionRes: Int,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClicked: (Boolean) -> Unit,
) {
    MaterialIconToggleButton(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onClicked,
    ) {
        Icon(
            painter = painterResource(
                if (checked) checkedIconRes else uncheckedIconRes,
            ),
            contentDescription = stringResource(
                if (checked) checkedContentDescriptionRes else uncheckedContentDescriptionRes,
            ),
            tint = tint,
        )
    }
}

@ThemePreview
@Composable
private fun IconToggleButtonPreview() {
    NextPlayTheme {
        IconToggleButton(
            checked = false,
            checkedIconRes = R.drawable.ic_bookmark_filled_24,
            uncheckedIconRes = R.drawable.ic_bookmark_24,
            checkedContentDescriptionRes = R.string.bookmark_remove,
            uncheckedContentDescriptionRes = R.string.bookmark_add,
            onClicked = {},
        )
    }
}

@ThemePreview
@Composable
private fun IconToggleButtonCheckedPreview() {
    NextPlayTheme {
        IconToggleButton(
            checked = true,
            checkedIconRes = R.drawable.ic_bookmark_filled_24,
            uncheckedIconRes = R.drawable.ic_bookmark_24,
            checkedContentDescriptionRes = R.string.bookmark_remove,
            uncheckedContentDescriptionRes = R.string.bookmark_add,
            onClicked = {},
        )
    }
}
