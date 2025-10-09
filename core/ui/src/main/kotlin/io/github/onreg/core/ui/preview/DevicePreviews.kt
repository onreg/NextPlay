package io.github.onreg.core.ui.preview

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multi-preview annotation that generates previews for Phone and Tablet
 * screen sizes with both themes
 */
@Preview(
    name = "Pixel 9 - Light",
    device = Devices.PIXEL_9,
    showBackground = true,
    backgroundColor = 0xFFE9ECEF
)
@Preview(
    name = "Pixel 9 - Dark",
    device = Devices.PIXEL_9,
    showBackground = true,
    backgroundColor = 0xFF151515,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Tablet - Light",
    device = Devices.TABLET,
    showBackground = true,
    backgroundColor = 0xFFE9ECEF
)
@Preview(
    name = "Tablet - Dark",
    device = Devices.TABLET,
    showBackground = true,
    backgroundColor = 0xFF151515,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
public annotation class DevicePreviews
