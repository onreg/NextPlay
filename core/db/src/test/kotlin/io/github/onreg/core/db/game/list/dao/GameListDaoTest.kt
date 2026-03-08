package io.github.onreg.core.db.game.list.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.list.entity.GameListEntity
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
internal class GameListDaoTest {
    private val firstGame = GameEntity(
        id = 101,
        title = "First Game",
        imageUrl = "https://example.com/first.png",
        releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
        rating = 4.2,
    )
    private val secondGame = GameEntity(
        id = 102,
        title = "Second Game",
        imageUrl = "https://example.com/second.png",
        releaseDate = Instant.parse("2024-02-02T00:00:00Z"),
        rating = 4.5,
    )
    private val thirdGame = GameEntity(
        id = 103,
        title = "Third Game",
        imageUrl = "https://example.com/third.png",
        releaseDate = Instant.parse("2024-03-03T00:00:00Z"),
        rating = 4.8,
    )
    private val firstPlatform = PlatformEntity(id = 201)
    private val secondPlatform = PlatformEntity(id = 202)
    private val thirdPlatform = PlatformEntity(id = 203)
    private val firstListEntry = GameListEntity(gameId = firstGame.id, position = 2)
    private val secondListEntry = GameListEntity(gameId = secondGame.id, position = 0)
    private val thirdListEntry = GameListEntity(gameId = thirdGame.id, position = 1)

    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameDao: GameDao = database.gameDao()
    private val gameListDao = database.gameListDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `pagingSource should return an empty page when there are no list entries`() = runTest {
        gameDao.insertGames(listOf(firstGame))

        assertEquals(emptyList(), loadPage())
    }

    @Test
    fun `pagingSource should return only games present in the game list`() = runTest {
        insertGamesWithPlatforms(
            games = listOf(firstGame, secondGame, thirdGame),
            platforms = listOf(firstPlatform, secondPlatform, thirdPlatform),
            crossRefs = listOf(
                GamePlatformCrossRef(gameId = firstGame.id, platformId = firstPlatform.id),
                GamePlatformCrossRef(gameId = secondGame.id, platformId = secondPlatform.id),
                GamePlatformCrossRef(gameId = thirdGame.id, platformId = thirdPlatform.id),
            ),
        )
        gameListDao.insertGameListEntries(listOf(firstListEntry, thirdListEntry))

        assertEquals(
            listOf(
                GameWithPlatforms(game = thirdGame, platforms = listOf(thirdPlatform)),
                GameWithPlatforms(game = firstGame, platforms = listOf(firstPlatform)),
            ),
            loadPage(),
        )
    }

    @Test
    fun `pagingSource should order results by list position rather than insertion order or game id`() =
        runTest {
            insertGamesWithPlatforms(
                games = listOf(firstGame, secondGame, thirdGame),
                platforms = listOf(firstPlatform, secondPlatform, thirdPlatform),
                crossRefs = listOf(
                    GamePlatformCrossRef(gameId = firstGame.id, platformId = firstPlatform.id),
                    GamePlatformCrossRef(gameId = secondGame.id, platformId = secondPlatform.id),
                    GamePlatformCrossRef(gameId = thirdGame.id, platformId = thirdPlatform.id),
                ),
            )
            gameListDao.insertGameListEntries(listOf(firstListEntry, secondListEntry, thirdListEntry))

            assertEquals(
                listOf(
                    GameWithPlatforms(game = secondGame, platforms = listOf(secondPlatform)),
                    GameWithPlatforms(game = thirdGame, platforms = listOf(thirdPlatform)),
                    GameWithPlatforms(game = firstGame, platforms = listOf(firstPlatform)),
                ),
                loadPage(),
            )
        }

    @Test
    fun `pagingSource should load platforms for each returned game`() = runTest {
        insertGamesWithPlatforms(
            games = listOf(firstGame, secondGame),
            platforms = listOf(firstPlatform, secondPlatform, thirdPlatform),
            crossRefs = listOf(
                GamePlatformCrossRef(gameId = firstGame.id, platformId = firstPlatform.id),
                GamePlatformCrossRef(gameId = firstGame.id, platformId = secondPlatform.id),
                GamePlatformCrossRef(gameId = secondGame.id, platformId = thirdPlatform.id),
            ),
        )
        gameListDao.insertGameListEntries(listOf(firstListEntry, secondListEntry))

        val page = loadPage()

        assertEquals(
            mapOf(
                secondGame.id to setOf(thirdPlatform),
                firstGame.id to setOf(firstPlatform, secondPlatform),
            ),
            page.associate { it.game.id to it.platforms.toSet() },
        )
    }

