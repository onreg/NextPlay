package io.github.onreg.core.ui.components.header

public data class AppHeaderUI(
    val titleResId: Int,
    val navigationItem: AppHeaderMenu? = null,
    val menuItems: List<AppHeaderMenu>? = null,
)

public data class AppHeaderMenu(
    val iconResId: Int,
    val contentDescriptionResId: Int,
)
