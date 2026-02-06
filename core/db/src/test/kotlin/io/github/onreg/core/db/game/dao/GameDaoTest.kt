package io.github.onreg.core.db.game.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GameListEntity
import io.github.onreg.core.db.game.entity.GameListRemoteKeysEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.model.GameInsertionBundle
import io.github.onreg.core.db.game.model.GameWithPlatforms
import io.github.onreg.core.db.platform.entity.PlatformEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
internal class GameDaoTest {
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
    fun `should return games with platforms ordered by list membership`() = runTest {
        val listKey = "default"
        val platforms = listOf(PlatformEntity(1), PlatformEntity(2))
        val games = listOf(
            GameEntity(
                id = 10,
                title = "First",
                imageUrl = "image1",
                releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
                rating = 4.5,
            ),
            GameEntity(
                id = 11,
                title = "Second",
                imageUrl = "image2",
                releaseDate = Instant.parse("2024-02-01T00:00:00Z"),
                rating = 4.0,
            ),
        )
        val listEntities = listOf(
            GameListEntity(listKey = listKey, gameId = 10, insertionOrder = 1),
            GameListEntity(listKey = listKey, gameId = 11, insertionOrder = 2),
        )
        val crossRefs = listOf(
            GamePlatformCrossRef(gameId = 10, platformId = 1),
            GamePlatformCrossRef(gameId = 10, platformId = 2),
            GamePlatformCrossRef(gameId = 11, platformId = 2),
        )

        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                games = games,
                listEntities = emptyList(),
                platforms = platforms,
                crossRefs = crossRefs,
            ),
        )
        gameListDao.insertAll(listEntities)

        val pagingSource = gameListDao.pagingSource(listKey)
        val result = pagingSource.load(
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
                    game = games[0],
                    platforms = listOf(platforms[0], platforms[1]),
                ),
                GameWithPlatforms(
                    game = games[1],
                    platforms = listOf(platforms[1]),
                ),
            ),
            result.data,
        )
    }

    @Test
    fun `should cascade delete cross refs and remote keys when clearing games`() = runTest {
        val listKey = "default"
        val platform = PlatformEntity(1)
        val game = GameEntity(
            id = 20,
            title = "Cascade",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-03-01T00:00:00Z"),
            rating = 4.8,
        )
        val listEntity = GameListEntity(listKey = listKey, gameId = game.id, insertionOrder = 1)
        val crossRef = GamePlatformCrossRef(gameId = game.id, platformId = platform.id)
        val remoteKey = GameListRemoteKeysEntity(
            listKey = listKey,
            gameId = game.id,
            prevKey = null,
            nextKey = 2,
        )

        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                games = listOf(game),
                listEntities = emptyList(),
                platforms = listOf(platform),
                crossRefs = listOf(crossRef),
            ),
        )
        gameListDao.insertAll(listOf(listEntity))
        remoteKeysDao.insertRemoteKeys(listOf(remoteKey))

        gameDao.clearGames()

        assertEquals(0, countRows(GameEntity.TABLE_NAME))
        assertEquals(0, countRows(GamePlatformCrossRef.TABLE_NAME))
        assertEquals(0, countRows(GameListEntity.TABLE_NAME))
        assertEquals(0, countRows(GameListRemoteKeysEntity.TABLE_NAME))
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
        )
        val crossRef = GamePlatformCrossRef(gameId = game.id, platformId = platform.id)

        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                games = listOf(game),
                listEntities = emptyList(),
                platforms = listOf(platform),
                crossRefs = listOf(crossRef),
            ),
        )

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${PlatformEntity.TABLE_NAME} WHERE ${PlatformEntity.ID} = ?",
            arrayOf(platform.id),
        )

        assertEquals(1, countRows(GameEntity.TABLE_NAME))
        assertEquals(0, countRows(GamePlatformCrossRef.TABLE_NAME))
    }

    private fun countRows(table: String): Int =
        database.query("SELECT COUNT(*) FROM $table", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
}
