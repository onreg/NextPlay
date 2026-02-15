package io.github.onreg.core.db.screenshots.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
internal class GameScreenshotsDaoTest {
    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameDao = database.gameDao()
    private val gameScreenshotsDao = database.gameScreenshotsDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `should return screenshots ordered by position`() = runTest {
        val firstGameId = 1001
        val secondGameId = 1002
        gameDao.insertGames(listOf(game(firstGameId, "First"), game(secondGameId, "Second")))

        val firstGameScreenshots = listOf(
            ScreenshotEntity(
                id = 1,
                gameId = firstGameId,
                position = 2,
                imageUrl = "first-2",
                width = 1920,
                height = 1080,
            ),
            ScreenshotEntity(
                id = 2,
                gameId = firstGameId,
                position = 1,
                imageUrl = "first-1",
                width = 1920,
                height = 1080,
            ),
        )
        val secondGameScreenshot = ScreenshotEntity(
            id = 3,
            gameId = secondGameId,
            position = 0,
            imageUrl = "second-0",
            width = 1280,
            height = 720,
        )
        gameScreenshotsDao.insertScreenshots(firstGameScreenshots + secondGameScreenshot)
        val result = loadScreenshots(firstGameId)
        assertEquals(
            listOf(
                firstGameScreenshots[1],
                firstGameScreenshots[0],
            ),
            result,
        )
    }

    @Test
    fun `should delete screenshots only for requested game id`() = runTest {
        val firstGameId = 2001
        val secondGameId = 2002
        gameDao.insertGames(listOf(game(firstGameId, "First"), game(secondGameId, "Second")))

        val firstGameScreenshot = ScreenshotEntity(
            id = 10,
            gameId = firstGameId,
            position = 0,
            imageUrl = "first-0",
            width = 1920,
            height = 1080,
        )
        val secondGameScreenshot = ScreenshotEntity(
            id = 11,
            gameId = secondGameId,
            position = 0,
            imageUrl = "second-0",
            width = 1920,
            height = 1080,
        )
        gameScreenshotsDao.insertScreenshots(listOf(firstGameScreenshot, secondGameScreenshot))

        gameScreenshotsDao.deleteByGameId(firstGameId)

        val firstGameResult = loadScreenshots(firstGameId)
        val secondGameResult = loadScreenshots(secondGameId)
        assertTrue(firstGameResult.isEmpty())
        assertEquals(listOf(secondGameScreenshot), secondGameResult)
        assertEquals(2, countRows(GameEntity.TABLE_NAME))
    }

    @Test
    fun `should return empty list when game has no screenshots`() = runTest {
        val gameId = 3001
        gameDao.insertGames(listOf(game(gameId, "No Screenshots")))

        val result = loadScreenshots(gameId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `should cascade delete screenshots for deleted game only`() = runTest {
        val firstGameId = 3002
        val secondGameId = 3003
        gameDao.insertGames(listOf(game(firstGameId, "First"), game(secondGameId, "Second")))
        val firstGameScreenshots = listOf(
            ScreenshotEntity(
                id = 20,
                gameId = firstGameId,
                position = 0,
                imageUrl = "first-20",
                width = 1920,
                height = 1080,
            ),
            ScreenshotEntity(
                id = 21,
                gameId = firstGameId,
                position = 1,
                imageUrl = "first-21",
                width = 1920,
                height = 1080,
            ),
        )
        val secondGameScreenshots = listOf(
            ScreenshotEntity(
                id = 22,
                gameId = secondGameId,
                position = 0,
                imageUrl = "second-22",
                width = 1280,
                height = 720,
            ),
        )
        gameScreenshotsDao.insertScreenshots(firstGameScreenshots + secondGameScreenshots)

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameEntity.TABLE_NAME} WHERE ${GameEntity.ID} = ?",
            arrayOf(firstGameId),
        )

        assertTrue(loadScreenshots(firstGameId).isEmpty())
        assertEquals(secondGameScreenshots, loadScreenshots(secondGameId))
        assertEquals(1, countRows(GameEntity.TABLE_NAME))
        assertEquals(secondGameScreenshots.size, countRows(ScreenshotEntity.TABLE_NAME))
    }

    private fun game(
        id: Int,
        title: String,
    ): GameEntity = GameEntity(
        id = id,
        title = title,
        imageUrl = title.lowercase(),
        releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
        rating = 4.0,
    )

    private suspend fun loadScreenshots(gameId: Int): List<ScreenshotEntity> {
        val result = gameScreenshotsDao.pagingSource(gameId).load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false,
            ),
        )
        assertTrue(result is PagingSource.LoadResult.Page)
        return result.data
    }

    private fun countRows(table: String): Int =
        database.query("SELECT COUNT(*) FROM $table", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
}
