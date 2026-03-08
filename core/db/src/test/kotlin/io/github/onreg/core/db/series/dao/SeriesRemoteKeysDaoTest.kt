package io.github.onreg.core.db.series.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.series.entity.SeriesEntity
import io.github.onreg.core.db.series.entity.SeriesRemoteKeysEntity
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
internal class SeriesRemoteKeysDaoTest {
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
        rating = 4.2,
    )
    private val thirdGame = GameEntity(
        id = 103,
        title = "Third Game",
        imageUrl = "https://example.com/third.png",
        releaseDate = Instant.parse("2024-03-03T00:00:00Z"),
        rating = 4.3,
    )

    private val firstSeriesEntry = SeriesEntity(
        gameId = firstGame.id,
        position = 1,
    )
    private val secondSeriesEntry = SeriesEntity(
        gameId = secondGame.id,
        position = 2,
    )
    private val thirdSeriesEntry = SeriesEntity(
        gameId = thirdGame.id,
        position = 3,
    )

    private val firstRemoteKeys = SeriesRemoteKeysEntity(
        gameId = firstGame.id,
        prevKey = null,
        nextKey = 2,
    )
    private val secondRemoteKeys = SeriesRemoteKeysEntity(
        gameId = secondGame.id,
        prevKey = 1,
        nextKey = 3,
    )
    private val thirdRemoteKeys = SeriesRemoteKeysEntity(
        gameId = thirdGame.id,
        prevKey = 2,
        nextKey = null,
    )

    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameDao = database.gameDao()
    private val seriesDao = database.seriesDao()
    private val seriesRemoteKeysDao = database.seriesRemoteKeysDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `getByGameId should return null when there are no remote keys for the requested game id`() =
        runTest {
            assertNull(seriesRemoteKeysDao.getByGameId(firstGame.id))
        }

    @Test
    fun `insertRemoteKeys should persist a remote key row and getByGameId should return the saved entity`() =
        runTest {
            gameDao.insertGames(listOf(firstGame))
            seriesDao.insertSeriesEntries(listOf(firstSeriesEntry))

            seriesRemoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys))

            assertEquals(firstRemoteKeys, seriesRemoteKeysDao.getByGameId(firstGame.id))
        }

    @Test
    fun `insertRemoteKeys should preserve nullable prevKey and nextKey values when one or both keys are null`() =
        runTest {
            val remoteKeysWithBothNull = secondRemoteKeys.copy(
                gameId = secondGame.id,
                prevKey = null,
                nextKey = null,
            )

            gameDao.insertGames(listOf(firstGame, secondGame, thirdGame))
            seriesDao.insertSeriesEntries(listOf(firstSeriesEntry, secondSeriesEntry, thirdSeriesEntry))

            seriesRemoteKeysDao.insertRemoteKeys(
                listOf(
                    firstRemoteKeys,
                    remoteKeysWithBothNull,
                    thirdRemoteKeys,
                ),
            )

            assertEquals(firstRemoteKeys, seriesRemoteKeysDao.getByGameId(firstGame.id))
            assertEquals(remoteKeysWithBothNull, seriesRemoteKeysDao.getByGameId(secondGame.id))
            assertEquals(thirdRemoteKeys, seriesRemoteKeysDao.getByGameId(thirdGame.id))
        }

    @Test
    fun `insertRemoteKeys should replace an existing row when another entity with the same gameId is inserted`() =
        runTest {
            val updatedRemoteKeys = firstRemoteKeys.copy(
                prevKey = 7,
                nextKey = 9,
            )

            gameDao.insertGames(listOf(firstGame))
            seriesDao.insertSeriesEntries(listOf(firstSeriesEntry))
            seriesRemoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys))

            seriesRemoteKeysDao.insertRemoteKeys(listOf(updatedRemoteKeys))

            assertEquals(updatedRemoteKeys, seriesRemoteKeysDao.getByGameId(firstGame.id))
        }

    @Test
    fun `insertRemoteKeys should save multiple remote key rows and getByGameId should return only the matching one`() =
        runTest {
            gameDao.insertGames(listOf(firstGame, secondGame))
            seriesDao.insertSeriesEntries(listOf(firstSeriesEntry, secondSeriesEntry))

            seriesRemoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys, secondRemoteKeys))

            assertEquals(firstRemoteKeys, seriesRemoteKeysDao.getByGameId(firstGame.id))
            assertEquals(secondRemoteKeys, seriesRemoteKeysDao.getByGameId(secondGame.id))
            assertNull(seriesRemoteKeysDao.getByGameId(thirdGame.id))
        }

    @Test
    fun `insertRemoteKeys should fail when the referenced SeriesEntity does not exist`() = runTest {
        gameDao.insertGames(listOf(firstGame))

        val error = try {
            seriesRemoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys))
            null
        } catch (throwable: Throwable) {
            throwable
        }

        assertTrue(error != null)
        assertNull(seriesRemoteKeysDao.getByGameId(firstGame.id))
    }

    @Test
    fun `should cascade delete remote keys when the parent series entry is deleted`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        seriesDao.insertSeriesEntries(listOf(firstSeriesEntry, secondSeriesEntry))
        seriesRemoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys, secondRemoteKeys))

        seriesDao.deleteByGameIds(listOf(firstGame.id))

        assertNull(seriesRemoteKeysDao.getByGameId(firstGame.id))
        assertEquals(secondRemoteKeys, seriesRemoteKeysDao.getByGameId(secondGame.id))
    }

    @Test
    fun `insertRemoteKeys with an empty list should be a no-op`() = runTest {
        gameDao.insertGames(listOf(firstGame))
        seriesDao.insertSeriesEntries(listOf(firstSeriesEntry))
        seriesRemoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys))

        seriesRemoteKeysDao.insertRemoteKeys(emptyList())

        assertEquals(firstRemoteKeys, seriesRemoteKeysDao.getByGameId(firstGame.id))
    }
}
