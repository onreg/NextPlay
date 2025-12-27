package io.github.onreg.core.ui.components.content.error

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

public data class ContentErrorUI(
    @DrawableRes val iconRes: Int,
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int,
    @StringRes val actionLabelResId: Int,
)
