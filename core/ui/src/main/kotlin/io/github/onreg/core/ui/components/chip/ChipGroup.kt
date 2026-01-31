package io.github.onreg.core.ui.components.chip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.onreg.core.ui.R
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing

@Composable
public fun ChipGroup(
    modifier: Modifier = Modifier,
    chips: List<ChipUI>,
    onChipClicked: ((Int) -> Unit)? = null,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(-(Spacing.sm)),
    ) {
        chips.forEachIndexed { index, chip ->
            Chip(
                chipUI = chip,
                onClick = if (onChipClicked != null) {
                    { onChipClicked.invoke(index) }
                } else {
                    null
                },
            )
        }
    }
}

@ThemePreview
@Composable
private fun StaticChipsPreview() {
    NextPlayTheme {
        ChipGroup(
            chips = listOf(
                ChipUI("Action", leadingIconRes = R.drawable.ic_controller_24),
                ChipUI("Adventure", isSelected = true, leadingIconRes = R.drawable.ic_share_24),
                ChipUI("RPG", leadingIconRes = R.drawable.ic_controller_off_24),
                ChipUI("Indie"),
                ChipUI("Strategy"),
                ChipUI("Simulation"),
                ChipUI("Sports"),
            ),
        )
    }
}

@ThemePreview
@Composable
private fun SelectableChipsPreview() {
    NextPlayTheme {
        ChipGroup(
            chips = listOf(
                ChipUI("Action", isSelected = false, leadingIconRes = R.drawable.ic_controller_24),
                ChipUI("Adventure", isSelected = true, leadingIconRes = R.drawable.ic_share_24),
                ChipUI("RPG", isSelected = false),
                ChipUI("Indie", isSelected = true),
                ChipUI("Strategy", isSelected = false),
                ChipUI("Simulation", isSelected = false),
                ChipUI("Sports", isSelected = true),
            ),
            onChipClicked = { /* Handle chip click */ },
        )
    }
}
