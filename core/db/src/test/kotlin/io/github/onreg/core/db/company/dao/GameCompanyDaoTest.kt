package io.github.onreg.core.db.company.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.company.entity.GameCompanyEntity
import io.github.onreg.core.db.company.entity.GameCompanyRoleEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
internal class GameCompanyDaoTest {
    private val firstCompany = GameCompanyEntity(
        id = "Developer\u001FFirst Studio",
        name = "First Studio",
        logoUrl = "https://example.com/first.png",
        role = GameCompanyRoleEntity.Developer,
    )
    private val secondCompany = GameCompanyEntity(
        id = "Publisher\u001FSecond Publishing",
        name = "Second Publishing",
        logoUrl = "https://example.com/second.png",
        role = GameCompanyRoleEntity.Publisher,
    )
    private val companyWithoutLogo = GameCompanyEntity(
        id = "Developer\u001FNo Logo",
        name = "No Logo",
        logoUrl = null,
        role = GameCompanyRoleEntity.Developer,
    )

    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameCompanyDao = database.gameCompanyDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertCompanies should persist multiple company rows`() = runTest {
        gameCompanyDao.insertCompanies(listOf(firstCompany, secondCompany))

        assertEquals(listOf(firstCompany, secondCompany), readCompanies())
    }

    @Test
    fun `insertCompanies should replace an existing company with the same id`() = runTest {
        val updatedFirstCompany = firstCompany.copy(
            name = "Updated First Studio",
            logoUrl = "https://example.com/updated-first.png",
            role = GameCompanyRoleEntity.Publisher,
        )

        gameCompanyDao.insertCompanies(listOf(firstCompany, secondCompany))
        gameCompanyDao.insertCompanies(listOf(updatedFirstCompany))

        assertEquals(listOf(updatedFirstCompany, secondCompany), readCompanies())
    }

    @Test
    fun `insertCompanies should persist nullable logoUrl values`() = runTest {
        gameCompanyDao.insertCompanies(listOf(companyWithoutLogo))

        assertEquals(listOf(companyWithoutLogo), readCompanies())
    }

    @Test
    fun `insertCompanies should persist both Developer and Publisher roles`() = runTest {
        gameCompanyDao.insertCompanies(listOf(firstCompany, secondCompany))

        assertEquals(
            listOf(GameCompanyRoleEntity.Developer, GameCompanyRoleEntity.Publisher),
            readCompanies().map(GameCompanyEntity::role),
        )
    }

    @Test
    fun `insertCompanies with an empty list should be a no-op`() = runTest {
        gameCompanyDao.insertCompanies(emptyList())

        assertEquals(emptyList(), readCompanies())
    }

    private fun readCompanies(): List<GameCompanyEntity> =
        database.query(
            """
            SELECT ${GameCompanyEntity.ID}, ${GameCompanyEntity.NAME}, ${GameCompanyEntity.LOGO_URL},
                ${GameCompanyEntity.ROLE}
            FROM ${GameCompanyEntity.TABLE_NAME}
            ORDER BY ${GameCompanyEntity.ID}
            """.trimIndent(),
            null,
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        GameCompanyEntity(
                            id = cursor.getString(0),
                            name = cursor.getString(1),
                            logoUrl = cursor.getString(2),
                            role = GameCompanyRoleEntity.valueOf(cursor.getString(3)),
                        ),
                    )
                }
            }
        }
}
