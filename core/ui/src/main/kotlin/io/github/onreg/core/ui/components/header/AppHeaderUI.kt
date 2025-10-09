package io.github.onreg.core.ui.components.header

public data class AppHeaderUI(
    val titleResId: Int,
    val navigationItemResId: Int? = null,
    val menuItemResIds: List<Int>? = null,
)