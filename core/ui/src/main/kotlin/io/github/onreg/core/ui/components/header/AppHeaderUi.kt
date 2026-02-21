package io.github.onreg.core.ui.components.header

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

public data class AppHeaderUi(
    val title: String = "",
    val navigationItem: AppHeaderMenu? = null,
    val menuItems: List<AppHeaderMenu>? = null,
)

public data class AppHeaderMenu(
    @param:DrawableRes
    val iconResId: Int,
    @param:StringRes
    val contentDescriptionResId: Int,
)
