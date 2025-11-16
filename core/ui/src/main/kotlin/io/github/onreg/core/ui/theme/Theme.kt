package io.github.onreg.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Orange60,
    onPrimary = Gray100,
    secondary = Gray18,
    onSecondary = Gray70,
    background = Gray08,
    onBackground = Gray100,
    surface = Gray12,
    onSurface = Gray100,
    onSurfaceVariant = Gray92,
    surfaceContainerLow = Gray12,
    surfaceContainerHigh = Gray33,

    outlineVariant = Gray70,
    onSecondaryContainer = Gray92,
    secondaryContainer = Orange60
)

private val LightColorScheme = lightColorScheme(
    primary = Orange60,
    onPrimary = Gray100,
    secondary = Gray96,
    onSecondary = Gray20,
    background = Gray92,
    onBackground = Gray13,
    surface = Gray100,
    onSurface = Gray13,
    onSurfaceVariant = Gray20,
    surfaceContainerLow = Gray100,
    surfaceContainerHigh = Gray94,

    outlineVariant = Gray40,
    onSecondaryContainer = Gray20,
    secondaryContainer = Orange60
)

@Composable
public fun NextPlayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // TODO Check dynamic color support
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
