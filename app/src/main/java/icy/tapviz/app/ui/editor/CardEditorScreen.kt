package icy.tapviz.app.ui.editor

import icy.tapviz.app.R

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import icy.tapviz.app.data.Channels
import icy.tapviz.app.data.ContactField
import icy.tapviz.app.data.TapVizCard
import icy.tapviz.app.ui.card.BrandIcon
import icy.tapviz.app.ui.card.CardFace
import icy.tapviz.app.ui.card.CardPalette
import icy.tapviz.app.ui.card.CardStyles
import icy.tapviz.app.ui.card.CardTemplate
import icy.tapviz.app.ui.icons.TapVizIcon
import icy.tapviz.app.ui.theme.LocalTapVizColors
import icy.tapviz.app.ui.theme.TapVizTheme
import java.io.ByteArrayOutputStream

private const val TAG = "CardEditorScreen"

/**
 * Декодирование Base64 строки в ImageBitmap.
 */
private fun decodeBase64Bitmap(data: String?): ImageBitmap? {
    if (data.isNullOrBlank()) return null
    return runCatching {
        val raw = if (data.contains("base64,")) data.substringAfter("base64,") else data
        val bytes = Base64.decode(raw.trim(), Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }.getOrNull()
}

/**
 * Полноэкранный редактор визитки TapViz.
 * Позволяет изменять персональные данные, загружать аватар, логотип и фон,
 * выбирать цветовые палитры и готовые шаблоны, настраивать контакты и управлять публикацией.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CardEditorScreen(
    card: TapVizCard,
    userEmail: String = "",
    saving: Boolean = false,
    error: String? = null,
    onSave: (TapVizCard) -> Unit,
    onPreviewWebsite: (TapVizCard) -> Unit = {},
    onPublish: (TapVizCard, Boolean) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val colors = LocalTapVizColors.current
    val initialCard = remember(card.id, card.version) {
        if (card.fields.isEmpty()) {
            card.copy(fields = listOf(Channels.newField("email", userEmail)))
        } else {
            card
        }
    }
    var draft by remember(card.id, card.version) { mutableStateOf(initialCard) }
    val hasChanges = remember(draft, initialCard) { draft != initialCard }
    var showDiscardDialog by remember { mutableStateOf(false) }

    fun handleBack() {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(enabled = hasChanges) {
        showDiscardDialog = true
    }

    var picker by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf<String?>(null) }
    val nameError = if (draft.displayName.isBlank()) "Имя нужно для публикации визитки." else null
    val fieldsValid = remember(draft.fields) { draft.fields.all { fieldValidationError(it) == null } }
    val canSubmit = (nameError == null && fieldsValid && !saving)

    val avatarBitmap = remember(draft.avatarData) { decodeBase64Bitmap(draft.avatarData) }
    val backgroundBitmap = remember(draft.backgroundData) { decodeBase64Bitmap(draft.backgroundData) }
    val logoBitmap = remember(draft.logoData) { decodeBase64Bitmap(draft.logoData) }

    // Лаунчер выбора фото профиля
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            Log.d(TAG, "Обработка фото профиля: uri=$uri")
            val processed = processImageUri(context, uri, maxDimension = 512)
            if (processed != null) {
                draft = draft.copy(avatarData = processed)
                imageError = null
                Log.i(TAG, "Фото профиля успешно загружено и сжато в Base64")
            } else {
                imageError = "Не удалось открыть фото. Выберите другое изображение."
                Log.w(TAG, "Ошибка обработки фото профиля из $uri")
            }
        }
    }

    // Лаунчер выбора фона визитки
    val backgroundLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            Log.d(TAG, "Обработка фонового изображения: uri=$uri")
            val processed = processImageUri(context, uri, maxDimension = 1024)
            if (processed != null) {
                draft = draft.copy(backgroundData = processed)
                imageError = null
                Log.i(TAG, "Фон успешно загружен и сжат в Base64")
            } else {
                imageError = "Не удалось обработать фон. Выберите JPG, PNG или WebP до 1024 × 1024."
                Log.w(TAG, "Ошибка обработки фона из $uri")
            }
        }
    }

    // Лаунчер выбора логотипа компании
    val logoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            Log.d(TAG, "Обработка логотипа компании: uri=$uri")
            val processed = processImageUri(context, uri, maxDimension = 512)
            if (processed != null) {
                draft = draft.copy(logoData = processed)
                imageError = null
                Log.i(TAG, "Логотип успешно загружен и сжат в Base64")
            } else {
                imageError = "Не удалось обработать логотип. Выберите PNG, JPG или WebP до 1 МБ."
                Log.w(TAG, "Ошибка обработки логотипа из $uri")
            }
        }
    }

    fun setField(i: Int, f: ContactField) {
        draft = draft.copy(fields = draft.fields.toMutableList().also { it[i] = f })
    }

    fun moveField(i: Int, d: Int) {
        val j = i + d
        if (j !in draft.fields.indices) return
        draft = draft.copy(fields = draft.fields.toMutableList().also { val t = it[i]; it[i] = it[j]; it[j] = t })
    }

    fun applyTemplate(t: CardTemplate) {
        Log.i(TAG, "Применение шаблона оформлении: id=${t.id}, theme=${t.theme}")
        val have = draft.fields.map { it.type }.toSet()
        val newFields = if (draft.fields.isEmpty()) {
            t.channels.map { Channels.newField(it, if (it == "email") userEmail else "") }
        } else {
            draft.fields + t.channels.filter { it !in have }.map { Channels.newField(it, if (it == "email") userEmail else "") }
        }
        draft = draft.copy(theme = t.theme, fields = newFields)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        // Шапка редактора
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceRaised)
                    .clickable(onClick = ::handleBack),
                contentAlignment = Alignment.Center,
            ) {
                TapVizIcon("back", "Назад", colors.textPrimary, size = 20.dp)
            }
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.card_editor_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    Log.i(TAG, "Нажата кнопка Сохранить визитку id=${draft.id}")
                    onSave(draft.copy(fields = draft.fields.filter { it.value.isNotBlank() }, status = "published"))
                },
                enabled = canSubmit,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.accentContent),
            ) {
                if (saving) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.accentContent)
                } else {
                    Text(stringResource(R.string.save), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            CardFace(draft, Modifier.fillMaxWidth().height(240.dp), showStatus = true)

            OutlinedButton(
                onClick = {
                    Log.i(TAG, "Предпросмотр веб-страницы сайта для визитки id=${draft.id}")
                    onPreviewWebsite(draft.copy(fields = draft.fields.filter { it.value.isNotBlank() }))
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(stringResource(R.string.preview_page_title), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                TapVizIcon("share", "Предпросмотр", colors.accent, size = 14.dp)
            }
            Text("Изменения сохранятся и опубликуются. Временная ссылка действует 10 минут.", fontSize = 11.sp, color = colors.textMuted)
            if (imageError != null) Text(imageError!!, color = colors.danger, fontSize = 12.sp, fontWeight = FontWeight.Medium)

            if (error != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF321319))
                        .border(1.dp, Color(0xFFFF6687), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Text(error, color = Color(0xFFFF99B0), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            Section("Шаблон", "Выбери — и осталось вписать свои данные") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(CardStyles.templates) { t ->
                        TemplateTile(
                            t = t,
                            selected = (draft.theme == t.theme && t.channels.all { c -> draft.fields.any { it.type == c } }),
                            onClick = { applyTemplate(t) },
                        )
                    }
                }
            }

            Section("Цвет") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(CardStyles.palettes) { p ->
                        PaletteDot(p, draft.theme == p.id) {
                            Log.d(TAG, "Выбрана тема визитки: ${p.id}")
                            draft = draft.copy(theme = p.id)
                        }
                    }
                }
            }

            Section("О тебе", "Фото и личные данные") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceRaised),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap,
                                    contentDescription = "Фото профиля",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                TapVizIcon("profile", "Фото профиля", colors.textMuted, size = 24.dp)
                            }
                        }

                        Column(Modifier.weight(1f)) {
                            Button(
                                onClick = { photoLauncher.launch("image/*") },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accentSoft, contentColor = colors.accent),
                            ) {
                                Text(
                                    if (draft.avatarData != null) "Изменить фото" else "Загрузить фото",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                )
                            }
                            if (draft.avatarData != null) {
                                TextButton(onClick = { draft = draft.copy(avatarData = null) }) {
                                    Text(stringResource(R.string.delete_photo), color = colors.danger, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    EditorField(
                        draft.displayName,
                        { draft = draft.copy(displayName = it) },
                        "Имя и фамилия",
                        maxLen = 60,
                        isError = nameError != null,
                        supporting = nameError,
                    )
                    EditorField(
                        draft.role,
                        { draft = draft.copy(role = it) },
                        "Должность или чем занимаешься",
                        maxLen = 60,
                    )
                    EditorField(
                        draft.organization,
                        { draft = draft.copy(organization = it) },
                        "Компания",
                        maxLen = 60,
                    )
                    EditorField(
                        draft.bio,
                        { draft = draft.copy(bio = it) },
                        "О себе (пара строк)",
                        singleLine = false,
                        maxLen = 240,
                        supporting = "${draft.bio.length}/240",
                    )
                }
            }

            Section("Обложка сайта", "Изображение появится на публичной веб-визитке") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.surface)
                        .border(BorderStroke(1.dp, colors.outline), RoundedCornerShape(18.dp)),
                ) {
                    if (backgroundBitmap != null) {
                        Image(
                            bitmap = backgroundBitmap,
                            contentDescription = "Фон визитки",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            colors.surface.copy(alpha = 0.5f),
                                            colors.surface.copy(alpha = 0.95f),
                                        ),
                                    ),
                                ),
                        )
                    }

                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                if (backgroundBitmap != null) "Миниатюра фона" else "Фон не выбран",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (backgroundBitmap != null) Color.White else colors.textMuted,
                            )
                            if (draft.backgroundData != null) {
                                TextButton(
                                    onClick = { draft = draft.copy(backgroundData = null) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                ) {
                                    Text(stringResource(R.string.remove_bg), color = colors.danger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp),
                        ) {
                            Button(
                                onClick = { backgroundLauncher.launch("image/*") },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (backgroundBitmap != null) colors.surfaceRaised else colors.accentSoft,
                                    contentColor = if (backgroundBitmap != null) colors.textPrimary else colors.accent,
                                ),
                            ) {
                                TapVizIcon("add", "Фон", if (backgroundBitmap != null) colors.textPrimary else colors.accent, size = 16.dp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (draft.backgroundData == null) "Добавить фон" else "Изменить фон",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                )
                            }
                            Text(
                                "до 1024 × 1024",
                                fontSize = 11.sp,
                                color = if (backgroundBitmap != null) Color.White.copy(alpha = 0.8f) else colors.textMuted,
                            )
                        }
                    }
                }
            }

            Section("Логотип компании", "На сайте появится поверх обложки справа") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.surface)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(colors.surfaceRaised),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (logoBitmap != null) {
                            Image(
                                bitmap = logoBitmap,
                                contentDescription = "Логотип компании",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                contentScale = ContentScale.Fit,
                            )
                        } else {
                            Text(stringResource(R.string.logo_label), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(if (draft.logoData == null) stringResource(R.string.logo_placeholder) else stringResource(R.string.logo_added), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                        Text(stringResource(R.string.png_transparent_hint), fontSize = 11.sp, color = colors.textMuted)
                    }
                    Button(
                        onClick = { logoLauncher.launch("image/*") },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentSoft, contentColor = colors.accent),
                    ) {
                        Text(if (draft.logoData == null) stringResource(R.string.upload) else stringResource(R.string.replace), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    if (draft.logoData != null) {
                        TextButton(onClick = { draft = draft.copy(logoData = null) }) {
                            Text(stringResource(R.string.remove), color = colors.danger, fontSize = 12.sp)
                        }
                    }
                }
            }

            Section("Контакты и соцсети", "Изначально в контактах только email, остальное по желанию") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    draft.fields.forEachIndexed { i, f ->
                        AnimatedVisibility(
                            visible = true,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut(),
                        ) {
                            ChannelRow(
                                field = f,
                                canUp = i > 0,
                                canDown = i < draft.fields.lastIndex,
                                onChange = { setField(i, it) },
                                onUp = { moveField(i, -1) },
                                onDown = { moveField(i, 1) },
                                onDelete = {
                                    Log.d(TAG, "Удаление контактного поля index=$i, type=${f.type}")
                                    draft = draft.copy(fields = draft.fields.filterIndexed { idx, _ -> idx != i })
                                },
                            )
                        }
                    }
                    if (draft.fields.size < 12) {
                        val interaction = remember { MutableInteractionSource() }
                        val scale by animateFloatAsState(
                            targetValue = if (interaction.collectIsPressedAsState().value) 0.96f else 1f,
                            animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
                            label = "add_contact_scale",
                        )
                        Button(
                            onClick = { picker = true },
                            interactionSource = interaction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .scale(scale),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accentSoft, contentColor = colors.accent),
                        ) {
                            TapVizIcon("add", stringResource(R.string.add_contact), colors.accent, size = 16.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.add_contact_or_social), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    } else {
                        Text(stringResource(R.string.max_contacts_hint), fontSize = 12.sp, color = colors.textMuted)
                    }
                }
            }

            Section("Публикация", "Проверь публичные поля перед публикацией") {
                val isPublished = (draft.status == "published")
                Button(
                    onClick = {
                        val nextStatus = if (isPublished) "draft" else "published"
                        val shouldPublish = (nextStatus == "published")
                        Log.i(TAG, "Изменение статуса публикации: publish=$shouldPublish, nextStatus=$nextStatus")
                        draft = draft.copy(status = nextStatus)
                        onPublish(draft.copy(fields = draft.fields.filter { it.value.isNotBlank() }, status = nextStatus), shouldPublish)
                    },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPublished) colors.surfaceRaised else colors.accent,
                        contentColor = if (isPublished) colors.textPrimary else colors.accentContent,
                    ),
                    border = if (isPublished) BorderStroke(1.dp, colors.outline) else null,
                ) {
                    Text(
                        if (isPublished) "Снять с публикации" else "Опубликовать визитку",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.unsaved_changes_title)) },
            text = { Text(stringResource(R.string.unsaved_changes_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    },
                ) {
                    Text(stringResource(R.string.exit_without_saving), color = colors.danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.cancel), color = colors.textPrimary)
                }
            },
            containerColor = colors.surface,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
        )
    }

    if (picker) {
        ModalBottomSheet(onDismissRequest = { picker = false }, containerColor = colors.surface) {
            Column(
                Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
                    .navigationBarsPadding(),
            ) {
                Text(stringResource(R.string.what_to_add), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(Modifier.height(14.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Channels.all.forEach { k ->
                        Column(
                            Modifier
                                .width(76.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    val valPrefill = if (k.type == "email") userEmail else ""
                                    Log.d(TAG, "Добавлено новое поле в редакторы: type=${k.type}")
                                    draft = draft.copy(fields = draft.fields + Channels.newField(k.type, valPrefill))
                                    picker = false
                                }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            BrandIcon(k.type, 52.dp)
                            Spacer(Modifier.height(5.dp))
                            Text(k.label, fontSize = 12.sp, color = colors.textSecondary, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Маска для красивого и корректного форматирования телефонного номера при вводе.
 */
class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text
        if (input.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val digits = input.filter { it.isDigit() }
        val clean = if (digits.startsWith("8") || digits.startsWith("7")) digits.drop(1) else digits
        val limited = clean.take(10)

        val formatted = StringBuilder("+7")
        if (limited.isNotEmpty()) {
            formatted.append(" (")
            formatted.append(limited.take(3))
            if (limited.length > 3) {
                formatted.append(") ")
                formatted.append(limited.substring(3, minOf(limited.length, 6)))
                if (limited.length > 6) {
                    formatted.append("-")
                    formatted.append(limited.substring(6, minOf(limited.length, 8)))
                    if (limited.length > 8) {
                        formatted.append("-")
                        formatted.append(limited.substring(8, minOf(limited.length, 10)))
                    }
                }
            }
        }

        val formattedString = formatted.toString()

        val origToTrans = IntArray(input.length + 1)
        val transToOrig = IntArray(formattedString.length + 1)

        var transIdx = 0
        var origIdx = 0
        var foundDigits = 0
        var digitIndex = 0

        val leadingPrefixSkip = if (digits.startsWith("8") || digits.startsWith("7")) 1 else 0

        while (origIdx < input.length) {
            origToTrans[origIdx] = transIdx.coerceAtMost(formattedString.length)
            if (input[origIdx].isDigit()) {
                if (foundDigits >= leadingPrefixSkip && digitIndex < 10) {
                    while (transIdx < formattedString.length && !formattedString[transIdx].isDigit()) {
                        transIdx++
                    }
                    if (transIdx < formattedString.length) {
                        transIdx++
                    }
                    digitIndex++
                }
                foundDigits++
            }
            origIdx++
        }
        origToTrans[input.length] = formattedString.length

        var o = 0
        for (t in 0..formattedString.length) {
            while (o < input.length && origToTrans[o + 1] <= t) {
                o++
            }
            transToOrig[t] = o
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return origToTrans[offset.coerceIn(0, input.length)]
            }

            override fun transformedToOriginal(offset: Int): Int {
                return transToOrig[offset.coerceIn(0, formattedString.length)]
            }
        }

        return TransformedText(AnnotatedString(formattedString), offsetMapping)
    }
}

