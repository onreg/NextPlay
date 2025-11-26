package io.github.onreg.core.db.game.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.entity.GameRemoteKeysEntity
import io.github.onreg.core.db.game.entity.GameWithPlatformsEntity
import io.github.onreg.core.db.platform.entity.PlatformEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
internal class GameDaoTest {

    private lateinit var database: NextPlayDatabase
    private lateinit var gameDao: GameDao
    private lateinit var remoteKeysDao: GameRemoteKeysDao

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        gameDao = database.gameDao()
        remoteKeysDao = database.gameRemoteKeysDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `should return games with platforms ordered by insertion`() = runTest {
        val platforms = listOf(PlatformEntity(1), PlatformEntity(2))
        val games = listOf(
            GameEntity(
                id = 10,
                title = "First",
                imageUrl = "image1",
                releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
                rating = 4.5,
                insertionOrder = 1
            ),
            GameEntity(
                id = 11,
                title = "Second",
                imageUrl = "image2",
                releaseDate = Instant.parse("2024-02-01T00:00:00Z"),
                rating = 4.0,
                insertionOrder = 2
            )
        )
        val crossRefs = listOf(
            GamePlatformCrossRef(gameId = 10, platformId = 1),
            GamePlatformCrossRef(gameId = 10, platformId = 2),
            GamePlatformCrossRef(gameId = 11, platformId = 2)
        )

        gameDao.insertGamesWithPlatforms(games, platforms, crossRefs)

        val pagingSource = gameDao.pagingSource()
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        assertEquals(
            listOf(
                GameWithPlatformsEntity(
                    game = games[0],
                    platforms = listOf(platforms[0], platforms[1])
                ),
                GameWithPlatformsEntity(
                    game = games[1],
                    platforms = listOf(platforms[1])
                )
            ),
            result.data
        )
    }

    @Test
    fun `should cascade delete cross refs and remote keys when clearing games`() = runTest {
        val platform = PlatformEntity(1)
        val game = GameEntity(
            id = 20,
            title = "Cascade",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-03-01T00:00:00Z"),
            rating = 4.8,
            insertionOrder = 1
        )
        val crossRef = GamePlatformCrossRef(gameId = game.id, platformId = platform.id)
        val remoteKey = GameRemoteKeysEntity(gameId = game.id, prevKey = null, nextKey = 2)

        gameDao.insertGamesWithPlatforms(listOf(game), listOf(platform), listOf(crossRef))
        remoteKeysDao.insertRemoteKeys(listOf(remoteKey))

        gameDao.clearGames()

        assertEquals(0, countRows(GameEntity.TABLE_NAME))
        assertEquals(0, countRows(GamePlatformCrossRef.TABLE_NAME))
        assertEquals(0, countRows(GameRemoteKeysEntity.TABLE_NAME))
    }

    @Test
    fun `should cascade delete cross refs when platform is removed`() = runTest {
        val platform = PlatformEntity(1)
        val game = GameEntity(
            id = 30,
            title = "Platform",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-04-01T00:00:00Z"),
            rating = 4.2,
            insertionOrder = 1
        )
        val crossRef = GamePlatformCrossRef(gameId = game.id, platformId = platform.id)

        gameDao.insertGamesWithPlatforms(listOf(game), listOf(platform), listOf(crossRef))

        database.query(
            "DELETE FROM ${PlatformEntity.TABLE_NAME} WHERE ${PlatformEntity.ID} = ${platform.id}",
            null
        )

        assertEquals(1, countRows(GameEntity.TABLE_NAME))
        assertEquals(0, countRows(GamePlatformCrossRef.TABLE_NAME))
    }

    private fun countRows(table: String): Int {
        return database.query("SELECT COUNT(*) FROM $table", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
    }
}
