package io.github.onreg.core.ui.components.header

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.github.onreg.core.ui.R
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.NextPlayTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AppHeader(
    modifier: Modifier = Modifier,
    appHeaderUI: AppHeaderUI,
    onNavigationClicked: () -> Unit = {},
    vararg onMenuItemClicked: (() -> Unit) = emptyArray(),
) {
    CenterAlignedTopAppBar(
        modifier = modifier.fillMaxWidth(),
        navigationIcon = {
            appHeaderUI.navigationItem?.let { menuItem ->
                IconButton(onClick = onNavigationClicked) {
                    Icon(
                        painter = painterResource(menuItem.iconResId),
                        contentDescription = stringResource(menuItem.contentDescriptionResId),
                    )
                }
            }
        },
        title = {
            Text(
                text = stringResource(appHeaderUI.titleResId),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        actions = {
            appHeaderUI.menuItems?.forEachIndexed { index, menuItem ->
                IconButton(
                    onClick = {
                        onMenuItemClicked[index].invoke()
                    },
                ) {
                    Icon(
                        painter = painterResource(menuItem.iconResId),
                        contentDescription = stringResource(menuItem.contentDescriptionResId),
                    )
                }
            }
        },
    )
}

@ThemePreview
@Composable
private fun AppHeaderPreview() {
    NextPlayTheme {
        AppHeader(
            appHeaderUI = AppHeaderUI(
                titleResId = R.string.preview_text,
                navigationItem = AppHeaderMenu(
                    iconResId = R.drawable.ic_back_24,
                    contentDescriptionResId = R.string.preview_text,
                ),
                menuItems = listOf(
                    AppHeaderMenu(
                        iconResId = R.drawable.ic_share_24,
                        contentDescriptionResId = R.string.preview_text,
                    ),
                ),
            ),
        )
    }
}
