package icy.tapviz.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Набор цветовых токенов для конкретной темы поверхности приложения.
 */
data class TapVizColorSet(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceHighest: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentContent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val outline: Color,
    val success: Color,
    val danger: Color,
)

/**
 * Именованная палитра темы с раздельной конфигурацией светлого и тёмного режима.
 */
data class TapVizPalette(
    val name: String,
    val dark: TapVizColorSet,
    val light: TapVizColorSet,
)

/**
 * Все темы приложения TapViz.
 */
object TapVizPalettes {
    val ElectricCyan = TapVizPalette(
        name = "Electric cyan",
        dark = TapVizColorSet(
            background = Color(0xFF080B10),
            surface = Color(0xFF111720),
            surfaceRaised = Color(0xFF17212C),
            surfaceHighest = Color(0xFF202B37),
            accent = Color(0xFF35D9FF),
            accentSoft = Color(0xFF123642),
            accentContent = Color(0xFF061219),
            textPrimary = Color(0xFFF4F7FB),
            textSecondary = Color(0xFFA5B2C0),
            textMuted = Color(0xFF748293),
            outline = Color(0xFF2B3845),
            success = Color(0xFF62E6B4),
            danger = Color(0xFFFF6687),
        ),
        light = TapVizColorSet(
            background = Color(0xFFF1F5F8),
            surface = Color(0xFFFFFFFF),
            surfaceRaised = Color(0xFFE7EDF2),
            surfaceHighest = Color(0xFFDDE6ED),
            accent = Color(0xFF007F9E),
            accentSoft = Color(0xFFD6F5FC),
            accentContent = Color(0xFFFFFFFF),
            textPrimary = Color(0xFF111923),
            textSecondary = Color(0xFF465461),
            textMuted = Color(0xFF657584),
            outline = Color(0xFFCBD5DE),
            success = Color(0xFF167A56),
            danger = Color(0xFFB93453),
        ),
    )

    val IonViolet = TapVizPalette(
        name = "Ion violet",
        dark = ElectricCyan.dark.copy(
            accent = Color(0xFFB88AFF),
            accentSoft = Color(0xFF302044),
            accentContent = Color(0xFF130B1D),
        ),
        light = ElectricCyan.light.copy(
            accent = Color(0xFF7446B5),
            accentSoft = Color(0xFFEFE4FF),
            accentContent = Color(0xFFFFFFFF),
        ),
    )

    val all: List<TapVizPalette> = listOf(ElectricCyan, IonViolet)
}

/**
 * Цветовые акценты для публичных визиток.
 */
data class TapVizCardThemeColors(val key: String, val accent: Color)

object TapVizCardThemes {
    val Ink = TapVizCardThemeColors("ink", Color(0xFF35D9FF))
    val Moss = TapVizCardThemeColors("moss", Color(0xFF9BC69A))
    val Plum = TapVizCardThemeColors("plum", Color(0xFFB998D9))

    val all: List<TapVizCardThemeColors> = listOf(Ink, Moss, Plum)
    fun forKey(key: String): TapVizCardThemeColors = all.firstOrNull { it.key == key } ?: Ink
}

val LocalTapVizPalette = staticCompositionLocalOf { TapVizPalettes.ElectricCyan }
val LocalTapVizColors = staticCompositionLocalOf { TapVizPalettes.ElectricCyan.dark }
