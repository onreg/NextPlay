package io.github.onreg.ui.platform.mapper

import io.github.onreg.core.util.android.resources.ResourcesProvider
import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.ui.platform.R
import io.github.onreg.ui.platform.model.PlatformUI
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class PlatformUiMapperTest {
    private val resourcesProvider: ResourcesProvider = mock {
        on { getString(R.string.platform_pc) } doReturn "PC"
        on { getString(R.string.platform_playstation) } doReturn "PlayStation"
        on { getString(R.string.platform_xbox) } doReturn "Xbox"
        on { getString(R.string.platform_linux) } doReturn "Linux"
        on { getString(R.string.platform_playstation_5) } doReturn "PlayStation 5"
        on { getString(R.string.platform_xbox_one) } doReturn "Xbox One"
    }
    private val mapper = PlatformUiMapperImpl(resourcesProvider)

    @Test
    fun `should map main platforms and group variants`() {
        val result = mapper.mapPlatform(
            setOf(
                GamePlatform.PC,
                GamePlatform.PLAYSTATION_5,
                GamePlatform.PLAYSTATION_4,
                GamePlatform.XBOX_ONE,
                GamePlatform.LINUX,
                GamePlatform.COMMODORE_AMIGA,
            ),
        )

        val expected = setOf(
            PlatformUI("PC", R.drawable.ic_windows_24),
            PlatformUI("PlayStation", R.drawable.ic_playstation_24),
            PlatformUI("Xbox", R.drawable.ic_xbox_24),
            PlatformUI("Linux", R.drawable.ic_linux_24),
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should map platforms to names`() {
        val result = mapper.mapName(
            setOf(
                GamePlatform.PC,
                GamePlatform.PLAYSTATION_5,
                GamePlatform.XBOX_ONE,
                GamePlatform.LINUX,
            ),
        )

        val expected = setOf(
            "PC",
            "PlayStation 5",
            "Xbox One",
            "Linux",
        )

        assertEquals(expected, result)
    }
}
