package io.github.onreg.core.ui.components.header

/**
 * Configuration state for the [AppHeader] component.
 *
 * @param titleResId String resource ID for the header title text
 * @param navigationItemResId Optional drawable resource ID for the navigation icon.
 * If null, no navigation icon will be displayed
 * @param menuItemResIds Optional list of drawable resource IDs for action menu items.
 * If null or empty, no action items will be displayed
 */
public data class AppHeaderState(
    val titleResId: Int,
    val navigationItemResId: Int? = null,
    val menuItemResIds: List<Int>? = null,
)