/**
 * Валидация корректности введенных данных в контактном поле.
 */
private fun fieldValidationError(field: ContactField): String? {
    val value = field.value.trim()
    if (value.isBlank()) return null
    return when (field.type) {
        "email" -> if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(value)) "Проверьте адрес email." else null
        "phone" -> if (!Regex("^\\+?[\\d\\s().-]{6,24}$").matches(value)) "Введите номер телефона, например +7 (931) 299-32-29." else null
        "address" -> if ('<' in value || '>' in value) "Адрес не должен содержать HTML-разметку." else null
        "max", "discord" -> if (!value.startsWith("http://") && !value.startsWith("https://")) "Для этого сервиса нужна полная ссылка с https://." else safeHttpUrl(value)
        "website", "link" -> safeHttpUrl(if (value.contains("://")) value else "https://$value")
        else -> when {
            value.startsWith("javascript:", true) || value.startsWith("data:", true) -> "Эта схема ссылки небезопасна."
            value.startsWith("http://", true) || value.startsWith("https://", true) -> safeHttpUrl(value)
            else -> null
        }
    }
}

private fun safeHttpUrl(value: String): String? {
    val valid = runCatching {
        val uri = value.toUri()
        uri.scheme in listOf("http", "https") && !uri.host.isNullOrBlank() && uri.userInfo == null
    }.getOrDefault(false)
    return if (valid) null else "Введите корректную ссылку на http или https."
}

