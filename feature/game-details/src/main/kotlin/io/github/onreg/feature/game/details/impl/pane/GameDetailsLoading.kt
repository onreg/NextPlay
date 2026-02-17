package io.github.onreg.feature.game.details.impl.pane

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.onreg.core.ui.animation.shimmer
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.ControlsSize
import io.github.onreg.core.ui.theme.IconsSize
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestTags

private const val PLACEHOLDER_WIDTH: Float = 0.5f
private const val PLACEHOLDER_WIDTH_SMALL: Float = 0.3f

private const val PLATFORM_COUNT: Int = 3
private const val DESCRIPTION_LINE_COUNT: Int = 4
private const val COMPANY_PLACEHOLDER_COUNT: Int = 2
private const val MEDIA_SECTION_COUNT: Int = 3
private const val MEDIA_SECTION_ITEMS_COUNT: Int = 3

@Composable
internal fun GameDetailsLoading(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(GameDetailsTestTags.LOADING)
            .verticalScroll(rememberScrollState()),
    ) {
        LoadingBannerSection()
        LoadingDetailsSection()

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
        LoadingDescriptionSection(Modifier.padding(vertical = Spacing.lg))

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
        LoadingDevelopersAndPublishersSection(Modifier.padding(vertical = Spacing.lg))

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
        LoadingMediaSection(Modifier.padding(vertical = Spacing.lg))

        repeat(MEDIA_SECTION_COUNT) {
            LoadingMediaSection(Modifier.padding(vertical = Spacing.lg))
        }
    }
}

@Composable
internal fun LoadingMediaSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        SectionTitlePlaceholder(modifier = Modifier.padding(horizontal = Spacing.lg))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            items(MEDIA_SECTION_ITEMS_COUNT) {
                Card(shape = MaterialTheme.shapes.medium) {
                    Box(
                        modifier = Modifier
                            .size(width = 288.dp, height = 162.dp)
                            .shimmer(),
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingBannerSection() {
    ShimmerSurface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(BANNER_ASPECT_RATIO),
        shape = MaterialTheme.shapes.small,
    )
}

@Composable
private fun LoadingDetailsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            ShimmerSurface(
                modifier = Modifier
                    .fillMaxWidth(PLACEHOLDER_WIDTH)
                    .height(IconsSize.sm),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                repeat(PLATFORM_COUNT) {
                    ShimmerSurface(modifier = Modifier.size(IconsSize.sm))
                }
            }
            ShimmerSurface(
                modifier = Modifier
                    .fillMaxWidth(PLACEHOLDER_WIDTH_SMALL)
                    .height(IconsSize.sm),
            )
        }
    }
}

@Composable
private fun LoadingDescriptionSection(modifier: Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        SectionTitlePlaceholder()
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            repeat(DESCRIPTION_LINE_COUNT) {
                ShimmerSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IconsSize.sm),
                )
            }
        }
    }
}

@Composable
private fun LoadingDevelopersAndPublishersSection(modifier: Modifier) {
    Column(
        modifier = modifier.padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        SectionTitlePlaceholder()
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            repeat(COMPANY_PLACEHOLDER_COUNT) {
                LoadingCompanyRow()
            }
        }
    }
}

@Composable
private fun LoadingCompanyRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        ShimmerSurface(
            modifier = Modifier.size(ControlsSize.IconButton),
            shape = RoundedCornerShape(Spacing.md),
        )
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            ShimmerSurface(
                modifier = Modifier
                    .fillMaxWidth(PLACEHOLDER_WIDTH)
                    .height(IconsSize.sm),
            )
            ShimmerSurface(
                modifier = Modifier
                    .fillMaxWidth(PLACEHOLDER_WIDTH_SMALL)
                    .height(Spacing.md),
            )
        }
    }
}

@Composable
private fun SectionTitlePlaceholder(modifier: Modifier = Modifier) {
    ShimmerSurface(
        modifier = modifier
            .fillMaxWidth(PLACEHOLDER_WIDTH)
            .height(Spacing.xl),
    )
}

@Composable
private fun ShimmerSurface(
    modifier: Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmer(),
    )
}

@ThemePreview
@Composable
private fun GameDetailsLoadingPreview() {
    NextPlayTheme {
        GameDetailsLoading(modifier = Modifier.fillMaxSize())
    }
}
