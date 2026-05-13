package io.github.onreg.feature.game.details.impl.test

import androidx.paging.PagingData
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.components.header.AppHeaderMenu
import io.github.onreg.core.ui.components.header.AppHeaderUi
import io.github.onreg.core.ui.runtime.paging.emptyPagingFlow
import io.github.onreg.core.ui.runtime.paging.loadedPagingFlow
import io.github.onreg.core.ui.runtime.paging.loadingPagingFlow
import io.github.onreg.feature.game.details.impl.model.DescriptionToggleUi
import io.github.onreg.feature.game.details.impl.model.GameCompanyUi
import io.github.onreg.feature.game.details.impl.model.GameDescriptionUi
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.model.GameDetailsUi
import io.github.onreg.feature.game.details.impl.model.MovieUI
import io.github.onreg.feature.game.details.impl.model.ScreenshotUI
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.platform.model.PlatformUI
import kotlinx.coroutines.flow.Flow
import io.github.onreg.core.ui.R as CoreUiR

internal object GameDetailsTestData {
    const val readyTitle: String = "Elden Ring"

    val readyState: GameDetailsState.Ready = GameDetailsState.Ready(
        details = GameDetailsUi(
            image = "example",
            rating = ChipUI(text = "4.8", isSelected = true),
            releaseDate = "Feb 25, 2022",
            platforms = setOf(
                PlatformUI(
                    name = "PC",
                    iconRes = CoreUiR.drawable.ic_controller_24,
                ),
                PlatformUI(
                    name = "PlayStation",
                    iconRes = CoreUiR.drawable.ic_controller_24,
                ),
            ),
            website = "https://example.com/game",
            gameDescriptionUi = GameDescriptionUi(
                description = "An expansive open-world action RPG with rich storytelling.",
                isExpanded = false,
                descriptionToggleUi = DescriptionToggleUi(
                    text = "Read more",
                    isVisible = true,
                ),
            ),
            companies = listOf(
                GameCompanyUi(
                    name = "FromSoftware",
                    logoUrl = "example",
                    role = "Developer",
                ),
                GameCompanyUi(
                    name = "Bandai Namco",
                    logoUrl = "example",
                    role = "Publisher",
                ),
            ),
            isBookmarked = true,
        ),
        headerUi = AppHeaderUi(
            title = readyTitle,
            navigationItem = AppHeaderMenu(
                iconResId = CoreUiR.drawable.ic_back_24,
                contentDescriptionResId = CoreUiR.string.back,
            ),
        ),
    )

    val loadingState: GameDetailsState.Loading = GameDetailsState.Loading(headerUi = AppHeaderUi())

    val screenshotsItems: List<ScreenshotUI> = listOf(
        ScreenshotUI(id = 1, imageUrl = "https://example.com/screenshot-1.webp"),
        ScreenshotUI(id = 2, imageUrl = "https://example.com/screenshot-2.webp"),
        ScreenshotUI(id = 3, imageUrl = "https://example.com/screenshot-3.webp"),
    )

    val moviesItems: List<MovieUI> = listOf(
        MovieUI(
            id = 1,
            videoUrl = "https://example.com/video-1.mp4",
            previewUrl = "example",
            name = "Launch Trailer",
        ),
        MovieUI(
            id = 2,
            videoUrl = "https://example.com/video-2.mp4",
            previewUrl = "example",
            name = "Gameplay Overview",
        ),
        MovieUI(
            id = 3,
            videoUrl = "https://example.com/video-3.mp4",
            previewUrl = "example",
            name = "Story Trailer",
        ),
    )

    val seriesItems: List<GameCardUI> = listOf(
        GameCardUI(
            id = 2,
            title = "Dark Souls III",
            imageUrl = "example",
            releaseDate = "Mar 24, 2016",
            platforms = setOf(
                PlatformUI(
                    name = "PC",
                    iconRes = CoreUiR.drawable.ic_controller_24,
                ),
            ),
            rating = ChipUI(text = "4.6", isSelected = true),
            isBookmarked = false,
        ),
        GameCardUI(
            id = 3,
            title = "Sekiro: Shadows Die Twice",
            imageUrl = "example",
            releaseDate = "Mar 22, 2019",
            platforms = setOf(
                PlatformUI(
                    name = "PC",
                    iconRes = CoreUiR.drawable.ic_controller_24,
                ),
            ),
            rating = ChipUI(text = "4.5", isSelected = true),
            isBookmarked = false,
        ),
        GameCardUI(
            id = 4,
            title = "Bloodborne",
            imageUrl = "example",
            releaseDate = "Mar 24, 2015",
            platforms = setOf(
                PlatformUI(
                    name = "PlayStation",
                    iconRes = CoreUiR.drawable.ic_controller_24,
                ),
            ),
            rating = ChipUI(text = "4.7", isSelected = true),
            isBookmarked = false,
        ),
    )

    val screenshots: Flow<PagingData<ScreenshotUI>> = loadedPagingFlow(screenshotsItems)
    val movies: Flow<PagingData<MovieUI>> = loadedPagingFlow(moviesItems)
    val seriesState: Flow<PagingData<GameCardUI>> = loadedPagingFlow(seriesItems)

    val emptyScreenshots: Flow<PagingData<ScreenshotUI>> = emptyPagingFlow()
    val emptyMovies: Flow<PagingData<MovieUI>> = emptyPagingFlow()
    val emptySeries: Flow<PagingData<GameCardUI>> = emptyPagingFlow()

    val loadingScreenshots: Flow<PagingData<ScreenshotUI>> = loadingPagingFlow()
    val loadingMovies: Flow<PagingData<MovieUI>> = loadingPagingFlow()
    val loadingSeries: Flow<PagingData<GameCardUI>> = loadingPagingFlow()

    val longDescription: String = List(120) {
        "This sprawling fantasy adventure crosses ruined kingdoms, hidden catacombs, towering castles, forgotten battlefields, and labyrinthine cities packed with secrets."
    }.joinToString(separator = " ")
}
