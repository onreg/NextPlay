package io.github.onreg.core.db.platform.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.platform.entity.PlatformEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
internal class PlatformDaoTest {
    private val firstPlatform = PlatformEntity(id = 201)
    private val secondPlatform = PlatformEntity(id = 202)

    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val platformDao = database.platformDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertPlatforms should persist new platform rows`() = runTest {
        platformDao.insertPlatforms(listOf(firstPlatform, secondPlatform))

        assertEquals(listOf(firstPlatform, secondPlatform), readPlatforms())
    }

    @Test
    fun `insertPlatforms should ignore already existing platform ids`() = runTest {
        platformDao.insertPlatforms(listOf(firstPlatform))

        platformDao.insertPlatforms(listOf(firstPlatform, secondPlatform))

        assertEquals(listOf(firstPlatform, secondPlatform), readPlatforms())
    }

    @Test
    fun `insertPlatforms should ignore duplicate ids in the same batch`() = runTest {
        platformDao.insertPlatforms(listOf(firstPlatform, firstPlatform, secondPlatform))

        assertEquals(listOf(firstPlatform, secondPlatform), readPlatforms())
    }

    @Test
    fun `insertPlatforms with an empty list should be a no-op`() = runTest {
        platformDao.insertPlatforms(emptyList())

        assertEquals(emptyList(), readPlatforms())
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
}
