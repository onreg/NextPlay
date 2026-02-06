package io.github.onreg.core.db.details.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import io.github.onreg.core.db.details.entity.DeveloperEntity
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.details.entity.GameDeveloperCrossRef

public data class GameDetailsWithDevelopers(
    @Embedded val details: GameDetailsEntity,
    @Relation(
        parentColumn = GameDetailsEntity.GAME_ID,
        entityColumn = DeveloperEntity.ID,
        entity = DeveloperEntity::class,
        associateBy = Junction(
            value = GameDeveloperCrossRef::class,
            parentColumn = GameDeveloperCrossRef.GAME_ID,
            entityColumn = GameDeveloperCrossRef.DEVELOPER_ID,
        ),
    )
    val developers: List<DeveloperEntity>,
)
