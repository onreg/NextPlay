package io.github.onreg.feature.game.impl.mapper

import io.github.onreg.feature.game.impl.R
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.data.game.api.model.GamePlatform
import javax.inject.Inject

public interface PlatformUiMapper {
    public fun map(platforms: Set<GamePlatform>): Set<GameCardUI.PlatformUI>
}

public class PlatformUiMapperImpl @Inject constructor(
    private val resourcesProvider: ResourcesProvider
) : PlatformUiMapper {
    override fun map(platforms: Set<GamePlatform>): Set<GameCardUI.PlatformUI> {
        return platforms.mapNotNull(::mapPlatform).toSet()
    }

    private fun mapPlatform(platform: GamePlatform): GameCardUI.PlatformUI? {
        return when (platform) {
            GamePlatform.PC -> GameCardUI.PlatformUI(
                name = resourcesProvider.getString(R.string.platform_pc),
                iconRes = R.drawable.ic_windows_24
            )
            GamePlatform.PLAYSTATION_5,
            GamePlatform.PLAYSTATION_4,
            GamePlatform.PLAYSTATION_3,
            GamePlatform.PLAYSTATION_2,
            GamePlatform.PLAYSTATION,
            GamePlatform.PS_VITA,
            GamePlatform.PSP -> GameCardUI.PlatformUI(
                name = resourcesProvider.getString(R.string.platform_playstation),
                iconRes = R.drawable.ic_playstation_24
            )
            GamePlatform.XBOX_ONE,
            GamePlatform.XBOX_SERIES_S_X,
            GamePlatform.XBOX_360,
            GamePlatform.XBOX -> GameCardUI.PlatformUI(
                name = resourcesProvider.getString(R.string.platform_xbox),
                iconRes = R.drawable.ic_xbox_24
            )
            GamePlatform.IOS -> GameCardUI.PlatformUI(
                name = resourcesProvider.getString(R.string.platform_ios),
                iconRes = R.drawable.ic_apple_24
            )
            GamePlatform.ANDROID -> GameCardUI.PlatformUI(
                name = resourcesProvider.getString(R.string.platform_android),
                iconRes = R.drawable.ic_android_24
            )
            GamePlatform.MACOS,
            GamePlatform.CLASSIC_MACINTOSH,
            GamePlatform.APPLE_II -> GameCardUI.PlatformUI(
                name = resourcesProvider.getString(R.string.platform_apple_macintosh),
                iconRes = R.drawable.ic_apple_24
            )
            GamePlatform.LINUX -> GameCardUI.PlatformUI(
                name = resourcesProvider.getString(R.string.platform_linux),
                iconRes = R.drawable.ic_linux_24
            )
            GamePlatform.NINTENDO_SWITCH,
            GamePlatform.NINTENDO_3DS,
            GamePlatform.NINTENDO_DS,
            GamePlatform.NINTENDO_DSI,
            GamePlatform.WII_U,
            GamePlatform.WII,
            GamePlatform.GAMECUBE,
            GamePlatform.NINTENDO_64,
            GamePlatform.GAME_BOY_ADVANCE,
            GamePlatform.GAME_BOY_COLOR,
            GamePlatform.GAME_BOY,
            GamePlatform.SNES,
            GamePlatform.NES -> GameCardUI.PlatformUI(
                name = resourcesProvider.getString(R.string.platform_nintendo),
                iconRes = R.drawable.ic_nintendo_24
            )
            GamePlatform.COMMODORE_AMIGA,
            GamePlatform.ATARI_7800,
            GamePlatform.ATARI_5200,
            GamePlatform.ATARI_2600,
            GamePlatform.ATARI_FLASHBACK,
            GamePlatform.ATARI_8_BIT,
            GamePlatform.ATARI_ST,
            GamePlatform.ATARI_LYNX,
            GamePlatform.ATARI_XEGS,
            GamePlatform.GENESIS,
            GamePlatform.SEGA_SATURN,
            GamePlatform.SEGA_CD,
            GamePlatform.SEGA_32X,
            GamePlatform.SEGA_MASTER_SYSTEM,
            GamePlatform.DREAMCAST,
            GamePlatform.P_3DO,
            GamePlatform.JAGUAR,
            GamePlatform.GAME_GEAR,
            GamePlatform.NEO_GEO -> null
        }
    }
}
