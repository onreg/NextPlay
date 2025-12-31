package io.github.onreg.ui.platform.mapper

import io.github.onreg.ui.platform.model.PlatformUI
import io.github.onreg.core.util.android.resources.ResourcesProvider
import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.ui.platform.R
import javax.inject.Inject

public interface PlatformUiMapper {
    public fun mapName(model: Set<GamePlatform>): Set<String>
    public fun mapPlatform(model: Set<GamePlatform>): Set<PlatformUI>
}

public class PlatformUiMapperImpl @Inject constructor(
    private val resourcesProvider: ResourcesProvider
) : PlatformUiMapper {
    override fun mapName(model: Set<GamePlatform>): Set<String> {
        return model.map(::mapName).toSet()
    }

    override fun mapPlatform(model: Set<GamePlatform>): Set<PlatformUI> {
        return model.mapNotNull(::mapPlatform).toSet()
    }

    private fun mapName(platform: GamePlatform): String {
        return when (platform) {
            GamePlatform.PC -> resourcesProvider.getString(R.string.platform_pc)
            GamePlatform.PLAYSTATION_5 -> resourcesProvider.getString(R.string.platform_playstation_5)
            GamePlatform.PLAYSTATION_4 -> resourcesProvider.getString(R.string.platform_playstation_4)
            GamePlatform.PLAYSTATION_3 -> resourcesProvider.getString(R.string.platform_playstation_3)
            GamePlatform.PLAYSTATION_2 -> resourcesProvider.getString(R.string.platform_playstation_2)
            GamePlatform.PLAYSTATION -> resourcesProvider.getString(R.string.platform_playstation)
            GamePlatform.PS_VITA -> resourcesProvider.getString(R.string.platform_ps_vita)
            GamePlatform.PSP -> resourcesProvider.getString(R.string.platform_psp)
            GamePlatform.XBOX_ONE -> resourcesProvider.getString(R.string.platform_xbox_one)
            GamePlatform.XBOX_SERIES_S_X -> resourcesProvider.getString(R.string.platform_xbox_series_sx)
            GamePlatform.XBOX_360 -> resourcesProvider.getString(R.string.platform_xbox_360)
            GamePlatform.XBOX -> resourcesProvider.getString(R.string.platform_xbox)
            GamePlatform.IOS -> resourcesProvider.getString(R.string.platform_ios)
            GamePlatform.ANDROID -> resourcesProvider.getString(R.string.platform_android)
            GamePlatform.MACOS -> resourcesProvider.getString(R.string.platform_macos)
            GamePlatform.CLASSIC_MACINTOSH ->
                resourcesProvider.getString(R.string.platform_classic_macintosh)
            GamePlatform.APPLE_II -> resourcesProvider.getString(R.string.platform_apple_ii)
            GamePlatform.LINUX -> resourcesProvider.getString(R.string.platform_linux)
            GamePlatform.NINTENDO_SWITCH -> resourcesProvider.getString(R.string.platform_nintendo_switch)
            GamePlatform.NINTENDO_3DS -> resourcesProvider.getString(R.string.platform_nintendo_3ds)
            GamePlatform.NINTENDO_DS -> resourcesProvider.getString(R.string.platform_nintendo_ds)
            GamePlatform.NINTENDO_DSI -> resourcesProvider.getString(R.string.platform_nintendo_dsi)
            GamePlatform.WII_U -> resourcesProvider.getString(R.string.platform_wii_u)
            GamePlatform.WII -> resourcesProvider.getString(R.string.platform_wii)
            GamePlatform.GAMECUBE -> resourcesProvider.getString(R.string.platform_gamecube)
            GamePlatform.NINTENDO_64 -> resourcesProvider.getString(R.string.platform_nintendo_64)
            GamePlatform.GAME_BOY_ADVANCE ->
                resourcesProvider.getString(R.string.platform_game_boy_advance)
            GamePlatform.GAME_BOY_COLOR -> resourcesProvider.getString(R.string.platform_game_boy_color)
            GamePlatform.GAME_BOY -> resourcesProvider.getString(R.string.platform_game_boy)
            GamePlatform.SNES -> resourcesProvider.getString(R.string.platform_snes)
            GamePlatform.NES -> resourcesProvider.getString(R.string.platform_nes)
            GamePlatform.COMMODORE_AMIGA -> resourcesProvider.getString(R.string.platform_commodore_amiga)
            GamePlatform.ATARI_7800 -> resourcesProvider.getString(R.string.platform_atari_7800)
            GamePlatform.ATARI_5200 -> resourcesProvider.getString(R.string.platform_atari_5200)
            GamePlatform.ATARI_2600 -> resourcesProvider.getString(R.string.platform_atari_2600)
            GamePlatform.ATARI_FLASHBACK -> resourcesProvider.getString(R.string.platform_atari_flashback)
            GamePlatform.ATARI_8_BIT -> resourcesProvider.getString(R.string.platform_atari_8_bit)
            GamePlatform.ATARI_ST -> resourcesProvider.getString(R.string.platform_atari_st)
            GamePlatform.ATARI_LYNX -> resourcesProvider.getString(R.string.platform_atari_lynx)
            GamePlatform.ATARI_XEGS -> resourcesProvider.getString(R.string.platform_atari_xegs)
            GamePlatform.GENESIS -> resourcesProvider.getString(R.string.platform_genesis)
            GamePlatform.SEGA_SATURN -> resourcesProvider.getString(R.string.platform_sega_saturn)
            GamePlatform.SEGA_CD -> resourcesProvider.getString(R.string.platform_sega_cd)
            GamePlatform.SEGA_32X -> resourcesProvider.getString(R.string.platform_sega_32x)
            GamePlatform.SEGA_MASTER_SYSTEM ->
                resourcesProvider.getString(R.string.platform_sega_master_system)
            GamePlatform.DREAMCAST -> resourcesProvider.getString(R.string.platform_dreamcast)
            GamePlatform.P_3DO -> resourcesProvider.getString(R.string.platform_3do)
            GamePlatform.JAGUAR -> resourcesProvider.getString(R.string.platform_jaguar)
            GamePlatform.GAME_GEAR -> resourcesProvider.getString(R.string.platform_game_gear)
            GamePlatform.NEO_GEO -> resourcesProvider.getString(R.string.platform_neo_geo)
        }
    }

    private fun mapPlatform(platform: GamePlatform): PlatformUI? {
        return when (platform) {
            GamePlatform.PC -> PlatformUI(
                name = resourcesProvider.getString(R.string.platform_pc),
                iconRes = R.drawable.ic_windows_24
            )
            GamePlatform.PLAYSTATION_5,
            GamePlatform.PLAYSTATION_4,
            GamePlatform.PLAYSTATION_3,
            GamePlatform.PLAYSTATION_2,
            GamePlatform.PLAYSTATION,
            GamePlatform.PS_VITA,
            GamePlatform.PSP -> PlatformUI(
                name = resourcesProvider.getString(R.string.platform_playstation),
                iconRes = R.drawable.ic_playstation_24
            )
            GamePlatform.XBOX_ONE,
            GamePlatform.XBOX_SERIES_S_X,
            GamePlatform.XBOX_360,
            GamePlatform.XBOX -> PlatformUI(
                name = resourcesProvider.getString(R.string.platform_xbox),
                iconRes = R.drawable.ic_xbox_24
            )
            GamePlatform.IOS -> PlatformUI(
                name = resourcesProvider.getString(R.string.platform_ios),
                iconRes = R.drawable.ic_apple_24
            )
            GamePlatform.ANDROID -> PlatformUI(
                name = resourcesProvider.getString(R.string.platform_android),
                iconRes = R.drawable.ic_android_24
            )
            GamePlatform.MACOS,
            GamePlatform.CLASSIC_MACINTOSH,
            GamePlatform.APPLE_II -> PlatformUI(
                name = resourcesProvider.getString(R.string.platform_apple_macintosh),
                iconRes = R.drawable.ic_apple_24
            )
            GamePlatform.LINUX -> PlatformUI(
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
            GamePlatform.NES -> PlatformUI(
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
