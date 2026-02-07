package io.github.onreg.core.db.series.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = SeriesRemoteKeysEntity.TABLE_NAME,
    primaryKeys = [SeriesRemoteKeysEntity.PARENT_GAME_ID, SeriesRemoteKeysEntity.GAME_ID],
    indices = [Index(SeriesRemoteKeysEntity.PARENT_GAME_ID), Index(SeriesRemoteKeysEntity.GAME_ID)],
)
public data class SeriesRemoteKeysEntity(
    @ColumnInfo(name = PARENT_GAME_ID)
    val parentGameId: Int,
    @ColumnInfo(name = GAME_ID)
    val gameId: Int,
    @ColumnInfo(name = PREV_KEY)
    val prevKey: Int?,
    @ColumnInfo(name = NEXT_KEY)
    val nextKey: Int?,
) {
    internal companion object {
        const val TABLE_NAME: String = "game_series_remote_keys"
        const val PARENT_GAME_ID: String = "parentGameId"
        const val GAME_ID: String = "gameId"
        const val PREV_KEY: String = "prevKey"
        const val NEXT_KEY: String = "nextKey"
    }
}
