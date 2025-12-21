package io.github.onreg.feature.game.impl.mapper

import io.github.onreg.feature.game.impl.R
import io.github.onreg.core.ui.components.card.GameCardUI
import io.github.onreg.data.game.api.model.GamePlatform
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PlatformUiMapperTest {
    private val resourcesProvider: ResourcesProvider = mock()
    private val mapper = PlatformUiMapperImpl(resourcesProvider)

    @Test
    fun `should map main platforms and group variants`() {
        whenever(resourcesProvider.getString(R.string.platform_pc)).thenReturn("PC")
        whenever(resourcesProvider.getString(R.string.platform_playstation)).thenReturn("PlayStation")
        whenever(resourcesProvider.getString(R.string.platform_xbox)).thenReturn("Xbox")
        whenever(resourcesProvider.getString(R.string.platform_linux)).thenReturn("Linux")

        val result = mapper.map(
            setOf(
                GamePlatform.PC,
                GamePlatform.PLAYSTATION_5,
                GamePlatform.PLAYSTATION_4,
                GamePlatform.XBOX_ONE,
                GamePlatform.LINUX,
                GamePlatform.COMMODORE_AMIGA
            )
        )

        val expected = setOf(
            GameCardUI.PlatformUI("PC", R.drawable.ic_windows_24),
            GameCardUI.PlatformUI("PlayStation", R.drawable.ic_playstation_24),
            GameCardUI.PlatformUI("Xbox", R.drawable.ic_xbox_24),
            GameCardUI.PlatformUI("Linux", R.drawable.ic_linux_24)
        )

        assertEquals(expected, result)
    }
}
