package io.github.onreg.core.ui.components.content.info

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

public data class ContentInfoUI(
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val titleResId: Int,
    @param:StringRes val descriptionResId: Int
)
