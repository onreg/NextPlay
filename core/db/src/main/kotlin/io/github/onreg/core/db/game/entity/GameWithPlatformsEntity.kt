package io.github.onreg.core.db.game.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.platform.entity.PlatformEntity

public data class GameWithPlatformsEntity(
    @Embedded val game: GameEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = PlatformEntity::class,
        associateBy = Junction(
            value = GamePlatformCrossRef::class,
            parentColumn = "gameId",
            entityColumn = "platformId"
        )
    )
    val platforms: List<PlatformEntity>
)
