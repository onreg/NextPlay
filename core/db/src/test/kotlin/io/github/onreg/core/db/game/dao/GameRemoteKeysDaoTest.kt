package io.github.onreg.core.db.game.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GameListRemoteKeysEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
internal class GameRemoteKeysDaoTest {
    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()
    private val gameDao = database.gameDao()
    private val remoteKeysDao = database.gameListRemoteKeysDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `should return remote key by list and game id`() = runTest {
        val listKey = "default"
        val game = GameEntity(
            id = 40,
            title = "Remote",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-05-01T00:00:00Z"),
            rating = 3.9,
        )
        gameDao.insertGames(listOf(game))
        val remoteKey = GameListRemoteKeysEntity(
            listKey = listKey,
            gameId = game.id,
            prevKey = null,
            nextKey = 5,
        )
        remoteKeysDao.insertRemoteKeys(listOf(remoteKey))

        val loaded = remoteKeysDao.getRemoteKey(listKey, game.id)

        assertEquals(remoteKey, loaded)
    }

    @Test
    fun `should cascade delete remote key when game is deleted`() = runTest {
        val listKey = "default"
        val game = GameEntity(
            id = 42,
            title = "Cascade",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-07-01T00:00:00Z"),
            rating = 4.1,
        )
        gameDao.insertGames(listOf(game))
        val remoteKey = GameListRemoteKeysEntity(
            listKey = listKey,
            gameId = game.id,
            prevKey = 1,
            nextKey = 3,
        )
        remoteKeysDao.insertRemoteKeys(listOf(remoteKey))

        gameDao.clearGames()

        val loaded = remoteKeysDao.getRemoteKey(listKey, game.id)
        assertNull(loaded)
    }
}
