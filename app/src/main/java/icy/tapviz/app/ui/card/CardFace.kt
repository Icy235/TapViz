package icy.tapviz.app.ui.card

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import icy.tapviz.app.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import icy.tapviz.app.data.Channels
import icy.tapviz.app.data.ContactField
import icy.tapviz.app.data.TapVizCard
import icy.tapviz.app.ui.theme.TapVizTheme

private const val TAG = "CardFace"

/**
 * Иконка бренда или сервиса контакта (например Telegram, WhatsApp, GitHub).
 * Подгружает системный drawable-ресурс `ic_brand_<type>` или рисует текстовый символ.
 *
 * @param type Тип соцсети или канала связи.
 * @param size Размер иконки.
 * @param tint Кастомный цвет иконки (по умолчанию берется цвет бренда).
 */
@Composable
fun BrandIcon(
    type: String,
    size: Dp = 40.dp,
    tint: Color? = null,
) {
    val kind = Channels.of(type)
    val context = LocalContext.current
    val res = remember(type) { context.resources.getIdentifier("ic_brand_$type", "drawable", context.packageName) }
    val c = tint ?: Color(kind.color)

    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(c.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        if (res != 0) {
            Icon(painterResource(res), contentDescription = kind.label, tint = c, modifier = Modifier.size(size * 0.52f))
        } else {
            Text(kind.glyph, color = c, fontSize = (size.value * 0.4f).sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Рендеринг лицевой стороны карточки визитки.
 * Отображает стилизованный градиент темы, аватар, имя, должность, компанию,
 * логотип, фоновое изображение и иконки публичных каналов связи.
 *
 * @param card Модель визитки TapVizCard.
 * @param modifier Модификатор макета.
 * @param active Находится ли визитка в фокусе (влияет на тень).
 * @param showStatus Показывать ли статус публикации (Опубликована / Черновик).
 */
@Composable
fun CardFace(
    card: TapVizCard,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    showStatus: Boolean = false,
) {
    val p = CardStyles.palette(card.theme)
    val shape = RoundedCornerShape(28.dp)
    val channels = remember(card.fields) {
        card.fields.filter { it.enabled && it.visibility == "public" && it.value.isNotBlank() }
    }

    val avatar = remember(card.avatarData) {
        card.avatarData?.takeIf { it.isNotBlank() }?.let { data ->
            runCatching {
                val raw = if (data.contains("base64,")) data.substringAfter("base64,") else data
                val bytes = Base64.decode(raw.trim(), Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }.onFailure { ex ->
                Log.w(TAG, "Ошибка декодирования аватара Base64 для визитки ${card.id}", ex)
            }.getOrNull()
        }
    }

    val backgroundImage = remember(card.backgroundData) {
        card.backgroundData?.takeIf { it.isNotBlank() }?.let { data ->
            runCatching {
                val raw = if (data.contains("base64,")) data.substringAfter("base64,") else data
                val bytes = Base64.decode(raw.trim(), Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }.onFailure { ex ->
                Log.w(TAG, "Ошибка декодирования фона Base64 для визитки ${card.id}", ex)
            }.getOrNull()
        }
    }

    val logo = remember(card.logoData) {
        card.logoData?.takeIf { it.isNotBlank() }?.let { data ->
            runCatching {
                val raw = if (data.contains("base64,")) data.substringAfter("base64,") else data
                val bytes = Base64.decode(raw.trim(), Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }.onFailure { ex ->
                Log.w(TAG, "Ошибка декодирования логотипа Base64 для визитки ${card.id}", ex)
            }.getOrNull()
        }
    }

    Box(
        modifier
            .shadow(if (active) 22.dp else 2.dp, shape, ambientColor = p.accent.copy(alpha = .25f), spotColor = p.accent.copy(alpha = .22f))
            .clip(shape)
            .background(Brush.linearGradient(listOf(p.bgTop, p.bgBottom)))
            .drawBehind {
                // Цветная «аура» + тонкая линия акцента сверху
                drawRect(Brush.radialGradient(listOf(p.accent.copy(alpha = .28f), Color.Transparent), center = Offset(size.width, 0f), radius = size.width * .8f))
                drawRect(Brush.radialGradient(listOf(p.accent2.copy(alpha = .16f), Color.Transparent), center = Offset(0f, size.height), radius = size.width * .7f))
                drawRect(Brush.horizontalGradient(listOf(p.accent, p.accent2)), size = Size(size.width, 4.dp.toPx()))
            },
    ) {
        if (backgroundImage != null) {
            Image(backgroundImage, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(.18f), Color.Black.copy(.78f)))))
        }
        if (logo != null) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(17.dp)
                    .size(54.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color.White.copy(alpha = .9f))
                    .padding(9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(logo, contentDescription = "Логотип компании", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showStatus) {
                    val live = card.status == "published"
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (live) Color(0xFF34D399) else p.muted),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(if (live) "Опубликована" else "Черновик", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = p.muted)
                }
                Spacer(Modifier.weight(1f))
                Text(card.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Spacer(Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(p.accent, p.accent2))),
                    contentAlignment = Alignment.Center,
                ) {
                    if (avatar != null) {
                        Image(avatar, contentDescription = "Аватар", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text(card.displayName.trim().firstOrNull()?.uppercase() ?: "T", fontSize = 24.sp, fontWeight = FontWeight.Black, color = p.onAccent)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        card.displayName.ifBlank { "Твоё имя" },
                        fontSize = if (card.displayName.length > 18) 22.sp else 27.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = p.text,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        card.role.ifBlank { "Добавь должность" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (card.role.isBlank()) p.muted.copy(alpha = .72f) else p.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (card.organization.isNotBlank()) {
                        Text(card.organization, fontSize = 12.sp, color = p.muted.copy(alpha = .82f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (channels.isEmpty()) {
                    Text(stringResource(R.string.add_contacts_appear_here), fontSize = 13.sp, color = p.muted)
                } else {
                    channels.take(5).forEach { f ->
                        val c = Color(Channels.of(f.type).color)
                        // На светлой теме бледные бренды читаются лучше с подстройкой tint
                        BrandIcon(f.type, 38.dp, tint = if (p.light && c.luminance() > 0.6f) p.text else null)
                    }
                    if (channels.size > 5) {
                        Text("+${channels.size - 5}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.accent)
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "/${card.slug}",
                    fontSize = 12.sp,
                    color = p.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1.2f, fill = false),
                )
            }
        }
    }
}

// ==================== PREVIEWS (ПРЕВЬЮ КАРТОЧКИ) ====================

private val sampleCardPreview = TapVizCard(
    id = "card-preview-1",
    slug = "alex-design",
    label = "Основная визитка",
    displayName = "Александр Смирнов",
    role = "Lead UI/UX Designer",
    organization = "Pixel Craft",
    bio = "Создаю современный UX для мобильных приложений.",
    theme = "aurora",
    status = "published",
    version = 1,
    fields = listOf(
        ContactField("p-1", "phone", "Телефон", "+7 999 100-20-30", "public"),
        ContactField("p-2", "email", "Email", "alex@example.com", "public"),
        ContactField("p-3", "telegram", "Telegram", "@alex_ui", "public"),
        ContactField("p-4", "github", "GitHub", "alex-ui", "public"),
    ),
)

@Preview(name = "Визитка · Аврора", showBackground = true, widthDp = 360, heightDp = 220)
@Composable
private fun CardFaceDarkPreview() {
    TapVizTheme(darkTheme = true) {
        Surface {
            CardFace(card = sampleCardPreview, showStatus = true, modifier = Modifier.padding(12.dp))
        }
    }
}

@Preview(name = "Визитка · Бумага (светлая)", showBackground = true, widthDp = 360, heightDp = 220)
@Composable
private fun CardFaceLightPreview() {
    TapVizTheme(darkTheme = false) {
        Surface {
            CardFace(card = sampleCardPreview.copy(theme = "paper"), showStatus = true, modifier = Modifier.padding(12.dp))
        }
    }
}

@Preview(name = "Визитка · Минимализм", showBackground = true, widthDp = 360, heightDp = 220)
@Composable
private fun CardFaceMinimalPreview() {
    TapVizTheme(darkTheme = true) {
        Surface {
            CardFace(
                card = sampleCardPreview.copy(theme = "mono", displayName = "Иван Иванов", role = "Разработчик", organization = "", fields = emptyList()),
                showStatus = false,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}
