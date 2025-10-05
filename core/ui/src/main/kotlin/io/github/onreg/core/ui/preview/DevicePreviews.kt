package io.github.onreg.core.ui.preview

import androidx.compose.ui.tooling.preview.Preview

/**
 * Multi-preview annotation that generates previews for Phone and Tablet
 * screen sizes with both themes
 */
@Preview(
    name = "Phone - Light",
    device = "spec:width=360dp,height=640dp,dpi=480",
    showBackground = true,
    backgroundColor = 0xFFE9ECEF
)
@Preview(
    name = "Phone - Dark",
    device = "spec:width=360dp,height=640dp,dpi=480",
    showBackground = true,
    backgroundColor = 0xFF151515,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Tablet - Light",
    device = "spec:width=1280dp,height=800dp,dpi=240",
    showBackground = true,
    backgroundColor = 0xFFE9ECEF
)
@Preview(
    name = "Tablet - Dark",
    device = "spec:width=1280dp,height=800dp,dpi=240",
    showBackground = true,
    backgroundColor = 0xFF151515,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
public annotation class DevicePreviews
