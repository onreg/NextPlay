package io.github.onreg.core.db.game.list.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
internal class GameListDaoTest {
    private data class PagingFixture(
        val games: List<GameEntity>,
        val platforms: List<PlatformEntity>,
        val crossRefs: List<GamePlatformCrossRef>,
        val listEntries: List<GameListEntity>,
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
        val fixture = pagingFixture()
        insertPagingFixture(fixture)
        val result = loadPagingSource()

        assertTrue(result is PagingSource.LoadResult.Page)
        assertEquals(listOf(10, 11, 12), result.data.map { it.game.id })
        assertPlatforms(result, gameId = 10, platformIds = setOf(1, 2))
        assertPlatforms(result, gameId = 11, platformIds = setOf(2))
        assertPlatforms(result, gameId = 12, platformIds = emptySet())
    }

    @Test
    fun `should clear only list membership when clearing list`() = runTest {
        val platform = PlatformEntity(1)
        val game = GameEntity(
            id = 20,
            title = "Cascade",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-03-01T00:00:00Z"),
            rating = 4.8,
        )
        val crossRef = GamePlatformCrossRef(gameId = game.id, platformId = platform.id)
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
        remoteKeysDao.deleteAll()

        assertEquals(1, countRows(GameEntity.TABLE_NAME))
        assertEquals(1, countRows(PlatformEntity.TABLE_NAME))
        assertEquals(1, countRows(GamePlatformCrossRef.TABLE_NAME))
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
                listOf(game),
                listOf(platform),
                listOf(crossRef),
            ),
        )

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${PlatformEntity.TABLE_NAME} WHERE ${PlatformEntity.ID} = ?",
            arrayOf(platform.id),
        )

        assertEquals(1, countRows(GameEntity.TABLE_NAME))
        assertEquals(0, countRows(GamePlatformCrossRef.TABLE_NAME))
    }

    @Test
    fun `should cascade delete only removed game cross refs when game is removed`() = runTest {
        val sharedPlatform = PlatformEntity(11)
        val firstOnlyPlatform = PlatformEntity(12)
        val firstGame = GameEntity(
            id = 40,
            title = "First Game",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-05-01T00:00:00Z"),
            rating = 4.4,
        )
        val secondGame = GameEntity(
            id = 41,
            title = "Second Game",
            imageUrl = "image",
            releaseDate = Instant.parse("2024-06-01T00:00:00Z"),
            rating = 4.1,
        )
        val crossRefs = listOf(
            GamePlatformCrossRef(gameId = firstGame.id, platformId = sharedPlatform.id),
            GamePlatformCrossRef(gameId = firstGame.id, platformId = firstOnlyPlatform.id),
            GamePlatformCrossRef(gameId = secondGame.id, platformId = sharedPlatform.id),
        )

        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                games = listOf(firstGame, secondGame),
                platforms = listOf(sharedPlatform, firstOnlyPlatform),
                crossRefs = crossRefs,
            ),
        )

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameEntity.TABLE_NAME} WHERE ${GameEntity.ID} = ?",
            arrayOf(firstGame.id),
        )

        val remainingCrossRefs = database
            .query(
                """
                SELECT ${GamePlatformCrossRef.GAME_ID}, ${GamePlatformCrossRef.PLATFORM_ID}
                FROM ${GamePlatformCrossRef.TABLE_NAME}
                """.trimIndent(),
                null,
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.getInt(0) to cursor.getInt(1))
                    }
                }
            }

        assertEquals(1, countRows(GameEntity.TABLE_NAME))
        assertEquals(setOf(secondGame.id to sharedPlatform.id), remainingCrossRefs.toSet())
    }

    private fun pagingFixture(): PagingFixture = PagingFixture(
        platforms = listOf(PlatformEntity(1), PlatformEntity(2), PlatformEntity(3)),
        games = listOf(
            game(id = 10, title = "First", releaseDate = "2024-01-01T00:00:00Z", rating = 4.5),
            game(id = 11, title = "Second", releaseDate = "2024-02-01T00:00:00Z", rating = 4.0),
            game(id = 12, title = "Third", releaseDate = "2024-03-01T00:00:00Z", rating = 3.8),
            game(
                id = 13,
                title = "Not In List",
                releaseDate = "2024-04-01T00:00:00Z",
                rating = 3.5,
            ),
        ),
        crossRefs = listOf(
            GamePlatformCrossRef(gameId = 10, platformId = 1),
            GamePlatformCrossRef(gameId = 10, platformId = 2),
            GamePlatformCrossRef(gameId = 11, platformId = 2),
            GamePlatformCrossRef(gameId = 13, platformId = 3),
        ),
        listEntries = listOf(
            GameListEntity(gameId = 10, position = 0),
            GameListEntity(gameId = 11, position = 1),
            GameListEntity(gameId = 12, position = 2),
        ),
    )

    private suspend fun insertPagingFixture(fixture: PagingFixture) {
        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(fixture.games, fixture.platforms, fixture.crossRefs),
        )
        gameListDao.insertGameListEntries(fixture.listEntries)
    }

    private suspend fun loadPagingSource(): PagingSource.LoadResult<Int, GameWithPlatforms> {
        val pagingSource = gameListDao.pagingSource()
        return pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false,
            ),
        )
    }

    private fun assertPlatforms(
        page: PagingSource.LoadResult.Page<Int, GameWithPlatforms>,
        gameId: Int,
        platformIds: Set<Int>,
    ) {
        val item = page.data.firstOrNull { it.game.id == gameId }
        assertNotNull(item)
        assertEquals(platformIds, item.platforms.map { it.id }.toSet())
    }

    private fun game(
        id: Int,
        title: String,
        releaseDate: String,
        rating: Double,
    ): GameEntity = GameEntity(
        id = id,
        title = title,
        imageUrl = "image$id",
        releaseDate = Instant.parse(releaseDate),
        rating = rating,
    )

    private fun countRows(table: String): Int =
        database.query("SELECT COUNT(*) FROM $table", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
}
