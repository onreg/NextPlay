package io.github.onreg.core.db.game.model

import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GameListEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.platform.entity.PlatformEntity

public data class GameInsertionBundle(
    val games: List<GameEntity>,
    val listEntities: List<GameListEntity>,
    val platforms: List<PlatformEntity>,
    val crossRefs: List<GamePlatformCrossRef>,
)
