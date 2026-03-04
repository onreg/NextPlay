package io.github.onreg.ui.game.list.presentation.mapper

import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.util.format.InstantTextFormatterImpl
import io.github.onreg.core.util.format.NumberTextFormatterImpl
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import io.github.onreg.ui.platform.model.PlatformUI
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.time.Instant

class GameCardUiMapperTest {
    private val platformUiMapper: PlatformUiMapper = mock {
        on { mapPlatform(setOf(GamePlatform.PC)) } doReturn
            setOf(PlatformUI(name = "PC", iconRes = 1))
    }

    private val mapper = GameCardUiMapperImpl(
        platformUiMapper = platformUiMapper,
        instantTextFormatter = InstantTextFormatterImpl(),
        numberTextFormatter = NumberTextFormatterImpl(),
    )

    @Test
    fun `should map game to game card ui`() {
        val game = Game(
            id = 1,
            title = "First",
            imageUrl = "image-1",
            releaseDate = Instant.parse("2023-07-19T00:00:00Z"),
            rating = 3.5,
            platforms = setOf(GamePlatform.PC),
        )

        val result = mapper.map(game = game, isBookmarked = true)

        assertEquals(
            GameCardUI(
                id = 1,
                title = "First",
                imageUrl = "image-1",
                releaseDate = "Jul 19, 2023",
                platforms = setOf(PlatformUI(name = "PC", iconRes = 1)),
                rating = ChipUI(text = "3.5", isSelected = true),
                isBookmarked = true,
            ),
            result,
        )
    }

    @Test
    fun `should format integer rating without trailing decimal zero`() {
        val game = Game(
            id = 1,
            title = "First",
            imageUrl = "image-1",
            releaseDate = Instant.parse("2023-07-19T00:00:00Z"),
            rating = 8.0,
            platforms = setOf(GamePlatform.PC),
        )

        val result = mapper.map(game = game, isBookmarked = false)

        assertEquals("8", result.rating.text)
    }
}
