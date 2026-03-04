package io.github.onreg.core.db.game.list.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.list.entity.GameListEntity
import io.github.onreg.core.db.game.list.entity.GameListRemoteKeysEntity
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class GameListDaoTest {
    private val pagingPlatforms = listOf(PlatformEntity(1), PlatformEntity(2), PlatformEntity(3))
    private val pagingGames = listOf(
        GameEntity(
            id = 10,
            title = "First",
            imageUrl = "image10",
            releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
            rating = 4.5,
        ),
        GameEntity(
            id = 11,
            title = "Second",
            imageUrl = "image11",
            releaseDate = Instant.parse("2024-02-01T00:00:00Z"),
            rating = 4.0,
        ),
        GameEntity(
            id = 12,
            title = "Third",
            imageUrl = "image12",
            releaseDate = Instant.parse("2024-03-01T00:00:00Z"),
            rating = 3.8,
        ),
        GameEntity(
            id = 13,
            title = "Not In List",
            imageUrl = "image13",
            releaseDate = Instant.parse("2024-04-01T00:00:00Z"),
            rating = 3.5,
        ),
    )

    private val pagingInsertionBundle = GameInsertionBundle(
        games = pagingGames,
        platforms = pagingPlatforms,
        crossRefs = listOf(
            GamePlatformCrossRef(gameId = 10, platformId = 1),
            GamePlatformCrossRef(gameId = 10, platformId = 2),
            GamePlatformCrossRef(gameId = 11, platformId = 2),
            GamePlatformCrossRef(gameId = 13, platformId = 3),
        ),
    )

    private val pagingListEntries = listOf(
        GameListEntity(gameId = 10, position = 0),
        GameListEntity(gameId = 11, position = 1),
        GameListEntity(gameId = 12, position = 2),
    )

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
    fun `should return games with platforms ordered by insertion`() = runTest {
        gameDao.insertGamesWithPlatforms(pagingInsertionBundle)
        gameListDao.insertGameListEntries(pagingListEntries)

        val result = gameListDao.pagingSource().load(
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
                    game = pagingGames[0],
                    platforms = listOf(pagingPlatforms[0], pagingPlatforms[1]),
                ),
                GameWithPlatforms(
                    game = pagingGames[1],
                    platforms = listOf(pagingPlatforms[1]),
                ),
                GameWithPlatforms(
                    game = pagingGames[2],
                    platforms = emptyList(),
                ),
            ),
            result.data,
        )
    }

    @Test
    fun `should clear only list membership when clearing list`() = runTest {
        val game = pagingGames[0]
        val platform = pagingPlatforms[0]
        val crossRef = pagingInsertionBundle.crossRefs.first {
            it.gameId == game.id && it.platformId == platform.id
        }
        val remoteKey =
            GameListRemoteKeysEntity(
                gameId = game.id,
                prevKey = null,
                nextKey = 2,
            )

        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                listOf(game),
                listOf(platform),
                listOf(crossRef),
            ),
        )
        gameListDao.insertGameListEntries(
            listOf(GameListEntity(gameId = game.id, position = 0)),
        )
        remoteKeysDao.insertRemoteKeys(listOf(remoteKey))

        gameListDao.deleteAll()

        assertEquals(1, countRows(GameEntity.TABLE_NAME))
        assertEquals(1, countRows(PlatformEntity.TABLE_NAME))
        assertEquals(1, countRows(GamePlatformCrossRef.TABLE_NAME))
        assertEquals(0, countRows(GameListEntity.TABLE_NAME))
        assertEquals(0, countRows(GameListRemoteKeysEntity.TABLE_NAME))
    }

    private fun countRows(table: String): Int =
        database.query("SELECT COUNT(*) FROM $table", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
}
