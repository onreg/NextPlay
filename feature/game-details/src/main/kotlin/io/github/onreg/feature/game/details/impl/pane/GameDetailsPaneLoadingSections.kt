package io.github.onreg.feature.game.details.impl.pane

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.onreg.core.ui.animation.shimmer

private const val LOADING_ITEM_COUNT: Int = 3

@Composable
internal fun SectionThumbnailLoadingRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(LOADING_ITEM_COUNT) {
            Card(shape = MaterialTheme.shapes.medium) {
                Box(
                    modifier = Modifier
                        .size(width = 288.dp, height = 162.dp)
                        .shimmer(288.dp),
                )
            }
        }
    }
}

@Composable
internal fun SeriesLoadingRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(LOADING_ITEM_COUNT) {
            Card(shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.size(width = 128.dp, height = 262.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .shimmer(128.dp),
                    )
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .size(height = 36.dp, width = 96.dp)
                            .shimmer(96.dp),
                    )
                }
            }
        }
    }
}
