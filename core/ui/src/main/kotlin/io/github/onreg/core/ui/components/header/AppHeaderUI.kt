package io.github.onreg.core.ui.components.header

public data class AppHeaderUI(
    val title: AppHeaderTitle,
    val navigationItem: AppHeaderMenu? = null,
    val menuItems: List<AppHeaderMenu>? = null,
)

public sealed interface AppHeaderTitle {
    public data class Res(val titleResId: Int) : AppHeaderTitle

    public data class Text(val value: String) : AppHeaderTitle
}

public data class AppHeaderMenu(
    val iconResId: Int,
    val contentDescriptionResId: Int,
)
