package io.github.onreg.feature.game.details.impl.pane

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import io.github.onreg.core.ui.components.button.TextButton
import io.github.onreg.core.ui.components.image.DynamicAsyncImage
import io.github.onreg.core.ui.theme.Gray12
import io.github.onreg.core.ui.theme.Gray26
import io.github.onreg.core.ui.theme.Gray45
import io.github.onreg.core.ui.theme.Gray70
import io.github.onreg.core.ui.theme.Gray92
import io.github.onreg.core.ui.theme.Gray98
import io.github.onreg.feature.game.details.impl.model.GameCompanyUi
import io.github.onreg.feature.game.details.impl.model.GameDescriptionUi
import io.github.onreg.feature.game.details.impl.model.MovieUI
import io.github.onreg.feature.game.details.impl.model.ScreenshotUI
import io.github.onreg.feature.game.details.impl.pane.component.LoadingMediaSection
import io.github.onreg.ui.game.list.presentation.components.card.GameCard
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.core.ui.R as CoreUiR
import io.github.onreg.feature.game.details.impl.R as FeatureR




@Composable
internal fun MoviesSection(
    movies: LazyPagingItems<MovieUI>,
    isLoading: Boolean,
    onMovieClicked: (String) -> Unit,
) {
    SectionHeader(title = stringResource(FeatureR.string.movies_section_title))
    if (isLoading) {
        LoadingMediaSection()
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(movies.itemSnapshotList.items) { movie ->
                Card(
                    modifier = Modifier
                        .size(width = 288.dp, height = 162.dp)
                        .clickable { onMovieClicked(movie.videoUrl) },
                    colors = CardDefaults.cardColors(containerColor = detailsCardColor()),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        DynamicAsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            imageUrl = movie.previewUrl,
                        )
                        Icon(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(40.dp),
                            painter = painterResource(CoreUiR.drawable.ic_play_24),
                            contentDescription = stringResource(FeatureR.string.play_video),
                            tint = Color.White,
                        )
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = movie.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = supportingTextColor(),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SeriesSection(
    series: LazyPagingItems<GameCardUI>,
    isLoading: Boolean,
    onSeriesClicked: (Int) -> Unit,
) {
    SectionHeader(title = stringResource(FeatureR.string.series_games_section_title))
    if (isLoading) {
        LoadingMediaSection()
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(series.itemSnapshotList.items) { game ->
                SeriesGameCard(
                    game = game,
                    onClick = { onSeriesClicked(game.id) },
                )
            }
        }
    }
}

@Composable
private fun SeriesGameCard(
    game: GameCardUI,
    onClick: () -> Unit,
) {
    GameCard(
        modifier = Modifier.width(240.dp),
        gameData = game,
        onBookmarkClick = {},
        onCardClicked = onClick,
    )
}

@Composable
private fun SectionHeader(title: String) {
    HorizontalDivider(color = if (isSystemInDarkTheme()) Gray26 else Gray92)
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 17.dp, bottom = 12.dp),
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun supportingTextColor() = if (isSystemInDarkTheme()) Gray70 else Gray45

@Composable
private fun detailsCardColor() = if (isSystemInDarkTheme()) Gray12 else Gray98
