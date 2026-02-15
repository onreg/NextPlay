package io.github.onreg.core.db.details.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.company.entity.GameCompanyEntity
import io.github.onreg.core.db.company.entity.GameCompanyRoleEntity
import io.github.onreg.core.db.details.entity.GameDetailsCompanyCrossRef
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.details.entity.GameDetailsPlatformCrossRef
import io.github.onreg.core.db.details.model.GameDetailsInsertionBundle
import io.github.onreg.core.db.platform.entity.PlatformEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
internal class GameDetailsDaoTest {
    private data class GameDetailsFixture(
        val gameId: Int,
        val title: String,
        val platformIds: List<Int>,
        val companyIds: List<String>,
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
    fun `should persist companies with enum roles`() = runTest {
        val gameId = 101
        gameDetailsDao.insertGameDetails(
            GameDetailsInsertionBundle(
                details = GameDetailsEntity(
                    gameId = gameId,
                    title = "Game",
                    imageUrl = "image",
                    releaseDate = null,
                    website = "https://example.com",
                    rating = 4.2,
                    description = "description",
                ),
                platforms = listOf(PlatformEntity(4)),
                platformCrossRefs = listOf(
                    GameDetailsPlatformCrossRef(
                        gameId = gameId,
                        platformId = 4,
                    ),
                ),
                companies = listOf(
                    GameCompanyEntity(
                        id = "Developer\u001FDev",
                        name = "Dev",
                        logoUrl = "https://dev",
                        role = GameCompanyRoleEntity.Developer,
                    ),
                    GameCompanyEntity(
                        id = "Publisher\u001FPub",
                        name = "Pub",
                        logoUrl = null,
                        role = GameCompanyRoleEntity.Publisher,
                    ),
                ),
                companyCrossRefs = listOf(
                    GameDetailsCompanyCrossRef(gameId = gameId, companyId = "Developer\u001FDev"),
                    GameDetailsCompanyCrossRef(gameId = gameId, companyId = "Publisher\u001FPub"),
                ),
            ),
        )

        val model = gameDetailsDao.observeGame(gameId).first { it != null }

        assertEquals(gameId, model?.details?.gameId)
        assertEquals(setOf(4), model?.platforms?.map { it.id }?.toSet())
        assertEquals(
            setOf(GameCompanyRoleEntity.Developer, GameCompanyRoleEntity.Publisher),
            model?.companies?.map { it.role }?.toSet(),
        )
    }

    @Test
    fun `should cascade delete details platform cross refs when platform is removed`() = runTest {
        val gameId = 102
        val platformId = 5
        gameDetailsDao.insertGameDetails(
            GameDetailsInsertionBundle(
                details = GameDetailsEntity(
                    gameId = gameId,
                    title = "Platform Cascade",
                    imageUrl = "image",
                    releaseDate = null,
                    website = null,
                    rating = 4.0,
                    description = "description",
                ),
                platforms = listOf(PlatformEntity(platformId)),
                platformCrossRefs = listOf(
                    GameDetailsPlatformCrossRef(
                        gameId = gameId,
                        platformId = platformId,
                    ),
                ),
                companies = emptyList(),
                companyCrossRefs = emptyList(),
            ),
        )

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${PlatformEntity.TABLE_NAME} WHERE ${PlatformEntity.ID} = ?",
            arrayOf(platformId),
        )

        assertEquals(1, countRows(GameDetailsEntity.TABLE_NAME))
        assertEquals(0, countRows(GameDetailsPlatformCrossRef.TABLE_NAME))
    }