    @Test
    fun `insertGameListEntries should persist list entries for existing games`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))

        gameListDao.insertGameListEntries(listOf(firstListEntry, secondListEntry))

        assertEquals(listOf(secondListEntry, firstListEntry), readListEntries())
    }

    @Test
    fun `insertGameListEntries should replace an existing entry for the same game id`() = runTest {
        val updatedFirstListEntry = firstListEntry.copy(position = 5)

        gameDao.insertGames(listOf(firstGame))
        gameListDao.insertGameListEntries(listOf(firstListEntry))
        gameListDao.insertGameListEntries(listOf(updatedFirstListEntry))

        assertEquals(listOf(updatedFirstListEntry), readListEntries())
        assertEquals(1, countRows(GameListEntity.TABLE_NAME))
    }

    @Test
    fun `insertGameListEntries should fail when the referenced game does not exist`() = runTest {
        val error = try {
            gameListDao.insertGameListEntries(listOf(firstListEntry))
            null
        } catch (throwable: Throwable) {
            throwable
        }

        assertNotNull(error)
        assertEquals(emptyList(), readListEntries())
        assertEquals(0, countRows(GameListEntity.TABLE_NAME))
    }

    @Test
    fun `insertGameListEntries with an empty list should be a no-op`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        gameListDao.insertGameListEntries(listOf(firstListEntry, secondListEntry))

        gameListDao.insertGameListEntries(emptyList())

        assertEquals(listOf(secondListEntry, firstListEntry), readListEntries())
    }

    @Test
    fun `deleteAll should clear all game list entries`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        gameListDao.insertGameListEntries(listOf(firstListEntry, secondListEntry))

        gameListDao.deleteAll()

        assertEquals(emptyList(), readListEntries())
        assertEquals(emptyList(), loadPage())
    }

    @Test
    fun `deleteAll should not delete games or platform relations`() = runTest {
        val crossRefs = listOf(
            GamePlatformCrossRef(gameId = firstGame.id, platformId = firstPlatform.id),
            GamePlatformCrossRef(gameId = secondGame.id, platformId = secondPlatform.id),
        )

        insertGamesWithPlatforms(
            games = listOf(firstGame, secondGame),
            platforms = listOf(firstPlatform, secondPlatform),
            crossRefs = crossRefs,
        )
        gameListDao.insertGameListEntries(listOf(firstListEntry, secondListEntry))

        gameListDao.deleteAll()

        assertEquals(listOf(firstGame, secondGame), readGames())
        assertEquals(crossRefs, readCrossRefs())
        assertEquals(emptyList(), loadPage())
    }

    @Test
    fun `deleteAll should be idempotent`() = runTest {
        gameDao.insertGames(listOf(firstGame))
        gameListDao.insertGameListEntries(listOf(firstListEntry))

        gameListDao.deleteAll()
        gameListDao.deleteAll()

        assertEquals(emptyList(), readListEntries())
        assertEquals(emptyList(), loadPage())
    }

    @Test
    fun `should cascade delete list entries when a game is removed`() = runTest {
        insertGamesWithPlatforms(
            games = listOf(firstGame, secondGame),
            platforms = listOf(firstPlatform, secondPlatform),
            crossRefs = listOf(
                GamePlatformCrossRef(gameId = firstGame.id, platformId = firstPlatform.id),
                GamePlatformCrossRef(gameId = secondGame.id, platformId = secondPlatform.id),
            ),
        )
        gameListDao.insertGameListEntries(listOf(firstListEntry, secondListEntry))

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameEntity.TABLE_NAME} WHERE ${GameEntity.ID} = ?",
            arrayOf(firstGame.id),
        )

        assertEquals(listOf(secondListEntry), readListEntries())
        assertEquals(
            listOf(GameWithPlatforms(game = secondGame, platforms = listOf(secondPlatform))),
            loadPage(),
        )
    }

    private suspend fun insertGamesWithPlatforms(
        games: List<GameEntity>,
        platforms: List<PlatformEntity>,
        crossRefs: List<GamePlatformCrossRef>,
    ) {
        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                games = games,
                platforms = platforms,
                crossRefs = crossRefs,
            ),
        )
    }

    private suspend fun loadPage(): List<GameWithPlatforms> {
        val result = gameListDao.pagingSource().load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 50,
                placeholdersEnabled = false,
            ),
        )

        return assertIs<PagingSource.LoadResult.Page<Int, GameWithPlatforms>>(result).data
    }

    private fun readListEntries(): List<GameListEntity> =
        database.query(
            """
            SELECT ${GameListEntity.GAME_ID}, ${GameListEntity.POSITION}
            FROM ${GameListEntity.TABLE_NAME}
            ORDER BY ${GameListEntity.POSITION}, ${GameListEntity.GAME_ID}
            """.trimIndent(),
            null,
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        GameListEntity(
                            gameId = cursor.getInt(0),
                            position = cursor.getLong(1),
                        ),
                    )
                }
            }
        }

    private fun readGames(): List<GameEntity> =
        database.query(
            """
            SELECT ${GameEntity.ID}, ${GameEntity.TITLE}, ${GameEntity.IMAGE_URL},
                ${GameEntity.RELEASE_DATE}, ${GameEntity.RATING}
            FROM ${GameEntity.TABLE_NAME}
            ORDER BY ${GameEntity.ID}
            """.trimIndent(),
            null,
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        GameEntity(
                            id = cursor.getInt(0),
                            title = cursor.getString(1),
                            imageUrl = cursor.getString(2),
                            releaseDate = if (cursor.isNull(3)) {
                                null
                            } else {
                                Instant.ofEpochMilli(cursor.getLong(3))
                            },
                            rating = cursor.getDouble(4),
                        ),
                    )
                }
            }
        }

    private fun readCrossRefs(): List<GamePlatformCrossRef> =
        database.query(
            """
            SELECT ${GamePlatformCrossRef.GAME_ID}, ${GamePlatformCrossRef.PLATFORM_ID}
            FROM ${GamePlatformCrossRef.TABLE_NAME}
            ORDER BY ${GamePlatformCrossRef.GAME_ID}, ${GamePlatformCrossRef.PLATFORM_ID}
            """.trimIndent(),
            null,
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        GamePlatformCrossRef(
                            gameId = cursor.getInt(0),
                            platformId = cursor.getInt(1),
                        ),
                    )
                }
            }
        }

    private fun countRows(tableName: String): Int =
        database.query("SELECT COUNT(*) FROM $tableName", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
}
