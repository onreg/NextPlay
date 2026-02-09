package io.github.onreg.core.db.series.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.db.series.entity.SeriesEntity
import io.github.onreg.core.db.series.entity.SeriesRemoteKeysEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
internal class SeriesDaoTest {
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
    fun `should return games with platforms ordered by series position`() = runTest {
        val fixture = insertOrderedSeriesFixture()

        val result = seriesDao.pagingSource().load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false,
            ),
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        assertEquals(
            listOf(
                GameWithPlatforms(
                    game = fixture.secondSeriesGame,
                    platforms = listOf(fixture.secondPlatform),
                ),
                GameWithPlatforms(
                    game = fixture.firstSeriesGame,
                    platforms = listOf(fixture.firstPlatform, fixture.secondPlatform),
                ),
            ),
            result.data,
        )
    }

    @Test
    fun `should clear only series membership when clearing series`() = runTest {
        val platform = PlatformEntity(id = 3)
        val game = GameEntity(
            id = 7010,
            title = "Series",
            imageUrl = "image",
            releaseDate = Instant.parse("2025-08-01T00:00:00Z"),
            rating = 4.9,
        )
        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                games = listOf(game),
                platforms = listOf(platform),
                crossRefs = listOf(
                    GamePlatformCrossRef(
                        gameId = game.id,
                        platformId = platform.id,
                    ),
                ),
            ),
        )
        seriesDao.insertSeriesEntries(listOf(SeriesEntity(gameId = game.id, position = 0)))
        seriesRemoteKeysDao.insertRemoteKeys(
            listOf(
                SeriesRemoteKeysEntity(
                    gameId = game.id,
                    prevKey = null,
                    nextKey = 2,
                ),
            ),
        )

        seriesDao.deleteAll()
        seriesRemoteKeysDao.deleteAll()

        assertEquals(1, countRows(GameEntity.TABLE_NAME))
        assertEquals(1, countRows(PlatformEntity.TABLE_NAME))
        assertEquals(1, countRows(GamePlatformCrossRef.TABLE_NAME))
        assertEquals(0, countRows(SeriesEntity.TABLE_NAME))
        assertEquals(0, countRows(SeriesRemoteKeysEntity.TABLE_NAME))
    }

    private suspend fun insertOrderedSeriesFixture(): SeriesOrderingFixture {
        val firstPlatform = PlatformEntity(id = 1)
        val secondPlatform = PlatformEntity(id = 2)
        val firstSeriesGame = game(
            id = 7001,
            title = "First",
            releaseDate = "2025-05-01T00:00:00Z",
            rating = 4.2,
        )
        val secondSeriesGame = game(
            id = 7002,
            title = "Second",
            releaseDate = "2025-06-01T00:00:00Z",
            rating = 4.7,
        )
        val nonSeriesGame = game(
            id = 7003,
            title = "Other",
            releaseDate = "2025-07-01T00:00:00Z",
            rating = 3.9,
        )

        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                games = listOf(firstSeriesGame, secondSeriesGame, nonSeriesGame),
                platforms = listOf(firstPlatform, secondPlatform),
                crossRefs = listOf(
                    GamePlatformCrossRef(
                        gameId = firstSeriesGame.id,
                        platformId = firstPlatform.id,
                    ),
                    GamePlatformCrossRef(
                        gameId = firstSeriesGame.id,
                        platformId = secondPlatform.id,
                    ),
                    GamePlatformCrossRef(
                        gameId = secondSeriesGame.id,
                        platformId = secondPlatform.id,
                    ),
                    GamePlatformCrossRef(
                        gameId = nonSeriesGame.id,
                        platformId = firstPlatform.id,
                    ),
                ),
            ),
        )
        seriesDao.insertSeriesEntries(
            listOf(
                SeriesEntity(gameId = firstSeriesGame.id, position = 1),
                SeriesEntity(gameId = secondSeriesGame.id, position = 0),
            ),
        )
        return SeriesOrderingFixture(
            firstPlatform = firstPlatform,
            secondPlatform = secondPlatform,
            firstSeriesGame = firstSeriesGame,
            secondSeriesGame = secondSeriesGame,
        )
    }

    private fun game(
        id: Int,
        title: String,
        releaseDate: String,
        rating: Double,
    ): GameEntity = GameEntity(
        id = id,
        title = title,
        imageUrl = title.lowercase(),
        releaseDate = Instant.parse(releaseDate),
        rating = rating,
    )

    private fun countRows(table: String): Int =
        database.query("SELECT COUNT(*) FROM $table", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    private data class SeriesOrderingFixture(
        val firstPlatform: PlatformEntity,
        val secondPlatform: PlatformEntity,
        val firstSeriesGame: GameEntity,
        val secondSeriesGame: GameEntity,
    )
}
