package io.github.onreg.core.db.game.list.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.list.entity.GameListEntity
import io.github.onreg.core.db.game.list.entity.GameListRemoteKeysEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class GameListRemoteKeysDaoTest {
    private val firstGame = GameEntity(
        id = 101,
        title = "First Game",
        imageUrl = "https://example.com/first.png",
        releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
        rating = 4.1,
    )
    private val secondGame = GameEntity(
        id = 102,
        title = "Second Game",
        imageUrl = "https://example.com/second.png",
        releaseDate = Instant.parse("2024-02-02T00:00:00Z"),
        rating = 4.4,
    )
    private val firstListEntry = GameListEntity(
        gameId = firstGame.id,
        position = 0,
    )
    private val secondListEntry = GameListEntity(
        gameId = secondGame.id,
        position = 1,
    )
    private val firstRemoteKeys = GameListRemoteKeysEntity(
        gameId = firstGame.id,
        prevKey = null,
        nextKey = null,
    )
    private val secondRemoteKeys = GameListRemoteKeysEntity(
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

    private val gameDao: GameDao = database.gameDao()
    private val gameListDao: GameListDao = database.gameListDao()
    private val remoteKeysDao: GameListRemoteKeysDao = database.gameListRemoteKeysDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `getByGameId should return null when no remote keys exist for the requested game`() =
        runTest {
            assertNull(remoteKeysDao.getByGameId(firstGame.id))
        }

    @Test
    fun `insertRemoteKeys should persist a remote key row and return the exact entity`() = runTest {
        gameDao.insertGames(listOf(firstGame))
        gameListDao.insertGameListEntries(listOf(firstListEntry))

        remoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys))

        assertEquals(firstRemoteKeys, remoteKeysDao.getByGameId(firstGame.id))
    }

    @Test
    fun `getByGameId should return the correct remote keys when multiple games have stored keys`() =
        runTest {
            gameDao.insertGames(listOf(firstGame, secondGame))
            gameListDao.insertGameListEntries(listOf(firstListEntry, secondListEntry))
            remoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys, secondRemoteKeys))

            assertEquals(secondRemoteKeys, remoteKeysDao.getByGameId(secondGame.id))
        }

    @Test
    fun `insertRemoteKeys should replace an existing row when another entity with the same gameId is inserted`() =
        runTest {
            val updatedRemoteKeys = firstRemoteKeys.copy(
                prevKey = 2,
                nextKey = 4,
            )

            gameDao.insertGames(listOf(firstGame))
            gameListDao.insertGameListEntries(listOf(firstListEntry))
            remoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys))

            remoteKeysDao.insertRemoteKeys(listOf(updatedRemoteKeys))

            assertEquals(updatedRemoteKeys, remoteKeysDao.getByGameId(firstGame.id))
        }

    @Test
    fun `insertRemoteKeys should replace only the conflicted gameId and leave other rows unchanged`() =
        runTest {
            val updatedFirstRemoteKeys = firstRemoteKeys.copy(
                prevKey = null,
                nextKey = 2,
            )

            gameDao.insertGames(listOf(firstGame, secondGame))
            gameListDao.insertGameListEntries(listOf(firstListEntry, secondListEntry))
            remoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys, secondRemoteKeys))

            remoteKeysDao.insertRemoteKeys(listOf(updatedFirstRemoteKeys))

            assertEquals(updatedFirstRemoteKeys, remoteKeysDao.getByGameId(firstGame.id))
            assertEquals(secondRemoteKeys, remoteKeysDao.getByGameId(secondGame.id))
        }

    @Test
    fun `insertRemoteKeys should fail when the referenced game list entry does not exist`() =
        runTest {
            val error = try {
                remoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys))
                null
            } catch (throwable: Throwable) {
                throwable
            }

            assertTrue(error != null)
            assertEquals(0, countRows(GameListRemoteKeysEntity.TABLE_NAME))
        }

    @Test
    fun `should cascade delete remote keys when the parent game list entry is removed`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        gameListDao.insertGameListEntries(listOf(firstListEntry, secondListEntry))
        remoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys, secondRemoteKeys))

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameListEntity.TABLE_NAME} WHERE ${GameListEntity.GAME_ID} = ?",
            arrayOf(firstGame.id),
        )

        assertNull(remoteKeysDao.getByGameId(firstGame.id))
        assertEquals(secondRemoteKeys, remoteKeysDao.getByGameId(secondGame.id))
    }

    @Test
    fun `should cascade delete remote keys when game list entries are cleared through deleteAll`() =
        runTest {
            gameDao.insertGames(listOf(firstGame, secondGame))
            gameListDao.insertGameListEntries(listOf(firstListEntry, secondListEntry))
            remoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys, secondRemoteKeys))

            gameListDao.deleteAll()

            assertNull(remoteKeysDao.getByGameId(firstGame.id))
            assertNull(remoteKeysDao.getByGameId(secondGame.id))
            assertEquals(2, countRows(GameEntity.TABLE_NAME))
        }

    private fun countRows(tableName: String): Int = database.query(
        "SELECT COUNT(*) FROM $tableName",
        null,
    ).use { cursor ->
        cursor.moveToFirst()
        cursor.getInt(0)
    }
}
