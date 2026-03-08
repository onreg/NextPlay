package io.github.onreg.core.db.details.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.company.entity.GameCompanyEntity
import io.github.onreg.core.db.company.entity.GameCompanyRoleEntity
import io.github.onreg.core.db.details.entity.GameDetailsCompanyCrossRef
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.details.entity.GameDetailsPlatformCrossRef
import io.github.onreg.core.db.details.model.GameDetailsInsertionBundle
import io.github.onreg.core.db.details.model.GameDetailsWithPlatformsAndCompanies
import io.github.onreg.core.db.platform.entity.PlatformEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class GameDetailsDaoTest {
    private val firstDetails = GameDetailsEntity(
        gameId = 101,
        title = "First Game",
        imageUrl = "https://example.com/first.png",
        releaseDate = null,
        website = "https://example.com/first",
        rating = 4.2,
        description = "First game description",
    )
    private val secondDetails = GameDetailsEntity(
        gameId = 102,
        title = "Second Game",
        imageUrl = "https://example.com/second.png",
        releaseDate = null,
        website = "https://example.com/second",
        rating = 4.5,
        description = "Second game description",
    )
    private val updatedFirstDetails = firstDetails.copy(
        title = "Updated First Game",
        imageUrl = "https://example.com/updated-first.png",
        releaseDate = Instant.parse("2024-03-03T00:00:00Z"),
        website = null,
        rating = 4.9,
        description = "Updated first game description",
    )
    private val releasedDetails = secondDetails.copy(
        gameId = 103,
        title = "Released Game",
        imageUrl = "https://example.com/released.png",
        releaseDate = Instant.parse("2024-04-04T12:34:56Z"),
        website = "https://example.com/released",
        rating = 4.7,
        description = "Released game description",
    )
    private val firstPlatform = PlatformEntity(id = 201)
    private val secondPlatform = PlatformEntity(id = 202)
    private val thirdPlatform = PlatformEntity(id = 203)
    private val firstCompany = GameCompanyEntity(
        id = "company-401",
        name = "First Studio",
        logoUrl = "https://example.com/first-company.png",
        role = GameCompanyRoleEntity.Developer,
    )
    private val secondCompany = GameCompanyEntity(
        id = "company-402",
        name = "Second Publishing",
        logoUrl = "https://example.com/second-company.png",
        role = GameCompanyRoleEntity.Publisher,
    )
    private val thirdCompany = GameCompanyEntity(
        id = "company-403",
        name = "Third Studio",
        logoUrl = null,
        role = GameCompanyRoleEntity.Developer,
    )
    private val updatedFirstCompany = firstCompany.copy(
        name = "Updated First Studio",
        logoUrl = "https://example.com/updated-first-company.png",
    )

    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameDetailsDao = database.gameDetailsDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `observeGame should emit null when the game does not exist`() = runTest {
        assertEquals(null, observeStoredGame(firstDetails.gameId))
    }

    @Test
    fun `insertGameDetails should persist all scalar fields on details`() = runTest {
        gameDetailsDao.insertGameDetails(
            GameDetailsInsertionBundle(
                details = firstDetails,
                platforms = emptyList(),
                platformCrossRefs = emptyList(),
                companies = emptyList(),
                companyCrossRefs = emptyList(),
            ),
        )

        assertEquals(firstDetails, readDetailsRow(firstDetails.gameId))
    }

    @Test
    fun `insertGameDetails should persist a non-null releaseDate through the Instant converter`() =
        runTest {
            gameDetailsDao.insertGameDetails(
                GameDetailsInsertionBundle(
                    details = releasedDetails,
                    platforms = emptyList(),
                    platformCrossRefs = emptyList(),
                    companies = emptyList(),
                    companyCrossRefs = emptyList(),
                ),
            )

            assertEquals(
                releasedDetails.releaseDate?.toEpochMilli(),
                readStoredReleaseDate(releasedDetails.gameId),
            )
            assertEquals(
                releasedDetails.releaseDate,
                observeStoredGame(releasedDetails.gameId)?.details?.releaseDate,
            )
        }

    @Test
    fun `insertGameDetails should roll back when a platform cross ref points to a missing platform`() =
        runTest {
            val error = try {
                gameDetailsDao.insertGameDetails(
                    GameDetailsInsertionBundle(
                        details = firstDetails,
                        platforms = listOf(firstPlatform),
                        platformCrossRefs = listOf(
                            GameDetailsPlatformCrossRef(
                                gameId = firstDetails.gameId,
                                platformId = secondPlatform.id,
                            ),
                        ),
                        companies = emptyList(),
                        companyCrossRefs = emptyList(),
                    ),
                )
                null
            } catch (throwable: Throwable) {
                throwable
            }

            assertTrue(error != null)
            assertEquals(0, countRows(GameDetailsEntity.TABLE_NAME))
            assertEquals(0, countRows(PlatformEntity.TABLE_NAME))
            assertEquals(0, countRows(GameDetailsPlatformCrossRef.TABLE_NAME))
        }

    @Test
    fun `insertGameDetails should roll back when a company cross ref points to a missing company`() =
        runTest {
            val error = try {
                gameDetailsDao.insertGameDetails(
                    GameDetailsInsertionBundle(
                        details = firstDetails,
                        platforms = listOf(firstPlatform),
                        platformCrossRefs = listOf(
                            GameDetailsPlatformCrossRef(
                                gameId = firstDetails.gameId,
                                platformId = firstPlatform.id,
                            ),
                        ),
                        companies = listOf(firstCompany),
                        companyCrossRefs = listOf(
                            GameDetailsCompanyCrossRef(
                                gameId = firstDetails.gameId,
                                companyId = secondCompany.id,
                            ),
                        ),
                    ),
                )
                null
            } catch (throwable: Throwable) {
                throwable
            }

            assertTrue(error != null)
            assertEquals(0, countRows(GameDetailsEntity.TABLE_NAME))
            assertEquals(0, countRows(PlatformEntity.TABLE_NAME))
            assertEquals(0, countRows(GameDetailsPlatformCrossRef.TABLE_NAME))
            assertEquals(0, countRows(GameCompanyEntity.TABLE_NAME))
            assertEquals(0, countRows(GameDetailsCompanyCrossRef.TABLE_NAME))
        }

    @Test
    fun `insertGameDetails should replace an existing game details row for the same game id`() =
        runTest {
            gameDetailsDao.insertGameDetails(
                GameDetailsInsertionBundle(
                    details = firstDetails,
                    platforms = emptyList(),
                    platformCrossRefs = emptyList(),
                    companies = emptyList(),
                    companyCrossRefs = emptyList(),
                ),
            )

            gameDetailsDao.insertGameDetails(
                GameDetailsInsertionBundle(
                    details = updatedFirstDetails,
                    platforms = emptyList(),
                    platformCrossRefs = emptyList(),
                    companies = emptyList(),
                    companyCrossRefs = emptyList(),
                ),
            )

            assertEquals(
                GameDetailsWithPlatformsAndCompanies(
                    details = updatedFirstDetails,
                    platforms = emptyList(),
                    companies = emptyList(),
                ),
                observeStoredGame(firstDetails.gameId),
            )
            assertEquals(1, countRows(GameDetailsEntity.TABLE_NAME))
        }

    @Test
    fun `insertGameDetails should refresh platform relations when the same game id is inserted again`() =
        runTest {
            gameDetailsDao.insertGameDetails(
                GameDetailsInsertionBundle(
                    details = firstDetails,
                    platforms = listOf(firstPlatform, secondPlatform),
                    platformCrossRefs = listOf(
                        GameDetailsPlatformCrossRef(
                            gameId = firstDetails.gameId,
                            platformId = firstPlatform.id,
                        ),
                        GameDetailsPlatformCrossRef(
                            gameId = firstDetails.gameId,
                            platformId = secondPlatform.id,
                        ),
                    ),
                    companies = emptyList(),
                    companyCrossRefs = emptyList(),
                ),
            )

            gameDetailsDao.insertGameDetails(
                GameDetailsInsertionBundle(
                    details = updatedFirstDetails,
                    platforms = listOf(thirdPlatform),
                    platformCrossRefs = listOf(
                        GameDetailsPlatformCrossRef(
                            gameId = updatedFirstDetails.gameId,
                            platformId = thirdPlatform.id,
                        ),
                    ),
                    companies = emptyList(),
                    companyCrossRefs = emptyList(),
                ),
            )

            assertEquals(
                GameDetailsWithPlatformsAndCompanies(
                    details = updatedFirstDetails,
                    platforms = listOf(thirdPlatform),
                    companies = emptyList(),
                ),
                observeStoredGame(firstDetails.gameId),
            )
            assertEquals(1, countRows(GameDetailsPlatformCrossRef.TABLE_NAME))
        }

    @Test
    fun `insertGameDetails should refresh company relations when the same game id is inserted again`() =
        runTest {
            gameDetailsDao.insertGameDetails(
                GameDetailsInsertionBundle(
                    details = firstDetails,
                    platforms = emptyList(),
                    platformCrossRefs = emptyList(),
                    companies = listOf(firstCompany, secondCompany),
                    companyCrossRefs = listOf(
                        GameDetailsCompanyCrossRef(
                            gameId = firstDetails.gameId,
                            companyId = firstCompany.id,
                        ),
                        GameDetailsCompanyCrossRef(
                            gameId = firstDetails.gameId,
                            companyId = secondCompany.id,
                        ),
                    ),
                ),
            )

            gameDetailsDao.insertGameDetails(
                GameDetailsInsertionBundle(
                    details = updatedFirstDetails,
                    platforms = emptyList(),
                    platformCrossRefs = emptyList(),
                    companies = listOf(thirdCompany),
                    companyCrossRefs = listOf(
                        GameDetailsCompanyCrossRef(
                            gameId = updatedFirstDetails.gameId,
                            companyId = thirdCompany.id,
                        ),
                    ),
                ),
            )

            assertEquals(
                GameDetailsWithPlatformsAndCompanies(
                    details = updatedFirstDetails,
                    platforms = emptyList(),
                    companies = listOf(thirdCompany),
                ),
                observeStoredGame(firstDetails.gameId),
            )
            assertEquals(1, countRows(GameDetailsCompanyCrossRef.TABLE_NAME))
        }

    @Test
    fun `insertGameDetails should update an existing company row when the same company id is inserted with changed data`() =
        runTest {
            gameDetailsDao.insertGameDetails(
                GameDetailsInsertionBundle(
                    details = firstDetails,
                    platforms = emptyList(),
                    platformCrossRefs = emptyList(),
                    companies = listOf(firstCompany),
                    companyCrossRefs = listOf(
                        GameDetailsCompanyCrossRef(
                            gameId = firstDetails.gameId,
                            companyId = firstCompany.id,
                        ),
                    ),
                ),
            )

            gameDetailsDao.insertGameDetails(
                GameDetailsInsertionBundle(
                    details = secondDetails,
                    platforms = emptyList(),
                    platformCrossRefs = emptyList(),
                    companies = listOf(updatedFirstCompany),
                    companyCrossRefs = listOf(
                        GameDetailsCompanyCrossRef(
                            gameId = secondDetails.gameId,
                            companyId = updatedFirstCompany.id,
                        ),
                    ),
                ),
            )

            assertEquals(
                GameDetailsWithPlatformsAndCompanies(
                    details = firstDetails,
                    platforms = emptyList(),
                    companies = listOf(updatedFirstCompany),
                ),
                observeStoredGame(firstDetails.gameId),
            )
            assertEquals(
                GameDetailsWithPlatformsAndCompanies(
                    details = secondDetails,
                    platforms = emptyList(),
                    companies = listOf(updatedFirstCompany),
                ),
                observeStoredGame(secondDetails.gameId),
            )
            assertEquals(1, countRows(GameCompanyEntity.TABLE_NAME))
        }

    @Test
    fun `insertGameDetails with an empty relations bundle should persist details and return empty related lists`() =
        runTest {
            gameDetailsDao.insertGameDetails(
                GameDetailsInsertionBundle(
                    details = firstDetails,
                    platforms = emptyList(),
                    platformCrossRefs = emptyList(),
                    companies = emptyList(),
                    companyCrossRefs = emptyList(),
                ),
            )

            assertEquals(
                GameDetailsWithPlatformsAndCompanies(
                    details = firstDetails,
                    platforms = emptyList(),
                    companies = emptyList(),
                ),
                observeStoredGame(firstDetails.gameId),
            )
        }

    private suspend fun observeStoredGame(gameId: Int): GameDetailsWithPlatformsAndCompanies? =
        gameDetailsDao.observeGame(gameId).first()

    private fun readDetailsRow(gameId: Int): GameDetailsEntity? =
        database.query(
            """
            SELECT ${GameDetailsEntity.GAME_ID}, ${GameDetailsEntity.TITLE},
                ${GameDetailsEntity.IMAGE_URL}, ${GameDetailsEntity.RELEASE_DATE},
                ${GameDetailsEntity.WEBSITE}, ${GameDetailsEntity.RATING},
                ${GameDetailsEntity.DESCRIPTION}
            FROM ${GameDetailsEntity.TABLE_NAME}
            WHERE ${GameDetailsEntity.GAME_ID} = ?
            """.trimIndent(),
            arrayOf(gameId),
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                return@use null
            }

            GameDetailsEntity(
                gameId = cursor.getInt(0),
                title = cursor.getString(1),
                imageUrl = cursor.getString(2),
                releaseDate = if (cursor.isNull(3)) {
                    null
                } else {
                    Instant.ofEpochMilli(cursor.getLong(3))
                },
                website = cursor.getString(4),
                rating = cursor.getDouble(5),
                description = cursor.getString(6),
            )
        }

    private fun readStoredReleaseDate(gameId: Int): Long? =
        database.query(
            """
            SELECT ${GameDetailsEntity.RELEASE_DATE}
            FROM ${GameDetailsEntity.TABLE_NAME}
            WHERE ${GameDetailsEntity.GAME_ID} = ?
            """.trimIndent(),
            arrayOf(gameId),
        ).use { cursor ->
            if (!cursor.moveToFirst() || cursor.isNull(0)) {
                return@use null
            }

            cursor.getLong(0)
        }

    private fun countRows(table: String): Int =
        database.query("SELECT COUNT(*) FROM $table", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
}
