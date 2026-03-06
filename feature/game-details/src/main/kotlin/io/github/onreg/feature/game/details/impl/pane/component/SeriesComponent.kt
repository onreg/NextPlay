package io.github.onreg.feature.game.details.impl.pane.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.runtime.paging.PagedListState
import io.github.onreg.core.ui.runtime.paging.resolveState
import io.github.onreg.core.ui.theme.MediaSectionTokens
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.feature.game.details.impl.R
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData
import io.github.onreg.ui.game.list.presentation.components.card.GameCard
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import kotlinx.coroutines.flow.Flow

@Composable
@Suppress("LongMethod")
internal fun SeriesComponent(
    modifier: Modifier = Modifier,
    pagingState: PagedListState<GameCardUI>,
    onSeriesClicked: (Int) -> Unit,
) {
    when (pagingState) {
        PagedListState.Loading -> {
            LoadingMediaSection(modifier = modifier)
        }

        is PagedListState.Loaded -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                Text(
                    text = stringResource(R.string.series_games_section_title),
                    modifier = Modifier.padding(horizontal = Spacing.lg),
                    style = MaterialTheme.typography.titleMedium,
                )

                LazyRow(
                    contentPadding = PaddingValues(Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    val items = pagingState.items
                    items(items.itemCount) { index ->
                        val game = items[index] ?: return@items
                        GameCard(
                            modifier = Modifier
                                .width(MediaSectionTokens.itemWidthPhone),
                            gameData = game,
                            onCardClicked = { onSeriesClicked(game.id) },
                        )

                    }
                }
            }
        }

        PagedListState.Empty -> Unit

        is PagedListState.Error -> Unit
    }
}

@Composable
@ThemePreview
private fun LoadedPreview() {
    SeriesComponentPreview(
        series = GameDetailsTestData.seriesState,
    )
}

@Composable
@ThemePreview
private fun LoadingPreview() {
    SeriesComponentPreview(
        series = GameDetailsTestData.loadingSeries,
    )
}

@Composable
private fun SeriesComponentPreview(
    series: Flow<PagingData<GameCardUI>>,
) {
    NextPlayTheme {
        val seriesItems = series.collectAsLazyPagingItems()
        Surface {
            SeriesComponent(
                pagingState = seriesItems.resolveState(),
                onSeriesClicked = {},
            )
        }
    }
}
