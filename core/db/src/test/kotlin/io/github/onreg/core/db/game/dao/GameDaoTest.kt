package io.github.onreg.core.db.game.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.model.GameInsertionBundle
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
internal class GameDaoTest {
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

    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameDao = database.gameDao()
    private val platformDao = database.platformDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertGames should persist game rows`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))

        assertEquals(listOf(firstGame, secondGame), readGames())
    }

    @Test
    fun `insertGames should replace an existing game with the same id`() = runTest {
        val updatedGame = firstGame.copy(
            title = "Updated First Game",
            imageUrl = "https://example.com/updated-first.png",
            rating = 4.9,
        )

        gameDao.insertGames(listOf(firstGame, secondGame))
        gameDao.insertGames(listOf(updatedGame))

        assertEquals(listOf(updatedGame, secondGame), readGames())
    }

    @Test
    fun `insertGamesWithPlatforms should persist platforms games and cross refs`() = runTest {
        val bundle = GameInsertionBundle(
            games = listOf(firstGame, secondGame),
            platforms = listOf(firstPlatform, secondPlatform),
            crossRefs = listOf(
                GamePlatformCrossRef(gameId = firstGame.id, platformId = firstPlatform.id),
                GamePlatformCrossRef(gameId = secondGame.id, platformId = secondPlatform.id),
            ),
        )

        gameDao.insertGamesWithPlatforms(bundle)

        assertEquals(bundle.games, readGames())
        assertEquals(bundle.platforms, readPlatforms())
        assertEquals(bundle.crossRefs, readCrossRefs())
    }

    @Test
    fun `insertGamesWithPlatforms should allow shared platforms across multiple games`() = runTest {
        val sharedCrossRef = GamePlatformCrossRef(
            gameId = firstGame.id,
            platformId = firstPlatform.id,
        )
        val secondSharedCrossRef = GamePlatformCrossRef(
            gameId = secondGame.id,
            platformId = firstPlatform.id,
        )
        val secondExclusiveCrossRef = GamePlatformCrossRef(
            gameId = secondGame.id,
            platformId = secondPlatform.id,
        )

        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                games = listOf(firstGame, secondGame),
                platforms = listOf(firstPlatform, secondPlatform),
                crossRefs = listOf(sharedCrossRef, secondSharedCrossRef, secondExclusiveCrossRef),
            ),
        )

        assertEquals(listOf(firstPlatform, secondPlatform), readPlatforms())
        assertEquals(
            listOf(sharedCrossRef, secondSharedCrossRef, secondExclusiveCrossRef),
            readCrossRefs(),
        )
    }

    @Test
    fun `insertGamesWithPlatforms should ignore already existing platforms`() = runTest {
        platformDao.insertPlatforms(listOf(firstPlatform))

        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                games = listOf(firstGame),
                platforms = listOf(firstPlatform, secondPlatform),
                crossRefs = listOf(
                    GamePlatformCrossRef(gameId = firstGame.id, platformId = firstPlatform.id),
                    GamePlatformCrossRef(gameId = firstGame.id, platformId = secondPlatform.id),
                ),
            ),
        )

        assertEquals(listOf(firstPlatform, secondPlatform), readPlatforms())
        assertEquals(listOf(firstGame), readGames())
        assertEquals(
            listOf(
                GamePlatformCrossRef(gameId = firstGame.id, platformId = firstPlatform.id),
                GamePlatformCrossRef(gameId = firstGame.id, platformId = secondPlatform.id),
            ),
            readCrossRefs(),
        )
    }

    @Test
    fun `insertGamesWithPlatforms should rollback the whole operation when a cross ref is invalid`() =
        runTest {
            val bundle = GameInsertionBundle(
                games = listOf(firstGame),
                platforms = listOf(firstPlatform),
                crossRefs = listOf(
                    GamePlatformCrossRef(gameId = firstGame.id, platformId = thirdPlatform.id),
                ),
            )

            val error = try {
                gameDao.insertGamesWithPlatforms(bundle)
                null
            } catch (throwable: Throwable) {
                throwable
            }

            assertTrue(error != null)
            assertEquals(emptyList(), readGames())
            assertEquals(emptyList(), readPlatforms())
            assertEquals(emptyList(), readCrossRefs())
        }

    @Test
    fun `should cascade delete cross refs when a game is removed`() = runTest {
        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                games = listOf(firstGame, secondGame),
                platforms = listOf(firstPlatform, secondPlatform),
                crossRefs = listOf(
                    GamePlatformCrossRef(gameId = firstGame.id, platformId = firstPlatform.id),
                    GamePlatformCrossRef(gameId = secondGame.id, platformId = firstPlatform.id),
                    GamePlatformCrossRef(gameId = secondGame.id, platformId = secondPlatform.id),
                ),
            ),
        )

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameEntity.TABLE_NAME} WHERE ${GameEntity.ID} = ?",
            arrayOf(firstGame.id),
        )

        assertEquals(listOf(secondGame), readGames())
        assertEquals(listOf(firstPlatform, secondPlatform), readPlatforms())
        assertEquals(
            listOf(
                GamePlatformCrossRef(gameId = secondGame.id, platformId = firstPlatform.id),
                GamePlatformCrossRef(gameId = secondGame.id, platformId = secondPlatform.id),
            ),
            readCrossRefs(),
        )
    }

    @Test
    fun `should cascade delete cross refs when a platform is removed`() = runTest {
        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                games = listOf(firstGame, secondGame),
                platforms = listOf(firstPlatform, secondPlatform),
                crossRefs = listOf(
                    GamePlatformCrossRef(gameId = firstGame.id, platformId = firstPlatform.id),
                    GamePlatformCrossRef(gameId = secondGame.id, platformId = firstPlatform.id),
                    GamePlatformCrossRef(gameId = secondGame.id, platformId = secondPlatform.id),
                ),
            ),
        )

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${PlatformEntity.TABLE_NAME} WHERE ${PlatformEntity.ID} = ?",
            arrayOf(firstPlatform.id),
        )

        assertEquals(listOf(firstGame, secondGame), readGames())
        assertEquals(listOf(secondPlatform), readPlatforms())
        assertEquals(
            listOf(GamePlatformCrossRef(gameId = secondGame.id, platformId = secondPlatform.id)),
            readCrossRefs(),
        )
    }

    @Test
    fun `insertGamesWithPlatforms with an empty bundle should be a no-op`() = runTest {
        gameDao.insertGamesWithPlatforms(
            GameInsertionBundle(
                games = emptyList(),
                platforms = emptyList(),
                crossRefs = emptyList(),
            ),
        )

        assertEquals(emptyList(), readGames())
        assertEquals(emptyList(), readPlatforms())
        assertEquals(emptyList(), readCrossRefs())
    }

    @Test
    fun `insertGamesWithPlatforms should not create duplicate cross refs for the same game and platform`() =
        runTest {
            val crossRef = GamePlatformCrossRef(
                gameId = thirdGame.id,
                platformId = thirdPlatform.id,
            )

            gameDao.insertGamesWithPlatforms(
                GameInsertionBundle(
                    games = listOf(thirdGame),
                    platforms = listOf(thirdPlatform),
                    crossRefs = listOf(crossRef, crossRef),
                ),
            )

            assertEquals(listOf(thirdGame), readGames())
            assertEquals(listOf(thirdPlatform), readPlatforms())
            assertEquals(listOf(crossRef), readCrossRefs())
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

    private fun readPlatforms(): List<PlatformEntity> =
        database.query(
            """
            SELECT ${PlatformEntity.ID}
            FROM ${PlatformEntity.TABLE_NAME}
            ORDER BY ${PlatformEntity.ID}
            """.trimIndent(),
            null,
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(PlatformEntity(id = cursor.getInt(0)))
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
}
