package io.github.onreg.core.db.movies.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.movies.entity.GameMovieRemoteKeysEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class GameMovieRemoteKeysDaoTest {
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
    private val firstGameRemoteKeys = GameMovieRemoteKeysEntity(
        gameId = firstGame.id,
        prevKey = null,
        nextKey = 2,
    )
    private val secondGameRemoteKeys = GameMovieRemoteKeysEntity(
        gameId = secondGame.id,
        prevKey = 1,
        nextKey = 3,
    )

    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameDao = database.gameDao()
    private val remoteKeysDao = database.gameMovieRemoteKeysDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `getByGameId should return null when no remote keys exist for the game`() = runTest {
        gameDao.insertGames(listOf(firstGame))

        val remoteKeys = remoteKeysDao.getByGameId(firstGame.id)

        assertNull(remoteKeys)
    }

    @Test
    fun `insertRemoteKeys should persist a remote keys row and return it from getByGameId`() =
        runTest {
            gameDao.insertGames(listOf(firstGame))

            remoteKeysDao.insertRemoteKeys(listOf(firstGameRemoteKeys))

            assertEquals(firstGameRemoteKeys, remoteKeysDao.getByGameId(firstGame.id))
        }

    @Test
    fun `insertRemoteKeys should preserve null prevKey and null nextKey values`() = runTest {
        val terminalRemoteKeys = firstGameRemoteKeys.copy(nextKey = null)

        gameDao.insertGames(listOf(firstGame))
        remoteKeysDao.insertRemoteKeys(listOf(terminalRemoteKeys))

        assertEquals(terminalRemoteKeys, remoteKeysDao.getByGameId(firstGame.id))
    }

    @Test
    fun `insertRemoteKeys should replace an existing row when the same gameId is inserted again`() =
        runTest {
            val updatedRemoteKeys = firstGameRemoteKeys.copy(prevKey = 4, nextKey = 6)

            gameDao.insertGames(listOf(firstGame))
            remoteKeysDao.insertRemoteKeys(listOf(firstGameRemoteKeys))
            remoteKeysDao.insertRemoteKeys(listOf(updatedRemoteKeys))

            assertEquals(updatedRemoteKeys, remoteKeysDao.getByGameId(firstGame.id))
            assertEquals(1, countRows(GameMovieRemoteKeysEntity.TABLE_NAME))
        }

    @Test
    fun `insertRemoteKeys should persist remote keys for multiple games and getByGameId should return the matching row for each game`() =
        runTest {
            gameDao.insertGames(listOf(firstGame, secondGame))

            remoteKeysDao.insertRemoteKeys(listOf(firstGameRemoteKeys, secondGameRemoteKeys))

            assertEquals(firstGameRemoteKeys, remoteKeysDao.getByGameId(firstGame.id))
            assertEquals(secondGameRemoteKeys, remoteKeysDao.getByGameId(secondGame.id))
        }

    @Test
    fun `getByGameId should not return remote keys that belong to a different game`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        remoteKeysDao.insertRemoteKeys(listOf(secondGameRemoteKeys))

        assertNull(remoteKeysDao.getByGameId(firstGame.id))
        assertEquals(secondGameRemoteKeys, remoteKeysDao.getByGameId(secondGame.id))
    }

    @Test
    fun `insertRemoteKeys should fail when the referenced game does not exist`() = runTest {
        val error = try {
            remoteKeysDao.insertRemoteKeys(listOf(firstGameRemoteKeys))
            null
        } catch (throwable: Throwable) {
            throwable
        }

        assertNotNull(error)
        assertNull(remoteKeysDao.getByGameId(firstGame.id))
        assertEquals(0, countRows(GameMovieRemoteKeysEntity.TABLE_NAME))
    }

    @Test
    fun `should cascade delete remote keys when the parent game is removed`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        remoteKeysDao.insertRemoteKeys(listOf(firstGameRemoteKeys, secondGameRemoteKeys))

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameEntity.TABLE_NAME} WHERE ${GameEntity.ID} = ?",
            arrayOf(firstGame.id),
        )

        assertNull(remoteKeysDao.getByGameId(firstGame.id))
        assertEquals(secondGameRemoteKeys, remoteKeysDao.getByGameId(secondGame.id))
        assertEquals(1, countRows(GameMovieRemoteKeysEntity.TABLE_NAME))
    }

    @Test
    fun `insertRemoteKeys with an empty list should be a no-op`() = runTest {
        gameDao.insertGames(listOf(firstGame))

        remoteKeysDao.insertRemoteKeys(emptyList())

        assertNull(remoteKeysDao.getByGameId(firstGame.id))
        assertTrue(countRows(GameMovieRemoteKeysEntity.TABLE_NAME) == 0)
    }

    private fun countRows(tableName: String): Int {
        val cursor = database.query("SELECT COUNT(*) FROM $tableName", null)
        cursor.use {
            assertTrue(it.moveToFirst())
            return it.getInt(0)
        }
    }
}
