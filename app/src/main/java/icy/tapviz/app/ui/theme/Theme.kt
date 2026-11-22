package icy.tapviz.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Главная тема приложения TapViz для Material 3.
 * Настраивает цветовую схему, шрифтовую гарнитуру Montserrat и скругления компонентам.
 *
 * @param darkTheme Использовать ли тёмную тему оформления.
 * @param dynamicColor Использовать ли динамические цвета (Material You).
 * @param palette Цветовая палитра токенов приложения (по умолчанию ElectricCyan).
 * @param content Содержимое Compose.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun TapVizTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    palette: TapVizPalette = TapVizPalettes.ElectricCyan,
    content: @Composable () -> Unit,
) {
    val tokens = if (darkTheme) palette.dark else palette.light
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = tokens.accent,
            onPrimary = tokens.accentContent,
            primaryContainer = tokens.accentSoft,
            onPrimaryContainer = tokens.textPrimary,
            secondary = tokens.success,
            background = tokens.background,
            onBackground = tokens.textPrimary,
            surface = tokens.surface,
            onSurface = tokens.textPrimary,
            surfaceVariant = tokens.surfaceRaised,
            onSurfaceVariant = tokens.textSecondary,
            outline = tokens.outline,
            error = tokens.danger,
        )
    } else {
        lightColorScheme(
            primary = tokens.accent,
            onPrimary = tokens.accentContent,
            primaryContainer = tokens.accentSoft,
            onPrimaryContainer = tokens.textPrimary,
            secondary = tokens.success,
            background = tokens.background,
            onBackground = tokens.textPrimary,
            surface = tokens.surface,
            onSurface = tokens.textPrimary,
            surfaceVariant = tokens.surfaceRaised,
            onSurfaceVariant = tokens.textSecondary,
            outline = tokens.outline,
            error = tokens.danger,
        )
    }

    CompositionLocalProvider(
        LocalTapVizPalette provides palette,
        LocalTapVizColors provides tokens,
        LocalTextStyle provides TextStyle(fontFamily = Montserrat),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = Shapes(
                extraSmall = RoundedCornerShape(10.dp),
                small = RoundedCornerShape(16.dp),
                medium = RoundedCornerShape(22.dp),
                large = RoundedCornerShape(28.dp),
                extraLarge = RoundedCornerShape(34.dp),
            ),
            content = content,
        )
    }
}
