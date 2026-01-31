package io.github.onreg.data.game.impl

import androidx.paging.testing.asSnapshot
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.data.game.api.model.GamePlatform
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GameRepositoryTest {
    private val mappedGame = Game(
        id = 1,
        title = "Title",
        imageUrl = "image",
        releaseDate = null,
        rating = 4.5,
        platforms = setOf(GamePlatform.PC),
    )

    private val gameEntity = GameEntity(
        id = 1,
        title = "Title",
        imageUrl = "image",
        releaseDate = null,
        rating = 4.5,
        insertionOrder = 0,
    )
    private val entityWithPlatforms = GameWithPlatforms(
        game = gameEntity,
        platforms = listOf(PlatformEntity(GamePlatform.PC.id)),
    )

    private val driver = GameRepositoryTestDriver
        .Builder()
        .gameDaoPagingSource(listOf(entityWithPlatforms))
        .gameEntityMapperMap(entityWithPlatforms, mappedGame)
        .build()

    @Test
    fun `should get games`() = runTest {
        val items = driver.getGames().asSnapshot()

        verify(driver.gameDao).pagingSource()
        verify(driver.entityMapper).map(entityWithPlatforms)
        assertEquals(listOf(mappedGame), items)
    }
}
