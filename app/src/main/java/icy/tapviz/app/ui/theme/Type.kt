@file:OptIn(ExperimentalTextApi::class)

package icy.tapviz.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font as LocalFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import icy.tapviz.app.R

/**
 * Провайдер онлайн-шрифтов Google Fonts для Montserrat.
 */
val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

val googleFontMontserrat = GoogleFont("Montserrat")

/**
 * Семейство шрифтов Montserrat с онлайн- и локальным вариативным фолбэком.
 */
@OptIn(ExperimentalTextApi::class)
val Montserrat = FontFamily(
    Font(googleFont = googleFontMontserrat, fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = googleFontMontserrat, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = googleFontMontserrat, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = googleFontMontserrat, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = googleFontMontserrat, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = googleFontMontserrat, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
    Font(googleFont = googleFontMontserrat, fontProvider = fontProvider, weight = FontWeight.Black),
    LocalFont(R.font.montserrat, FontWeight.Light, variationSettings = FontVariation.Settings(FontVariation.weight(300))),
    LocalFont(R.font.montserrat, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    LocalFont(R.font.montserrat, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    LocalFont(R.font.montserrat, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    LocalFont(R.font.montserrat, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    LocalFont(R.font.montserrat, FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800))),
    LocalFont(R.font.montserrat, FontWeight.Black, variationSettings = FontVariation.Settings(FontVariation.weight(900))),
)

/**
 * Стили типографики приложений TapViz.
 */
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Black,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
