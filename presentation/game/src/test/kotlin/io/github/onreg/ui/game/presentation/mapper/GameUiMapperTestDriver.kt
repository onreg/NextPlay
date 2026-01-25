package io.github.onreg.ui.game.presentation.mapper

import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import io.github.onreg.ui.platform.model.PlatformUI
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class GameUiMapperTestDriver private constructor(platformUiMapper: PlatformUiMapper) {
    val mapper = GameUiMapperImpl(platformUiMapper)

    class Builder {
        private val platformUiMapper: PlatformUiMapper = mock()

        fun platformUiMapperMap(
            platforms: Set<GamePlatform>,
            platformUi: Set<PlatformUI>,
        ) = apply {
            whenever(platformUiMapper.mapPlatform(platforms)).thenReturn(platformUi)
        }

        fun build() = GameUiMapperTestDriver(platformUiMapper)
    }
}
