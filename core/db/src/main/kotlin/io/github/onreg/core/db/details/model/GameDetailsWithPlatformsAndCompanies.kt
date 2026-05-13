package io.github.onreg.core.db.details.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import io.github.onreg.core.db.company.entity.GameCompanyEntity
import io.github.onreg.core.db.details.entity.GameDetailsCompanyCrossRef
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.details.entity.GameDetailsPlatformCrossRef
import io.github.onreg.core.db.platform.entity.PlatformEntity

public data class GameDetailsWithPlatformsAndCompanies(
    @Embedded val details: GameDetailsEntity,
    @Relation(
        parentColumn = GameDetailsEntity.GAME_ID,
        entityColumn = PlatformEntity.ID,
        entity = PlatformEntity::class,
        associateBy = Junction(
            value = GameDetailsPlatformCrossRef::class,
            parentColumn = GameDetailsPlatformCrossRef.GAME_ID,
            entityColumn = GameDetailsPlatformCrossRef.PLATFORM_ID,
        ),
    )
    val platforms: List<PlatformEntity>,
    @Relation(
        parentColumn = GameDetailsEntity.GAME_ID,
        entityColumn = GameCompanyEntity.ID,
        entity = GameCompanyEntity::class,
        associateBy = Junction(
            value = GameDetailsCompanyCrossRef::class,
            parentColumn = GameDetailsCompanyCrossRef.GAME_ID,
            entityColumn = GameDetailsCompanyCrossRef.COMPANY_ID,
        ),
    )
    val companies: List<GameCompanyEntity>,
)
