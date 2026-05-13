package io.github.onreg.core.db.series.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.db.series.entity.SeriesEntity
import io.github.onreg.core.db.test.loadDaoRefreshPage
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class SeriesDaoTest {
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
    private val thirdGame = GameEntity(
        id = 103,
        title = "Third Game",
        imageUrl = "https://example.com/third.png",
        releaseDate = Instant.parse("2024-03-03T00:00:00Z"),
        rating = 4.7,
    )
    private val fourthGame = GameEntity(
        id = 104,
        title = "Fourth Game",
        imageUrl = "https://example.com/fourth.png",
        releaseDate = Instant.parse("2024-04-04T00:00:00Z"),
        rating = 4.9,
    )
    private val firstPlatform = PlatformEntity(id = 201)
    private val secondPlatform = PlatformEntity(id = 202)
    private val thirdPlatform = PlatformEntity(id = 203)

    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameDao: GameDao = database.gameDao()
    private val seriesDao: SeriesDao = database.seriesDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `pagingSource should return only games that have series entries`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame, thirdGame))
        seriesDao.insertSeriesEntries(
            listOf(
                SeriesEntity(gameId = firstGame.id, position = 1),
                SeriesEntity(gameId = thirdGame.id, position = 0),
            ),
        )

        val page = loadSeriesPage()

        assertEquals(
            listOf(
                GameWithPlatforms(game = thirdGame, platforms = emptyList()),
                GameWithPlatforms(game = firstGame, platforms = emptyList()),
            ),
            page.data,
        )
    }

    @Test
    fun `pagingSource should order games by series position`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame, thirdGame))
        seriesDao.insertSeriesEntries(
            listOf(
                SeriesEntity(gameId = firstGame.id, position = 30),
                SeriesEntity(gameId = secondGame.id, position = 10),
                SeriesEntity(gameId = thirdGame.id, position = 20),
            ),
        )

        val page = loadSeriesPage()

        assertEquals(
            listOf(secondGame, thirdGame, firstGame),
            page.data.map(GameWithPlatforms::game),
        )
    }

    @Test
    fun `pagingSource should return games with their related platforms`() = runTest {
        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                games = listOf(firstGame, secondGame),
                platforms = listOf(firstPlatform, secondPlatform, thirdPlatform),
                crossRefs = listOf(
                    GamePlatformCrossRef(gameId = firstGame.id, platformId = thirdPlatform.id),
                    GamePlatformCrossRef(gameId = firstGame.id, platformId = firstPlatform.id),
                    GamePlatformCrossRef(gameId = secondGame.id, platformId = secondPlatform.id),
                ),
            ),
        )
        seriesDao.insertSeriesEntries(
            listOf(
                SeriesEntity(gameId = secondGame.id, position = 0),
                SeriesEntity(gameId = firstGame.id, position = 1),
            ),
        )

        val page = loadSeriesPage()

        assertEquals(
            listOf(
                GameWithPlatforms(
                    game = secondGame,
                    platforms = listOf(secondPlatform),
                ),
                GameWithPlatforms(
                    game = firstGame,
                    platforms = listOf(firstPlatform, thirdPlatform),
                ),
            ),
            page.data.normalized(),
        )
    }

    @Test
    fun `pagingSource should return empty platforms when a series game has no platform cross refs`() =
        runTest {
            gameDao.insertGames(listOf(firstGame))
            seriesDao.insertSeriesEntries(listOf(SeriesEntity(gameId = firstGame.id, position = 0)))

            val page = loadSeriesPage()

            assertEquals(
                listOf(GameWithPlatforms(game = firstGame, platforms = emptyList())),
                page.data,
            )
        }

    @Test
    fun `insertSeriesEntries should persist series rows`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))

        seriesDao.insertSeriesEntries(
            listOf(
                SeriesEntity(gameId = firstGame.id, position = 5),
                SeriesEntity(gameId = secondGame.id, position = 7),
            ),
        )

        assertEquals(
            listOf(
                SeriesEntity(gameId = firstGame.id, position = 5),
                SeriesEntity(gameId = secondGame.id, position = 7),
            ),
            readSeriesEntries(),
        )
    }

    @Test
    fun `insertSeriesEntries should replace an existing row with the same gameId`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        seriesDao.insertSeriesEntries(
            listOf(
                SeriesEntity(gameId = firstGame.id, position = 1),
                SeriesEntity(gameId = secondGame.id, position = 2),
            ),
        )

        seriesDao.insertSeriesEntries(listOf(SeriesEntity(gameId = firstGame.id, position = 9)))

        assertEquals(
            listOf(
                SeriesEntity(gameId = firstGame.id, position = 9),
                SeriesEntity(gameId = secondGame.id, position = 2),
            ),
            readSeriesEntries(),
        )
    }

    @Test
    fun `deleteByGameIds should delete only matching series rows`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame, thirdGame))
        seriesDao.insertSeriesEntries(
            listOf(
                SeriesEntity(gameId = firstGame.id, position = 0),
                SeriesEntity(gameId = secondGame.id, position = 1),
                SeriesEntity(gameId = thirdGame.id, position = 2),
            ),
        )

        seriesDao.deleteByGameIds(listOf(firstGame.id, thirdGame.id))

        assertEquals(
            listOf(SeriesEntity(gameId = secondGame.id, position = 1)),
            readSeriesEntries(),
        )
    }

    @Test
    fun `deleteByGameIds with unknown ids should be a no-op`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        seriesDao.insertSeriesEntries(
            listOf(
                SeriesEntity(gameId = firstGame.id, position = 3),
                SeriesEntity(gameId = secondGame.id, position = 4),
            ),
        )

        seriesDao.deleteByGameIds(listOf(999, 1000))

        assertEquals(
            listOf(
                SeriesEntity(gameId = firstGame.id, position = 3),
                SeriesEntity(gameId = secondGame.id, position = 4),
            ),
            readSeriesEntries(),
        )
    }

    @Test
    fun `deleteAll should clear all series rows`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame, thirdGame))
        seriesDao.insertSeriesEntries(
            listOf(
                SeriesEntity(gameId = firstGame.id, position = 0),
                SeriesEntity(gameId = secondGame.id, position = 1),
                SeriesEntity(gameId = thirdGame.id, position = 2),
            ),
        )

        seriesDao.deleteAll()

        assertEquals(emptyList(), readSeriesEntries())
    }

    @Test
    fun `should cascade delete series rows when a game is removed`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        seriesDao.insertSeriesEntries(
            listOf(
                SeriesEntity(gameId = firstGame.id, position = 0),
                SeriesEntity(gameId = secondGame.id, position = 1),
            ),
        )

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameEntity.TABLE_NAME} WHERE ${GameEntity.ID} = ?",
            arrayOf(firstGame.id),
        )

        assertEquals(
            listOf(SeriesEntity(gameId = secondGame.id, position = 1)),
            readSeriesEntries(),
        )
    }

    @Test
    fun `insertSeriesEntries should fail when a referenced game does not exist`() = runTest {
        gameDao.insertGames(listOf(fourthGame))

        val error = try {
            seriesDao.insertSeriesEntries(listOf(SeriesEntity(gameId = 999, position = 0)))
            null
        } catch (throwable: Throwable) {
            throwable
        }

        assertTrue(error != null)
        assertEquals(emptyList(), readSeriesEntries())
    }

    private suspend fun loadSeriesPage(): PagingSource.LoadResult.Page<Int, GameWithPlatforms> {
        return seriesDao.pagingSource().loadDaoRefreshPage()
    }

    private fun readSeriesEntries(): List<SeriesEntity> =
        database.query(
            """
            SELECT ${SeriesEntity.GAME_ID}, ${SeriesEntity.POSITION}
            FROM ${SeriesEntity.TABLE_NAME}
            ORDER BY ${SeriesEntity.GAME_ID}
            """.trimIndent(),
            null,
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        SeriesEntity(
                            gameId = cursor.getInt(0),
                            position = cursor.getLong(1),
                        ),
                    )
                }
            }
        }

    private fun List<GameWithPlatforms>.normalized(): List<GameWithPlatforms> =
        map { model ->
            model.copy(platforms = model.platforms.sortedBy(PlatformEntity::id))
        }
}
