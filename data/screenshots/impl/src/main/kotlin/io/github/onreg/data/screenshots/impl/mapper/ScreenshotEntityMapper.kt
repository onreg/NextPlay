package io.github.onreg.data.screenshots.impl.mapper

import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.data.screenshots.api.model.Screenshot
import javax.inject.Inject

public interface ScreenshotEntityMapper {
    public fun map(entity: ScreenshotEntity): Screenshot
}

public class ScreenshotEntityMapperImpl
    @Inject
    constructor() : ScreenshotEntityMapper {
        override fun map(entity: ScreenshotEntity): Screenshot = Screenshot(
            id = entity.id,
            imageUrl = entity.imageUrl,
            width = entity.width,
            height = entity.height,
        )
    }
