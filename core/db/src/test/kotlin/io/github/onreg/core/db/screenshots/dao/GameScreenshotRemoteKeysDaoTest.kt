package io.github.onreg.core.db.screenshots.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.screenshots.entity.GameScreenshotRemoteKeysEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
internal class GameScreenshotRemoteKeysDaoTest {
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
    private val firstRemoteKeys = GameScreenshotRemoteKeysEntity(
        gameId = firstGame.id,
        prevKey = null,
        nextKey = 2,
    )
    private val secondRemoteKeys = GameScreenshotRemoteKeysEntity(
        gameId = secondGame.id,
        prevKey = 1,
        nextKey = 3,
    )

    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameDao = database.gameDao()
    private val gameScreenshotRemoteKeysDao = database.gameScreenshotRemoteKeysDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `getByGameId should return null when no remote keys exist for the requested game`() = runTest {
        gameDao.insertGames(listOf(firstGame))

        val result = gameScreenshotRemoteKeysDao.getByGameId(firstGame.id)

        assertNull(result)
    }

    @Test
    fun `insertRemoteKeys should persist remote keys for an existing game`() = runTest {
        gameDao.insertGames(listOf(firstGame))

        gameScreenshotRemoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys))

        assertEquals(firstRemoteKeys, gameScreenshotRemoteKeysDao.getByGameId(firstGame.id))
    }

    @Test
    fun `insertRemoteKeys should persist multiple remote key rows in a single call`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))

        gameScreenshotRemoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys, secondRemoteKeys))

        assertEquals(
            listOf(firstRemoteKeys, secondRemoteKeys),
            readRemoteKeysOrderedByGameId(),
        )
    }

    @Test
    fun `insertRemoteKeys should replace an existing row when the same gameId is inserted again`() =
        runTest {
            val updatedRemoteKeys = firstRemoteKeys.copy(prevKey = 4, nextKey = 6)

            gameDao.insertGames(listOf(firstGame))
            gameScreenshotRemoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys))

            gameScreenshotRemoteKeysDao.insertRemoteKeys(listOf(updatedRemoteKeys))

            assertEquals(updatedRemoteKeys, gameScreenshotRemoteKeysDao.getByGameId(firstGame.id))
        }

    @Test
    fun `insertRemoteKeys should replace only the matching gameId and keep other remote key rows unchanged`() =
        runTest {
            val updatedFirstRemoteKeys = firstRemoteKeys.copy(prevKey = 8, nextKey = 9)

            gameDao.insertGames(listOf(firstGame, secondGame))
            gameScreenshotRemoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys, secondRemoteKeys))

            gameScreenshotRemoteKeysDao.insertRemoteKeys(listOf(updatedFirstRemoteKeys))

            assertEquals(updatedFirstRemoteKeys, gameScreenshotRemoteKeysDao.getByGameId(firstGame.id))
            assertEquals(secondRemoteKeys, gameScreenshotRemoteKeysDao.getByGameId(secondGame.id))
            assertEquals(
                listOf(updatedFirstRemoteKeys, secondRemoteKeys),
                readRemoteKeysOrderedByGameId(),
            )
        }

    @Test
    fun `insertRemoteKeys should fail when inserting remote keys for a non-existent game`() =
        runTest {
            gameDao.insertGames(listOf(firstGame))

            assertFails {
                gameScreenshotRemoteKeysDao.insertRemoteKeys(
                    listOf(
                        GameScreenshotRemoteKeysEntity(
                            gameId = 999,
                            prevKey = null,
                            nextKey = 1,
                        ),
                    ),
                )
            }
            assertEquals(emptyList(), readRemoteKeysOrderedByGameId())
        }

    @Test
    fun `should cascade delete remote keys when the parent game is deleted`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        gameScreenshotRemoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys, secondRemoteKeys))

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameEntity.TABLE_NAME} WHERE ${GameEntity.ID} = ?",
            arrayOf(firstGame.id),
        )

        assertNull(gameScreenshotRemoteKeysDao.getByGameId(firstGame.id))
        assertEquals(secondRemoteKeys, gameScreenshotRemoteKeysDao.getByGameId(secondGame.id))
        assertEquals(listOf(secondRemoteKeys), readRemoteKeysOrderedByGameId())
    }

    @Test
    fun `insertRemoteKeys with an empty list should leave the table unchanged`() = runTest {
        gameDao.insertGames(listOf(firstGame))
        gameScreenshotRemoteKeysDao.insertRemoteKeys(listOf(firstRemoteKeys))

        gameScreenshotRemoteKeysDao.insertRemoteKeys(emptyList())

        assertEquals(firstRemoteKeys, gameScreenshotRemoteKeysDao.getByGameId(firstGame.id))
        assertEquals(listOf(firstRemoteKeys), readRemoteKeysOrderedByGameId())
    }

    private fun readRemoteKeysOrderedByGameId(): List<GameScreenshotRemoteKeysEntity> =
        database.openHelper.readableDatabase.query(
            "SELECT * FROM ${GameScreenshotRemoteKeysEntity.TABLE_NAME} " +
                "ORDER BY ${GameScreenshotRemoteKeysEntity.GAME_ID}",
        ).use { cursor ->
            buildList {
                val gameIdColumnIndex = cursor.getColumnIndexOrThrow(
                    GameScreenshotRemoteKeysEntity.GAME_ID,
                )
                val prevKeyColumnIndex = cursor.getColumnIndexOrThrow(
                    GameScreenshotRemoteKeysEntity.PREV_KEY,
                )
                val nextKeyColumnIndex = cursor.getColumnIndexOrThrow(
                    GameScreenshotRemoteKeysEntity.NEXT_KEY,
                )

                while (cursor.moveToNext()) {
                    add(
                        GameScreenshotRemoteKeysEntity(
                            gameId = cursor.getInt(gameIdColumnIndex),
                            prevKey = if (cursor.isNull(prevKeyColumnIndex)) {
                                null
                            } else {
                                cursor.getInt(prevKeyColumnIndex)
                            },
                            nextKey = if (cursor.isNull(nextKeyColumnIndex)) {
                                null
                            } else {
                                cursor.getInt(nextKeyColumnIndex)
                            },
                        ),
                    )
                }
            }
        }
}
