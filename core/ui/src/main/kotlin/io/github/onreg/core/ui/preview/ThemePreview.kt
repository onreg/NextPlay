package io.github.onreg.core.ui.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview


@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Preview(
    name = " Dark",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
public annotation class ThemePreview()

@Preview(
    name = "Tablet - Light",
    device = Devices.TABLET,
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Preview(
    name = "Tablet - Dark",
    device = Devices.TABLET,
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
public annotation class TabletThemePreview()
