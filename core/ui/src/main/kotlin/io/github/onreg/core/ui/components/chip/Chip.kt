package io.github.onreg.core.ui.components.chip

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilterChipDefaults.filterChipBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import io.github.onreg.core.ui.R
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.Gray92
import io.github.onreg.core.ui.theme.IconsSize
import io.github.onreg.core.ui.theme.NextPlayTheme

@Composable
public fun Chip(
    modifier: Modifier = Modifier,
    chipUI: ChipUI,
    onClick: (() -> Unit)? = null,
) {
    FilterChip(
        modifier = modifier,
        selected = chipUI.isSelected,
        onClick = {
            onClick?.invoke()
        },
        label = { Text(text = chipUI.text) },
        leadingIcon = chipUI.leadingIconRes?.let { iconRes ->
            {
                Icon(
                    modifier = Modifier.size(IconsSize.sm),
                    painter = painterResource(iconRes),
                    contentDescription = null
                )
            }
        },
        shape = MaterialTheme.shapes.large,
        colors = FilterChipDefaults.filterChipColors(
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLeadingIconColor = getColor(chipUI.isSelected),
            selectedLabelColor = getColor(chipUI.isSelected),
            disabledContainerColor = Color.Transparent,
            disabledLabelColor = getColor(chipUI.isSelected),
            disabledLeadingIconColor = getColor(chipUI.isSelected),
            disabledSelectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        border = filterChipBorder(
            enabled = onClick != null,
            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
            selected = chipUI.isSelected
        ),
        enabled = onClick != null
    )
}

@Composable
private fun getColor(isSelected: Boolean) = when {
    isSelected -> Gray92
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@ThemePreview
@Composable
public fun StaticSelectedPreview() {
    NextPlayTheme {
        Chip(
            chipUI = ChipUI(
                text = "Selected",
                isSelected = true,
                leadingIconRes = R.drawable.ic_apple_24
            ),
            onClick = null
        )
    }
}

@ThemePreview
@Composable
public fun StaticNotSelectedPreview() {
    NextPlayTheme {
        Chip(
            chipUI = ChipUI(
                text = "Not selected",
                isSelected = false,
                leadingIconRes = R.drawable.ic_apple_24
            ),
            onClick = null
        )
    }
}

@ThemePreview
@Composable
public fun ClickableSelectedPreview() {
    NextPlayTheme {
        Chip(
            chipUI = ChipUI(
                text = "Selected",
                isSelected = true,
                leadingIconRes = R.drawable.ic_apple_24
            ),
            onClick = {}
        )
    }
}

@ThemePreview
@Composable
public fun ClickableNotSelectedPreview() {
    NextPlayTheme {
        Chip(
            chipUI = ChipUI(
                text = "Not selected",
                isSelected = false,
                leadingIconRes = R.drawable.ic_xbox_24
            ),
            onClick = {}
        )
    }
}