    @Test
    fun `should cascade delete details company cross refs when company is removed`() = runTest {
        val gameId = 103
        val companyId = "Developer\u001FCascadeDev"
        gameDetailsDao.insertGameDetails(
            GameDetailsInsertionBundle(
                details = GameDetailsEntity(
                    gameId = gameId,
                    title = "Company Cascade",
                    imageUrl = "image",
                    releaseDate = null,
                    website = null,
                    rating = 4.1,
                    description = "description",
                ),
                platforms = emptyList(),
                platformCrossRefs = emptyList(),
                companies = listOf(
                    GameCompanyEntity(
                        id = companyId,
                        name = "Cascade Dev",
                        logoUrl = null,
                        role = GameCompanyRoleEntity.Developer,
                    ),
                ),
                companyCrossRefs = listOf(
                    GameDetailsCompanyCrossRef(
                        gameId = gameId,
                        companyId = companyId,
                    ),
                ),
            ),
        )

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameCompanyEntity.TABLE_NAME} WHERE ${GameCompanyEntity.ID} = ?",
            arrayOf(companyId),
        )

        assertEquals(1, countRows(GameDetailsEntity.TABLE_NAME))
        assertEquals(0, countRows(GameDetailsCompanyCrossRef.TABLE_NAME))
    }

    @Test
    fun `should isolate related platforms and companies per game details`() = runTest {
        val firstFixture = GameDetailsFixture(
            gameId = 201,
            title = "First",
            platformIds = listOf(100, 101),
            companyIds = listOf("Developer\u001FShared", "Publisher\u001FFirstOnly"),
        )
        val secondFixture = GameDetailsFixture(
            gameId = 202,
            title = "Second",
            platformIds = listOf(100, 102),
            companyIds = listOf("Developer\u001FShared", "Publisher\u001FSecondOnly"),
        )

        insertFixture(
            fixture = firstFixture,
            companies = listOf(
                company(id = "Developer\u001FShared", name = "Shared"),
                company(
                    id = "Publisher\u001FFirstOnly",
                    name = "First Only",
                    role = GameCompanyRoleEntity.Publisher,
                ),
            ),
        )
        insertFixture(
            fixture = secondFixture,
            companies = listOf(
                company(
                    id = "Publisher\u001FSecondOnly",
                    name = "Second Only",
                    role = GameCompanyRoleEntity.Publisher,
                ),
            ),
        )

        val firstModel = gameDetailsDao.observeGame(firstFixture.gameId).first { it != null }
            ?: error("Expected first game details")
        val secondModel = gameDetailsDao.observeGame(secondFixture.gameId).first { it != null }
            ?: error("Expected second game details")

        assertEquals(firstFixture.platformIds.toSet(), firstModel.platforms.map { it.id }.toSet())
        assertEquals(secondFixture.platformIds.toSet(), secondModel.platforms.map { it.id }.toSet())
        assertEquals(firstFixture.companyIds.toSet(), firstModel.companies.map { it.id }.toSet())
        assertEquals(secondFixture.companyIds.toSet(), secondModel.companies.map { it.id }.toSet())
    }

    @Test
    fun `should return empty related platforms and companies when cross refs are absent`() =
        runTest {
            val gameId = 203

            gameDetailsDao.insertGameDetails(
                GameDetailsInsertionBundle(
                    details = GameDetailsEntity(
                        gameId = gameId,
                        title = "No Relations",
                        imageUrl = "image",
                        releaseDate = null,
                        website = null,
                        rating = 4.3,
                        description = "description",
                    ),
                    platforms = emptyList(),
                    platformCrossRefs = emptyList(),
                    companies = emptyList(),
                    companyCrossRefs = emptyList(),
                ),
            )

            val model = gameDetailsDao.observeGame(gameId).first { it != null }
                ?: error("Expected game details")

            assertEquals(emptyList(), model.platforms)
            assertEquals(emptyList(), model.companies)
        }

    @Test
    fun `should cascade delete details cross refs when details row is removed`() = runTest {
        val gameId = 204
        val platformId = 200
        val companyId = "Developer\u001FDetailsCascade"

        gameDetailsDao.insertGameDetails(
            GameDetailsInsertionBundle(
                details = GameDetailsEntity(
                    gameId = gameId,
                    title = "Details Cascade",
                    imageUrl = "image",
                    releaseDate = null,
                    website = null,
                    rating = 4.4,
                    description = "description",
                ),
                platforms = listOf(PlatformEntity(platformId)),
                platformCrossRefs = listOf(GameDetailsPlatformCrossRef(gameId, platformId)),
                companies = listOf(
                    GameCompanyEntity(
                        id = companyId,
                        name = "Details Cascade",
                        logoUrl = null,
                        role = GameCompanyRoleEntity.Developer,
                    ),
                ),
                companyCrossRefs = listOf(GameDetailsCompanyCrossRef(gameId, companyId)),
            ),
        )

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameDetailsEntity.TABLE_NAME} WHERE ${GameDetailsEntity.GAME_ID} = ?",
            arrayOf(gameId),
        )

        assertEquals(0, countRows(GameDetailsEntity.TABLE_NAME))
        assertEquals(0, countRows(GameDetailsPlatformCrossRef.TABLE_NAME))
        assertEquals(0, countRows(GameDetailsCompanyCrossRef.TABLE_NAME))
        assertEquals(1, countRows(PlatformEntity.TABLE_NAME))
        assertEquals(1, countRows(GameCompanyEntity.TABLE_NAME))
    }

    private suspend fun insertFixture(
        fixture: GameDetailsFixture,
        companies: List<GameCompanyEntity>,
    ) {
        gameDetailsDao.insertGameDetails(
            GameDetailsInsertionBundle(
                details = GameDetailsEntity(
                    gameId = fixture.gameId,
                    title = fixture.title,
                    imageUrl = "image",
                    releaseDate = null,
                    website = null,
                    rating = 4.0,
                    description = "description",
                ),
                platforms = fixture.platformIds.map(::PlatformEntity),
                platformCrossRefs = fixture.platformIds.map {
                    GameDetailsPlatformCrossRef(fixture.gameId, it)
                },
                companies = companies,
                companyCrossRefs = fixture.companyIds.map {
                    GameDetailsCompanyCrossRef(fixture.gameId, it)
                },
            ),
        )
    }

    private fun company(
        id: String,
        name: String,
        role: GameCompanyRoleEntity = GameCompanyRoleEntity.Developer,
    ): GameCompanyEntity = GameCompanyEntity(
        id = id,
        name = name,
        logoUrl = null,
        role = role,
    )

    private fun countRows(table: String): Int =
        database.query("SELECT COUNT(*) FROM $table", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
}