/**
 * Масштабирование и оптимизация изображения до Base64 JPEG.
 */
private fun processImageUri(context: Context, uri: Uri, maxDimension: Int): String? {
    return runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / sample > maxDimension * 2 || bounds.outHeight / sample > maxDimension * 2) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.RGB_565 }
        val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null
        val scale = minOf(1f, maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt().coerceAtLeast(1), (bitmap.height * scale).toInt().coerceAtLeast(1), true)
        } else {
            bitmap
        }
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 82, outputStream)
        val bytes = outputStream.toByteArray()
        if (bytes.size > 900_000) return null
        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }.getOrNull()
}

@Composable
private fun Section(title: String, subtitle: String? = null, content: @Composable () -> Unit) {
    val colors = LocalTapVizColors.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            subtitle?.let { Text(it, fontSize = 13.sp, color = colors.textSecondary) }
        }
        content()
    }
}

@Composable
private fun TemplateTile(t: CardTemplate, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalTapVizColors.current
    val p = CardStyles.palette(t.theme)
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "template_tile_scale",
    )
    Column(
        Modifier
            .width(132.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .border(BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.accent else colors.outline), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .background(colors.surface)
            .padding(8.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(p.bgTop, p.bgBottom))),
        ) {
            Box(Modifier.fillMaxWidth().height(4.dp).background(Brush.horizontalGradient(listOf(p.accent, p.accent2))))
            Row(Modifier.align(Alignment.BottomStart).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(minOf(4, t.channels.size)) {
                    Box(Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(p.accent.copy(alpha = .8f)))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, maxLines = 1)
        Text(t.tagline, fontSize = 11.sp, color = colors.textSecondary, maxLines = 1)
    }
}

