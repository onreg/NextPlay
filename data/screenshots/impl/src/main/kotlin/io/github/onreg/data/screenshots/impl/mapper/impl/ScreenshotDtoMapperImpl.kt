package io.github.onreg.data.screenshots.impl.mapper.impl

import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.core.network.rawg.dto.ScreenshotDto
import io.github.onreg.data.screenshots.impl.mapper.ScreenshotDtoMapper
import javax.inject.Inject

public class ScreenshotDtoMapperImpl
    @Inject
    constructor() : ScreenshotDtoMapper {
        override fun map(dto: ScreenshotDto): ScreenshotEntity = ScreenshotEntity(
            id = dto.id,
            imageUrl = dto.imageUrl.orEmpty(),
            width = dto.width,
            height = dto.height,
        )
    }
