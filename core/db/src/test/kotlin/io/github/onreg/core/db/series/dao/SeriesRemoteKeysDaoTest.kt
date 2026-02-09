package io.github.onreg.core.db.series.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.series.entity.SeriesEntity
import io.github.onreg.core.db.series.entity.SeriesRemoteKeysEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
internal class SeriesRemoteKeysDaoTest {
    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameDao = database.gameDao()
    private val seriesDao = database.seriesDao()
    private val remoteKeysDao = database.seriesRemoteKeysDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `should return remote key by game id`() = runTest {
        val game = GameEntity(
            id = 8001,
            title = "Series",
            imageUrl = "image",
            releaseDate = Instant.parse("2025-09-01T00:00:00Z"),
            rating = 4.3,
        )
        val seriesEntry = SeriesEntity(gameId = game.id, position = 0)
        val remoteKey = SeriesRemoteKeysEntity(
            gameId = game.id,
            prevKey = null,
            nextKey = 2,
        )
        gameDao.insertGames(listOf(game))
        seriesDao.insertSeriesEntries(listOf(seriesEntry))
        remoteKeysDao.insertRemoteKeys(listOf(remoteKey))

        val loaded = remoteKeysDao.getByGameId(game.id)

        assertEquals(remoteKey, loaded)
    }

    @Test
    fun `should cascade delete remote key when game is deleted`() = runTest {
        val game = GameEntity(
            id = 8002,
            title = "Cascade",
            imageUrl = "image",
            releaseDate = Instant.parse("2025-10-01T00:00:00Z"),
            rating = 4.5,
        )
        gameDao.insertGames(listOf(game))
        seriesDao.insertSeriesEntries(listOf(SeriesEntity(gameId = game.id, position = 0)))
        remoteKeysDao.insertRemoteKeys(
            listOf(
                SeriesRemoteKeysEntity(
                    gameId = game.id,
                    prevKey = 1,
                    nextKey = 3,
                ),
            ),
        )

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameEntity.TABLE_NAME} WHERE ${GameEntity.ID} = ?",
            arrayOf(game.id),
        )

        assertNull(remoteKeysDao.getByGameId(game.id))
    }

    @Test
    fun `should cascade delete remote keys when series is cleared`() = runTest {
        val game = GameEntity(
            id = 8003,
            title = "Clear",
            imageUrl = "image",
            releaseDate = Instant.parse("2025-11-01T00:00:00Z"),
            rating = 4.6,
        )
        gameDao.insertGames(listOf(game))
        seriesDao.insertSeriesEntries(listOf(SeriesEntity(gameId = game.id, position = 0)))
        remoteKeysDao.insertRemoteKeys(
            listOf(
                SeriesRemoteKeysEntity(
                    gameId = game.id,
                    prevKey = null,
                    nextKey = 4,
                ),
            ),
        )

        seriesDao.deleteAll()

        assertNull(remoteKeysDao.getByGameId(game.id))
    }
}
