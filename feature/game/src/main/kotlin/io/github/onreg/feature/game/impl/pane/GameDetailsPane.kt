package io.github.onreg.feature.game.impl.pane

import android.R.attr.text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
public fun GameDetailsPane(
    gameId: String,
    modifier: Modifier = Modifier,
    goBack: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.clickable {
                goBack()
            },
            text = gameId
        )
    }
}
