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
            appHeaderUI.navigationItemResId?.let {
                IconButton(onClick = onNavigationClicked) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_24),
                        contentDescription = null
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
            appHeaderUI.menuItemResIds?.forEachIndexed { index, menuItemResId ->
                IconButton(
                    onClick = {
                        onMenuItemClicked[index].invoke()
                    }
                ) {
                    Icon(
                        painter = painterResource(menuItemResId),
                        contentDescription = null
                    )
                }
            }
        }
    )
}

@ThemePreview
@Composable
private fun AppHeaderPreview() {
    NextPlayTheme {
        AppHeader(
            appHeaderUI = AppHeaderUI(
                titleResId = R.string.preview_test_15,
                navigationItemResId = R.drawable.ic_back_24,
                menuItemResIds = listOf(R.drawable.ic_share_24)
            )
        )
    }
}