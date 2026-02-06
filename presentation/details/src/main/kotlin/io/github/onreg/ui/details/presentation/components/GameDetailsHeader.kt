package io.github.onreg.ui.details.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.github.onreg.ui.details.presentation.R
import io.github.onreg.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun GameDetailsHeader(
    modifier: Modifier = Modifier,
    title: String,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(CoreUiR.drawable.ic_back_24),
                    contentDescription = stringResource(R.string.details_back),
                )
            }
        },
    )
}