@Composable
private fun PaletteDot(p: CardPalette, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalTapVizColors.current
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "palette_dot_scale",
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(44.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(p.accent, p.accent2)))
                .border(if (selected) 3.dp else 1.dp, if (selected) colors.textPrimary else colors.outline, CircleShape)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        )
        Text(p.name, fontSize = 10.sp, color = if (selected) colors.textPrimary else colors.textMuted, maxLines = 1)
    }
}

@Composable
private fun ChannelRow(
    field: ContactField,
    canUp: Boolean,
    canDown: Boolean,
    onChange: (ContactField) -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalTapVizColors.current
    val kind = Channels.of(field.type)
    val public = field.visibility == "public"
    val fieldError = fieldValidationError(field)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BrandIcon(field.type, 40.dp)
            Text(kind.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary, modifier = Modifier.weight(1f))
            IconButton(onClick = onUp, enabled = canUp, modifier = Modifier.size(32.dp)) {
                TapVizIcon("up", "Выше", if (canUp) colors.textSecondary else colors.outline, size = 16.dp)
            }
            IconButton(onClick = onDown, enabled = canDown, modifier = Modifier.size(32.dp)) {
                TapVizIcon("down", "Ниже", if (canDown) colors.textSecondary else colors.outline, size = 16.dp)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                TapVizIcon("trash", "Удалить", colors.textMuted, size = 16.dp)
            }
        }
        EditorField(
            value = field.value,
            onChange = { onChange(field.copy(value = it)) },
            label = kind.hint,
            isError = fieldError != null,
            supporting = fieldError,
            keyboard = when (field.type) {
                "phone", "whatsapp" -> KeyboardType.Phone
                "email" -> KeyboardType.Email
                "website" -> KeyboardType.Uri
                else -> KeyboardType.Text
            },
            singleLine = field.type != "address",
            visualTransformation = if (field.type == "phone" || field.type == "whatsapp") PhoneVisualTransformation() else VisualTransformation.None,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (public) "Показывать на странице" else "Скрыто — видишь только ты", fontSize = 13.sp, color = colors.textSecondary, modifier = Modifier.weight(1f))
            Switch(
                checked = public,
                onCheckedChange = { onChange(field.copy(visibility = if (it) "public" else "private")) },
                colors = SwitchDefaults.colors(checkedTrackColor = colors.accent, checkedThumbColor = colors.accentContent),
            )
        }
    }
}

