package io.github.onreg.core.db.screenshots.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class GameScreenshotsDaoTest {
    private val firstGame = GameEntity(
        id = 101,
        title = "First Game",
        imageUrl = "https://example.com/first.png",
        releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
        rating = 4.2,
    )
    private val secondGame = GameEntity(
        id = 102,
        title = "Second Game",
        imageUrl = "https://example.com/second.png",
        releaseDate = Instant.parse("2024-02-02T00:00:00Z"),
        rating = 4.5,
    )
    private val firstGameFirstScreenshot = ScreenshotEntity(
        id = 1001,
        gameId = firstGame.id,
        position = 0,
        imageUrl = "https://example.com/101-0.png",
        width = 1920,
        height = 1080,
    )
    private val firstGameSecondScreenshot = ScreenshotEntity(
        id = 1002,
        gameId = firstGame.id,
        position = 1,
        imageUrl = "https://example.com/101-1.png",
        width = 1280,
        height = 720,
    )
    private val secondGameFirstScreenshot = ScreenshotEntity(
        id = 2001,
        gameId = secondGame.id,
        position = 0,
        imageUrl = "https://example.com/102-0.png",
        width = 1600,
        height = 900,
    )

    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameDao: GameDao = database.gameDao()
    private val screenshotsDao: GameScreenshotsDao = database.gameScreenshotsDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertScreenshots should persist screenshot rows for an existing game`() = runTest {
        gameDao.insertGames(listOf(firstGame))

        screenshotsDao.insertScreenshots(
            listOf(
                firstGameFirstScreenshot,
                firstGameSecondScreenshot,
            ),
        )

        assertEquals(
            listOf(
                firstGameFirstScreenshot,
                firstGameSecondScreenshot,
            ),
            loadScreenshots(firstGame.id),
        )
    }

    @Test
    fun `insertScreenshots should replace an existing screenshot when the same id is inserted again`() =
        runTest {
            val updatedScreenshot = firstGameFirstScreenshot.copy(
                imageUrl = "https://example.com/101-0-updated.png",
                width = null,
                height = 1440,
            )

            gameDao.insertGames(listOf(firstGame))
            screenshotsDao.insertScreenshots(
                listOf(
                    firstGameFirstScreenshot,
                    firstGameSecondScreenshot,
                ),
            )

            screenshotsDao.insertScreenshots(listOf(updatedScreenshot))

            assertEquals(
                listOf(
                    updatedScreenshot,
                    firstGameSecondScreenshot,
                ),
                loadScreenshots(firstGame.id),
            )
        }

    @Test
    fun `insertScreenshots should preserve nullable width and height values`() = runTest {
        val screenshotWithUnknownSize = firstGameFirstScreenshot.copy(
            width = null,
            height = null,
        )

        gameDao.insertGames(listOf(firstGame))

        screenshotsDao.insertScreenshots(listOf(screenshotWithUnknownSize))

        assertEquals(listOf(screenshotWithUnknownSize), loadScreenshots(firstGame.id))
    }

    @Test
    fun `pagingSource should return only screenshots for the requested game id`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        screenshotsDao.insertScreenshots(
            listOf(
                firstGameFirstScreenshot,
                firstGameSecondScreenshot,
                secondGameFirstScreenshot,
            ),
        )

        assertEquals(
            listOf(
                firstGameFirstScreenshot,
                firstGameSecondScreenshot,
            ),
            loadScreenshots(firstGame.id),
        )
        assertEquals(listOf(secondGameFirstScreenshot), loadScreenshots(secondGame.id))
    }

    @Test
    fun `pagingSource should return screenshots ordered by position ascending regardless of insert order`() =
        runTest {
            val thirdScreenshot = ScreenshotEntity(
                id = 1003,
                gameId = firstGame.id,
                position = 2,
                imageUrl = "https://example.com/101-2.png",
                width = 1024,
                height = 768,
            )

            gameDao.insertGames(listOf(firstGame))
            screenshotsDao.insertScreenshots(
                listOf(
                    thirdScreenshot,
                    firstGameSecondScreenshot,
                    firstGameFirstScreenshot,
                ),
            )

            assertEquals(
                listOf(
                    firstGameFirstScreenshot,
                    firstGameSecondScreenshot,
                    thirdScreenshot,
                ),
                loadScreenshots(firstGame.id),
            )
        }

    @Test
    fun `pagingSource should return an empty page when the game has no screenshots`() = runTest {
        gameDao.insertGames(listOf(firstGame))

        assertEquals(emptyList(), loadScreenshots(firstGame.id))
    }

    @Test
    fun `deleteByGameId should delete screenshots only for the specified game`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        screenshotsDao.insertScreenshots(
            listOf(
                firstGameFirstScreenshot,
                firstGameSecondScreenshot,
                secondGameFirstScreenshot,
            ),
        )

        screenshotsDao.deleteByGameId(firstGame.id)

        assertEquals(emptyList(), loadScreenshots(firstGame.id))
        assertEquals(listOf(secondGameFirstScreenshot), loadScreenshots(secondGame.id))
    }

    @Test
    fun `deleteByGameId should be a no-op when the game has no screenshots`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        screenshotsDao.insertScreenshots(listOf(secondGameFirstScreenshot))

        screenshotsDao.deleteByGameId(firstGame.id)

        assertEquals(emptyList(), loadScreenshots(firstGame.id))
        assertEquals(listOf(secondGameFirstScreenshot), loadScreenshots(secondGame.id))
    }

    @Test
    fun `should cascade delete screenshots when the parent game row is removed`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        screenshotsDao.insertScreenshots(
            listOf(
                firstGameFirstScreenshot,
                firstGameSecondScreenshot,
                secondGameFirstScreenshot,
            ),
        )

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameEntity.TABLE_NAME} WHERE ${GameEntity.ID} = ?",
            arrayOf(firstGame.id),
        )

        assertEquals(emptyList(), loadScreenshots(firstGame.id))
        assertEquals(listOf(secondGameFirstScreenshot), loadScreenshots(secondGame.id))
    }

    @Test
    fun `insertScreenshots should fail when a screenshot references a non-existent game`() =
        runTest {
            val error = try {
                screenshotsDao.insertScreenshots(listOf(firstGameFirstScreenshot))
                null
            } catch (throwable: Throwable) {
                throwable
            }

            assertTrue(error != null)
            assertEquals(emptyList(), loadScreenshots(firstGame.id))
        }

    private suspend fun loadScreenshots(gameId: Int): List<ScreenshotEntity> {
        val result = screenshotsDao.pagingSource(gameId).load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 50,
                placeholdersEnabled = false,
            ),
        )

        return assertIs<PagingSource.LoadResult.Page<Int, ScreenshotEntity>>(result).data
    }
}
