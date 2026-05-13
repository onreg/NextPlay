package io.github.onreg.data.screenshots.impl.mapper

import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.core.network.rawg.dto.ScreenshotDto
import javax.inject.Inject

public interface ScreenshotDtoMapper {
    public fun map(
        dto: ScreenshotDto,
        gameId: Int,
        position: Long,
    ): ScreenshotEntity
}

public class ScreenshotDtoMapperImpl
    @Inject
    constructor() : ScreenshotDtoMapper {
        override fun map(
            dto: ScreenshotDto,
            gameId: Int,
            position: Long,
        ): ScreenshotEntity = ScreenshotEntity(
            id = dto.id,
            gameId = gameId,
            position = position,
            imageUrl = dto.imageUrl.orEmpty(),
            width = dto.width,
            height = dto.height,
        )
    }