@Composable
private fun EditorField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    maxLen: Int = 200,
    keyboard: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    supporting: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = LocalTapVizColors.current
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= maxLen) onChange(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        isError = isError,
        supportingText = { if (supporting != null) Text(supporting) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.outline,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            focusedLabelColor = colors.accent,
            unfocusedLabelColor = colors.textMuted,
            cursorColor = colors.accent,
        ),
    )
}

// ==================== PREVIEWS (ПРЕВЬЮ РЕДАКТОРА) ====================

private val editorSampleCard = TapVizCard(
    id = "editor-preview-id",
    slug = "anna-petrova",
    label = "Основная визитка",
    displayName = "Анна Петрова",
    role = "Продуктовый дизайнер",
    organization = "TapViz Studio",
    bio = "Дизайн интерфейсов и цифровых продуктов.",
    theme = "aurora",
    status = "published",
    version = 1,
    fields = listOf(
        ContactField("f1", "email", "Email", "anna@example.com", "public"),
        ContactField("f2", "phone", "Телефон", "+7 900 123-45-67", "public"),
    ),
)

@Preview(name = "Редактор визитки", showBackground = true, backgroundColor = 0xFF080B10, widthDp = 393, heightDp = 3000)
@Composable
private fun CardEditorScreenPreview() {
    TapVizTheme(darkTheme = false) {
        Surface {
            CardEditorScreen(
                card = editorSampleCard,
                userEmail = "anna@example.com",
                onSave = {},
                onBack = {},
            )
        }
    }
}
