package io.github.onreg.data.screenshots.impl.mapper

import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.core.network.rawg.dto.ScreenshotDto

public interface ScreenshotDtoMapper {
    public fun map(dto: ScreenshotDto): ScreenshotEntity
}
