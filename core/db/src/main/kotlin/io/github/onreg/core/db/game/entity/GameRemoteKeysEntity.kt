package io.github.onreg.core.db.game.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_remote_keys")
public data class GameRemoteKeysEntity(
    @PrimaryKey
    val gameId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)
