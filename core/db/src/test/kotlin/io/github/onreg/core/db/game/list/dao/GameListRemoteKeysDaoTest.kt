package io.github.onreg.core.db.game.list.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.list.entity.GameListEntity
import io.github.onreg.core.db.game.list.entity.GameListRemoteKeysEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
internal class GameListRemoteKeysDaoTest {
    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()
    private val gameDao = database.gameDao()
    private val gameListDao = database.gameListDao()
    private val remoteKeysDao = database.gameListRemoteKeysDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `should return remote key by game id`() = runTest {
        val game = GameEntity(
            id = 40,
            title = "Remote",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-05-01T00:00:00Z"),
            rating = 3.9,
        )
        gameDao.insertGames(listOf(game))
        gameListDao.insertGameListEntries(
            listOf(
                GameListEntity(
                    gameId = game.id,
                    position = 0,
                ),
            ),
        )
        val remoteKey =
            GameListRemoteKeysEntity(
                gameId = game.id,
                prevKey = null,
                nextKey = 5,
            )
        remoteKeysDao.insertRemoteKeys(listOf(remoteKey))

        val loaded = remoteKeysDao.getByGameId(game.id)

        assertEquals(remoteKey, loaded)
    }

    @Test
    fun `should cascade delete remote key when game is deleted`() = runTest {
        val game = GameEntity(
            id = 42,
            title = "Cascade",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-07-01T00:00:00Z"),
            rating = 4.1,
        )
        gameDao.insertGames(listOf(game))
        gameListDao.insertGameListEntries(
            listOf(
                GameListEntity(
                    gameId = game.id,
                    position = 0,
                ),
            ),
        )
        val remoteKey =
            GameListRemoteKeysEntity(
                gameId = game.id,
                prevKey = 1,
                nextKey = 3,
            )
        remoteKeysDao.insertRemoteKeys(listOf(remoteKey))

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameEntity.TABLE_NAME} WHERE ${GameEntity.ID} = ?",
            arrayOf(game.id),
        )

        val loaded = remoteKeysDao.getByGameId(game.id)
        assertNull(loaded)
        assertEquals(0, countRows(GameListEntity.TABLE_NAME))
    }

    @Test
    fun `should cascade delete remote key when list membership is cleared`() = runTest {
        val game = GameEntity(
            id = 43,
            title = "Membership Cascade",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-08-01T00:00:00Z"),
            rating = 4.0,
        )
        gameDao.insertGames(listOf(game))
        gameListDao.insertGameListEntries(
            listOf(
                GameListEntity(
                    gameId = game.id,
                    position = 0,
                ),
            ),
        )
        remoteKeysDao.insertRemoteKeys(
            listOf(
                GameListRemoteKeysEntity(
                    gameId = game.id,
                    prevKey = null,
                    nextKey = 2,
                ),
            ),
        )

        gameListDao.deleteAll()

        assertNull(remoteKeysDao.getByGameId(game.id))
        assertEquals(0, countRows(GameListEntity.TABLE_NAME))
    }

    private fun countRows(table: String): Int =
        database.query("SELECT COUNT(*) FROM $table", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
}
