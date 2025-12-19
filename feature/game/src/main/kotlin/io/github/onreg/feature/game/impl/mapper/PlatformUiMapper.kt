package io.github.onreg.feature.game.impl.mapper

import io.github.onreg.core.ui.components.card.PlatformUI
import io.github.onreg.data.game.api.model.GamePlatform

public interface PlatformUiMapper {
    public fun map(platform: GamePlatform): PlatformUI
    public fun map(platforms: Set<GamePlatform>): Set<PlatformUI>
}

internal class PlatformUiMapperImpl : PlatformUiMapper {
    override fun map(platform: GamePlatform): PlatformUI {
        TODO("Not yet implemented")
    }

    override fun map(platforms: Set<GamePlatform>): Set<PlatformUI> = platforms
        .map(::map)
        .toSet()
}