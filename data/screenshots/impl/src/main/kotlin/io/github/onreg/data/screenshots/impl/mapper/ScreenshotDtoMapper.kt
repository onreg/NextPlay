package io.github.onreg.data.screenshots.impl.mapper

import io.github.onreg.core.network.rawg.dto.ScreenshotDto
import io.github.onreg.data.screenshots.api.model.Screenshot
import javax.inject.Inject

public interface ScreenshotDtoMapper {
    public fun map(dto: ScreenshotDto): Screenshot?
}

public class ScreenshotDtoMapperImpl
    @Inject
    constructor() : ScreenshotDtoMapper {
        override fun map(dto: ScreenshotDto): Screenshot? {
            val imageUrl = dto.imageUrl
            return when {
                dto.isDeleted == true -> null

                imageUrl == null -> null

                else -> Screenshot(
                    id = dto.id,
                    imageUrl = imageUrl,
                    width = dto.width,
                    height = dto.height,
                )
            }
        }
    }
