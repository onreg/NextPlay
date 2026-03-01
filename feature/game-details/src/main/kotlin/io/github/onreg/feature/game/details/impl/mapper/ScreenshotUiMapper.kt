package io.github.onreg.feature.game.details.impl.mapper

import io.github.onreg.data.screenshots.api.model.Screenshot
import io.github.onreg.feature.game.details.impl.model.ScreenshotUI
import javax.inject.Inject

internal interface ScreenshotUiMapper {
    fun map(model: Screenshot): ScreenshotUI
}

internal class ScreenshotUiMapperImpl
    @Inject
    constructor() : ScreenshotUiMapper {
        override fun map(model: Screenshot): ScreenshotUI = ScreenshotUI(
            id = model.id,
            imageUrl = model.imageUrl,
        )
    }
