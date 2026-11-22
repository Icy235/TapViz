package icy.tapviz.app.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Единый компонент системных тонкоконтурных векторных иконок для навигации и кнопок TapViz.
 * Рисует иконку в Canvas в сетке 24x24 без сторонних тяжелых библиотек.
 *
 * @param name Имя иконки (cards, qr, profile, share, edit, add, download, trash, palette, web, back, up, down, close, nfc).
 * @param description Доступное текстовое описание для Accessibility / TalkBack.
 * @param tint Цвет иконки.
 * @param modifier Модификатор Compose.
 * @param size Размер иконки.
 */
@Composable
fun TapVizIcon(
    name: String,
    description: String,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    Canvas(modifier.size(size).semantics { contentDescription = description }) {
        val u = this.size.minDimension / 24f
        val sw = 1.8f * u
        fun line(a: Offset, b: Offset, width: Float = sw) = drawLine(tint, a * u, b * u, width)
        fun box(left: Float, top: Float, width: Float, height: Float, radius: Float = 3f) = drawRoundRect(
            tint, Offset(left * u, top * u), Size(width * u, height * u), CornerRadius(radius * u), style = Stroke(sw),
        )
        when (name) {
            "cards" -> { box(4f, 6f, 14f, 13f); box(7f, 4f, 13f, 13f) }
            "qr" -> {
                box(3f, 3f, 7f, 7f, 1f); box(14f, 3f, 7f, 7f, 1f); box(3f, 14f, 7f, 7f, 1f)
                drawRect(tint, Offset(5f * u, 5f * u), Size(3f * u, 3f * u))
                drawRect(tint, Offset(16f * u, 5f * u), Size(3f * u, 3f * u))
                drawRect(tint, Offset(5f * u, 16f * u), Size(3f * u, 3f * u))
                box(14f, 14f, 3f, 3f, 0f); box(18f, 18f, 3f, 3f, 0f)
                line(Offset(18f, 14f), Offset(21f, 14f)); line(Offset(14f, 18f), Offset(14f, 21f))
            }
            "stats" -> {
                line(Offset(4f, 20f), Offset(4f, 5f)); line(Offset(4f, 20f), Offset(21f, 20f))
                val path = Path().apply {
                    moveTo(7f * u, 16f * u)
                    lineTo(11f * u, 12f * u)
                    lineTo(14f * u, 14f * u)
                    lineTo(20f * u, 7f * u)
                }
                drawPath(path, tint, style = Stroke(sw * 1.25f))
                line(Offset(17f, 7f), Offset(20f, 7f)); line(Offset(20f, 7f), Offset(20f, 10f))
            }
            "profile" -> {
                drawCircle(tint, 3.3f * u, Offset(12f * u, 8f * u), style = Stroke(sw))
                val path = Path().apply {
                    moveTo(4.5f * u, 20f * u)
                    cubicTo(5f * u, 15f * u, 8f * u, 13.5f * u, 12f * u, 13.5f * u)
                    cubicTo(16f * u, 13.5f * u, 19f * u, 15f * u, 19.5f * u, 20f * u)
                }
                drawPath(path, tint, style = Stroke(sw, cap = StrokeCap.Round))
            }
            "share" -> {
                line(Offset(7f, 12f), Offset(19f, 5f)); line(Offset(7f, 12f), Offset(19f, 19f))
                drawCircle(tint, 3f * u, Offset(5f * u, 12f * u))
                drawCircle(tint, 3f * u, Offset(19f * u, 4f * u))
                drawCircle(tint, 3f * u, Offset(19f * u, 20f * u))
            }
            "edit" -> {
                rotate(-42f, Offset(12f * u, 12f * u)) {
                    drawRoundRect(tint, Offset(9f * u, 3f * u), Size(6f * u, 14f * u), CornerRadius(2f * u), style = Stroke(sw))
                    line(Offset(9f, 17f), Offset(15f, 17f)); line(Offset(9f, 20f), Offset(15f, 20f))
                }
            }
            "add" -> { line(Offset(12f, 4f), Offset(12f, 20f)); line(Offset(4f, 12f), Offset(20f, 12f)) }
            "download" -> {
                line(Offset(12f, 3f), Offset(12f, 15f))
                line(Offset(7f, 11f), Offset(12f, 16f))
                line(Offset(17f, 11f), Offset(12f, 16f))
                line(Offset(4f, 20f), Offset(20f, 20f))
            }
            "trash" -> {
                line(Offset(4f, 7f), Offset(20f, 7f))
                line(Offset(9f, 4f), Offset(15f, 4f))
                box(6f, 8f, 12f, 12f, 2f)
                line(Offset(10f, 11f), Offset(10f, 17f))
                line(Offset(14f, 11f), Offset(14f, 17f))
            }
            "palette" -> {
                drawCircle(tint, 9f * u, Offset(12f * u, 12f * u), style = Stroke(sw))
                drawCircle(tint, 1f * u, Offset(8f * u, 9f * u))
                drawCircle(tint, 1f * u, Offset(12f * u, 6f * u))
                drawCircle(tint, 1f * u, Offset(16f * u, 9f * u))
            }
            "web" -> {
                drawCircle(tint, 9f * u, Offset(12f * u, 12f * u), style = Stroke(sw))
                line(Offset(3f, 12f), Offset(21f, 12f))
                line(Offset(12f, 3f), Offset(12f, 21f))
                drawOval(tint, Offset(8f * u, 3f * u), Size(8f * u, 18f * u), style = Stroke(sw))
            }
            "back" -> { line(Offset(15f, 6f), Offset(9f, 12f)); line(Offset(9f, 12f), Offset(15f, 18f)) }
            "up" -> { line(Offset(6f, 15f), Offset(12f, 9f)); line(Offset(12f, 9f), Offset(18f, 15f)) }
            "down" -> { line(Offset(6f, 9f), Offset(12f, 15f)); line(Offset(12f, 15f), Offset(18f, 9f)) }
            "close" -> { line(Offset(6f, 6f), Offset(18f, 18f)); line(Offset(18f, 6f), Offset(6f, 18f)) }
            "nfc" -> {
                drawCircle(tint, 2f * u, Offset(12f * u, 12f * u))
                drawArc(tint, 220f, 100f, useCenter = false, Offset(7f * u, 7f * u), Size(10f * u, 10f * u), style = Stroke(sw))
                drawArc(tint, 220f, 100f, useCenter = false, Offset(4f * u, 4f * u), Size(16f * u, 16f * u), style = Stroke(sw))
                drawArc(tint, 220f, 100f, useCenter = false, Offset(1f * u, 1f * u), Size(22f * u, 22f * u), style = Stroke(sw))
            }
            else -> box(4f, 4f, 16f, 16f)
        }
    }
}
