package io.github.onreg.feature.game.details.impl.mapper

import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.components.header.AppHeaderMenu
import io.github.onreg.core.ui.components.header.AppHeaderUi
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.feature.game.details.impl.model.ContentState
import io.github.onreg.feature.game.details.impl.model.DescriptionToggleUi
import io.github.onreg.feature.game.details.impl.model.GameDescriptionUi
import io.github.onreg.feature.game.details.impl.model.GameDetailsInternalState
import io.github.onreg.feature.game.details.impl.model.GameDetailsState
import io.github.onreg.feature.game.details.impl.model.GameDetailsUi
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class GameDetailsStateMapperTest {
    private val baseHeader = AppHeaderUi(
        navigationItem = AppHeaderMenu(
            iconResId = io.github.onreg.core.ui.R.drawable.ic_back_24,
            contentDescriptionResId = io.github.onreg.core.ui.R.string.back,
        ),
    )

    private val localStateIdle = GameDetailsInternalState(
        isBookmarked = false,
        contentState = ContentState.Idle,
        isDescriptionExpanded = false,
        isReadMoreVisible = false,
    )

    private val localStateError = localStateIdle.copy(
        contentState = ContentState.Error,
    )

    private val gameDetails = GameDetails(
        gameId = 1,
        title = "Some title",
        imageUrl = "img",
        releaseDate = null,
        platforms = emptySet(),
        website = null,
        rating = 0.0,
        description = "",
        companies = emptyList(),
    )

    private val mappedUi = GameDetailsUi(
        image = "img",
        rating = ChipUI(text = "0", isSelected = true),
        releaseDate = "",
        platforms = emptySet(),
        website = null,
        gameDescriptionUi = GameDescriptionUi(
            description = "",
            isExpanded = false,
            descriptionToggleUi = DescriptionToggleUi(
                text = "",
                isVisible = false,
            ),
        ),
        companies = emptyList(),
        isBookmarked = false,
    )

    private val gameDetailsUiMapper: GameDetailsUiMapper = mock {
        on { map(model = gameDetails, localState = localStateIdle) } doReturn mappedUi
    }

    private val mapper = GameDetailsStateMapperImpl(
        gameDetailsUiMapper = gameDetailsUiMapper,
    )

    @Test
    fun `should map to Ready when gameDetails is not null`() {
        val result = mapper.map(
            gameDetails = gameDetails,
            localState = localStateIdle,
        )

        assertEquals(
            GameDetailsState.Ready(
                details = mappedUi,
                headerUi = baseHeader.copy(title = "Some title"),
            ),
            result,
        )
    }

    @Test
    fun `should map to Error when gameDetails is null and contentState is Error`() {
        val result = mapper.map(
            gameDetails = null,
            localState = localStateError,
        )

        assertEquals(
            GameDetailsState.Error(
                headerUi = baseHeader,
            ),
            result,
        )
    }

    @Test
    fun `should map to Loading when gameDetails is null and contentState is not Error`() {
        val result = mapper.map(
            gameDetails = null,
            localState = localStateIdle,
        )

        assertEquals(
            GameDetailsState.Loading(
                headerUi = baseHeader,
            ),
            result,
        )
    }
}
