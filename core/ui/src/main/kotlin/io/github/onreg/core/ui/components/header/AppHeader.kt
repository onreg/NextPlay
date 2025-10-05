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
import io.github.onreg.core.ui.preview.DevicePreviews
import io.github.onreg.core.ui.theme.NextPlayTheme

/**
 * This component uses Material 3's [CenterAlignedTopAppBar]
 *
 * #### Colors Used:
 * - **Container**: Uses Material Theme's surface color
 * - **Title**: Uses Material Theme's onSurface color
 * - **Navigation icon**: Uses Material Theme's onSurface color
 * - **Menu items**: Uses Material Theme's onSurfaceVariant color
 *
 * #### Typography:
 * - **Title**: Material Theme's titleMedium style
 *
 * @param modifier Modifier to be applied to the component
 * @param appHeaderState Configuration state containing title and navigation/menu items
 * @param onNavigationClicked Callback invoked when the navigation icon is clicked
 * @param onMenuItemClicked Variable number of callbacks for menu item clicks, indexed by position
 *
 * @see AppHeaderState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AppHeader(
    modifier: Modifier = Modifier,
    appHeaderState: AppHeaderState,
    onNavigationClicked: () -> Unit = {},
    vararg onMenuItemClicked: (() -> Unit) = emptyArray(),
) {
    CenterAlignedTopAppBar(
        modifier = modifier.fillMaxWidth(),
        navigationIcon = {
            appHeaderState.navigationItemResId?.let {
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
                text = stringResource(appHeaderState.titleResId),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        actions = {
            appHeaderState.menuItemResIds?.forEachIndexed { index, menuItemResId ->
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

@DevicePreviews
@Composable
private fun AppHeaderPreview() {
    NextPlayTheme {
        AppHeader(
            appHeaderState = AppHeaderState(
                titleResId = R.string.preview_test_15,
                navigationItemResId = R.drawable.ic_back_24,
                menuItemResIds = listOf(R.drawable.ic_share_24)
            )
        )
    }
}