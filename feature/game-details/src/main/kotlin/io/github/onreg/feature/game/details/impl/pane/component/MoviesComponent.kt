package io.github.onreg.feature.game.details.impl.pane.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.onreg.core.ui.components.image.DynamicAsyncImage
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.runtime.paging.PagedListState
import io.github.onreg.core.ui.runtime.paging.resolveState
import io.github.onreg.core.ui.theme.IconsSize
import io.github.onreg.core.ui.theme.MediaSectionTokens
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.feature.game.details.impl.R
import io.github.onreg.feature.game.details.impl.model.MovieUI
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestTags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import io.github.onreg.core.ui.R as CoreUiR

@Composable
@Suppress("LongMethod")
internal fun MoviesComponent(
    modifier: Modifier = Modifier,
    pagingState: PagedListState<MovieUI>,
    onMovieClicked: (String) -> Unit,
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
                    text = stringResource(R.string.movies_section_title),
                    modifier = Modifier.padding(horizontal = Spacing.lg),
                    style = MaterialTheme.typography.titleMedium,
                )
                LazyRow(
                    contentPadding = PaddingValues(Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    val lazyPagingItems = pagingState.items
                    items(
                        count = lazyPagingItems.itemCount,
                        key = lazyPagingItems.itemKey { movie ->
                            movie.id
                        },
                    ) { index ->
                        val movie = lazyPagingItems[index] ?: return@items
                        Card(
                            onClick = { onMovieClicked(movie.videoUrl) },
                            modifier = Modifier
                                .width(MediaSectionTokens.itemWidthPhone)
                                .aspectRatio(MediaSectionTokens.aspectRatio16x9)
                                .testTag(GameDetailsTestTags.movieItemTag(movie.id)),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                DynamicAsyncImage(
                                    modifier = Modifier.fillMaxSize(),
                                    imageUrl = movie.previewUrl,
                                )
                                Icon(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(Spacing.sm)
                                        .size(IconsSize.xl),
                                    painter = painterResource(CoreUiR.drawable.ic_play_24),
                                    contentDescription = stringResource(R.string.play_video),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
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
    MoviesComponentPreview(
        movies = GameDetailsTestData.movies,
    )
}

@Composable
@ThemePreview
private fun LoadingPreview() {
    MoviesComponentPreview(
        movies = loadingMovies(),
    )
}

@Composable
private fun MoviesComponentPreview(
    movies: Flow<PagingData<MovieUI>>,
) {
    NextPlayTheme {
        val moviesItems = movies.collectAsLazyPagingItems()
        Surface {
            MoviesComponent(
                pagingState = moviesItems.resolveState(),
                onMovieClicked = {},
            )
        }
    }
}

private fun loadingMovies(): Flow<PagingData<MovieUI>> = flowOf(
    PagingData.empty(
        sourceLoadStates = LoadStates(
            refresh = LoadState.Loading,
            prepend = LoadState.NotLoading(false),
            append = LoadState.NotLoading(false),
        ),
    ),
)
