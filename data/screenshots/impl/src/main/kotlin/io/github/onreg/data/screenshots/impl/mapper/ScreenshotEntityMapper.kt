package io.github.onreg.data.screenshots.impl.mapper

import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.data.screenshots.api.model.Screenshot

public interface ScreenshotEntityMapper {
    public fun map(entity: ScreenshotEntity): Screenshot
}
