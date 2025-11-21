package io.github.onreg.core.db.game.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import io.github.onreg.core.db.platform.entity.PlatformEntity

public data class GameWithPlatformsEntity(
    @Embedded val game: GameEntity,
    @Relation(
        parentColumn = GameEntity.ID,
        entityColumn = PlatformEntity.ID,
        entity = PlatformEntity::class,
        associateBy = Junction(
            value = GamePlatformCrossRef::class,
            parentColumn = GamePlatformCrossRef.GAME_ID,
            entityColumn = GamePlatformCrossRef.PLATFORM_ID
        )
    )
    val platforms: List<PlatformEntity>
)
