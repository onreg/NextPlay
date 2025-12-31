package io.github.onreg.ui.game.presentation.mapper

import androidx.paging.PagingData
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.testing.unit.paging.asSnapshot
import io.github.onreg.ui.game.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.platform.model.PlatformUI
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class GameUiMapperTest {
    @Test
    fun `should map paging data and apply bookmarks`() = runTest {
        val gamePlatform = setOf(GamePlatform.PC)
        val platformUi = setOf(PlatformUI(name = "PC", iconRes = 1))
        val driver = GameUiMapperTestDriver.Builder()
            .platformUiMapperMap(gamePlatform, platformUi)
            .build()

        val games = listOf(
            Game(
                id = 1,
                title = "First",
                imageUrl = "image-1",
                releaseDate = Instant.parse("2023-07-19T00:00:00Z"),
                rating = 3.5,
                platforms = gamePlatform
            ),
            Game(
                id = 2,
                title = "Second",
                imageUrl = "image-2",
                releaseDate = null,
                rating = 4.5,
                platforms = gamePlatform
            )
        )

        val pagingData = driver.mapper.map(PagingData.from(games), setOf("2"))
        val items = pagingData.asSnapshot()

        val expected = listOf(
            GameCardUI(
                id = "1",
                title = "First",
                imageUrl = "image-1",
                releaseDate = "Jul 19, 2023",
                platforms = platformUi,
                rating = ChipUI(text = "3.5", isSelected = true),
                isBookmarked = false
            ),
            GameCardUI(
                id = "2",
                title = "Second",
                imageUrl = "image-2",
                releaseDate = "",
                platforms = platformUi,
                rating = ChipUI(text = "4.5", isSelected = true),
                isBookmarked = true
            )
        )

        assertEquals(expected, items)
    }
}
