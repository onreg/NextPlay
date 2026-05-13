package io.github.onreg.core.db.test

import androidx.paging.PagingSource
import androidx.room.RoomDatabase
import kotlin.test.assertIs

internal suspend fun <Value : Any> PagingSource<Int, Value>.loadDaoRefreshPage():
    PagingSource.LoadResult.Page<Int, Value> {
    val result = load(
        PagingSource.LoadParams.Refresh(
            key = null,
            loadSize = 50,
            placeholdersEnabled = false,
        ),
    )

    return assertIs<PagingSource.LoadResult.Page<Int, Value>>(result)
}

internal fun RoomDatabase.tableRowsExist(tableName: String): Boolean =
    countTableRows(tableName) > 0

internal fun RoomDatabase.tableRowsDoNotExist(tableName: String): Boolean =
    !tableRowsExist(tableName)

internal fun RoomDatabase.countTableRows(tableName: String): Int =
    query("SELECT COUNT(*) FROM $tableName", null).use { cursor ->
        cursor.moveToFirst()
        cursor.getInt(0)
    }
