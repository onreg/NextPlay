package io.github.onreg.core.ui.components.chip

public data class ChipUI(
    public val text: String,
    public val isSelected: Boolean = false,
    public val leadingIconRes: Int? = null,
)
