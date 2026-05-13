package io.github.onreg.core.db.details.model

import io.github.onreg.core.db.company.entity.GameCompanyEntity
import io.github.onreg.core.db.details.entity.GameDetailsCompanyCrossRef
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.details.entity.GameDetailsPlatformCrossRef
import io.github.onreg.core.db.platform.entity.PlatformEntity

public data class GameDetailsInsertionBundle(
    val details: GameDetailsEntity,
    val platforms: List<PlatformEntity>,
    val platformCrossRefs: List<GameDetailsPlatformCrossRef>,
    val companies: List<GameCompanyEntity>,
    val companyCrossRefs: List<GameDetailsCompanyCrossRef>,
)
