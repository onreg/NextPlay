package io.github.onreg.core.db.screenshots.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.screenshots.entity.GameScreenshotRemoteKeysEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
internal class GameScreenshotRemoteKeysDaoTest {
    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameDao = database.gameDao()
    private val remoteKeysDao = database.gameScreenshotRemoteKeysDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `should return remote key by game id`() = runTest {
        val game = GameEntity(
            id = 3001,
            title = "Remote",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-05-01T00:00:00Z"),
            rating = 4.1,
        )
        val remoteKey = GameScreenshotRemoteKeysEntity(
            gameId = game.id,
            prevKey = null,
            nextKey = 2,
        )
        gameDao.insertGames(listOf(game))
        remoteKeysDao.insertRemoteKeys(listOf(remoteKey))

        val loaded = remoteKeysDao.getByGameId(game.id)

        assertEquals(remoteKey, loaded)
    }

    @Test
    fun `should delete remote key by game id`() = runTest {
        val firstGame = GameEntity(
            id = 3002,
            title = "First",
            imageUrl = "first",
            releaseDate = Instant.parse("2024-06-01T00:00:00Z"),
            rating = 4.0,
        )
        val secondGame = GameEntity(
            id = 3003,
            title = "Second",
            imageUrl = "second",
            releaseDate = Instant.parse("2024-07-01T00:00:00Z"),
            rating = 4.2,
        )
        gameDao.insertGames(listOf(firstGame, secondGame))
        val firstRemoteKey = GameScreenshotRemoteKeysEntity(
            gameId = firstGame.id,
            prevKey = 1,
            nextKey = 3,
        )
        val secondRemoteKey = GameScreenshotRemoteKeysEntity(
            gameId = secondGame.id,
            prevKey = null,
            nextKey = 4,
        )
        remoteKeysDao.insertRemoteKeys(listOf(firstRemoteKey, secondRemoteKey))

        remoteKeysDao.deleteByGameId(firstGame.id)

        assertNull(remoteKeysDao.getByGameId(firstGame.id))
        assertEquals(secondRemoteKey, remoteKeysDao.getByGameId(secondGame.id))
    }

    @Test
    fun `should cascade delete remote key when game is deleted`() = runTest {
        val game = GameEntity(
            id = 3004,
            title = "Cascade",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-08-01T00:00:00Z"),
            rating = 4.5,
        )
        val remoteKey = GameScreenshotRemoteKeysEntity(
            gameId = game.id,
            prevKey = null,
            nextKey = 6,
        )
        gameDao.insertGames(listOf(game))
        remoteKeysDao.insertRemoteKeys(listOf(remoteKey))

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameEntity.TABLE_NAME} WHERE ${GameEntity.ID} = ?",
            arrayOf(game.id),
        )

        assertNull(remoteKeysDao.getByGameId(game.id))
    }
}
