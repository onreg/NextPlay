package io.github.onreg.ui.game.list.presentation.components.card

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.game.list.presentation.components.card.test.GameCardTestTags.GAME_CARD_ADD_BOOKMARK_BUTTON
import io.github.onreg.ui.platform.model.PlatformUI
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import io.github.onreg.core.ui.R as CoreUiR

@RunWith(RobolectricTestRunner::class)
internal class GameCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `series variant should hide bookmark button`() {
        composeRule.setContent {
            GameCard(gameData = gameCardUi())
        }

        composeRule.onAllNodesWithTag(GAME_CARD_ADD_BOOKMARK_BUTTON).assertCountEquals(0)
        composeRule.onNodeWithText("Game title").assertIsDisplayed()
    }

    private fun gameCardUi(): GameCardUI = GameCardUI(
        id = 1,
        title = "Game title",
        imageUrl = "https://image",
        releaseDate = "Jan 1, 2020",
        platforms = setOf(PlatformUI("PC", CoreUiR.drawable.ic_controller_24)),
        rating = ChipUI(text = "4.2", isSelected = true),
        isBookmarked = false,
    )
}
