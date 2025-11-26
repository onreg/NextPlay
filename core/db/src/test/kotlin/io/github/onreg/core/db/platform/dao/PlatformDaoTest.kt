package io.github.onreg.core.db.platform.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.platform.entity.PlatformEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
internal class PlatformDaoTest {

    private lateinit var database: NextPlayDatabase
    private lateinit var platformDao: PlatformDao

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        platformDao = database.platformDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `should ignore duplicates in case of insertin platforms`() = runTest {
        platformDao.insertPlatforms(listOf(PlatformEntity(1), PlatformEntity(2)))
        platformDao.insertPlatforms(listOf(PlatformEntity(2), PlatformEntity(3)))

        val count = database.query("SELECT COUNT(*) FROM ${PlatformEntity.TABLE_NAME}", null)
            .use { cursor ->
                cursor.moveToFirst()
                cursor.getInt(0)
            }
        assertEquals(3, count)
    }
}
