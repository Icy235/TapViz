package icy.tapviz.app

import androidx.compose.ui.res.stringResource

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.firebase.FirebaseApp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import icy.tapviz.app.data.ApiClient
import icy.tapviz.app.data.ApiException
import icy.tapviz.app.data.ContactField
import icy.tapviz.app.data.TapVizCard
import icy.tapviz.app.ui.editor.CardEditorScreen
import icy.tapviz.app.ui.home.TapVizHomeScreen
import icy.tapviz.app.ui.icons.TapVizIcon
import icy.tapviz.app.ui.theme.LocalTapVizColors
import icy.tapviz.app.ui.theme.TapVizTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "MainActivity"

/**
 * Главная Активити приложения TapViz.
 * Отвечает за инициализацию базовых сервисов (Firebase, AppMetrica),
 * навигацию и отображение корневого UI на основе Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate: запуск MainActivity TapViz")
        enableEdgeToEdge()

        // Инициализация внешних SDK при запуске
        initExternalSdks()

        setContent {
            val settings = remember { getSharedPreferences("tapviz_settings", MODE_PRIVATE) }
            var darkMode by remember { mutableStateOf(settings.getBoolean("dark_mode", false)) }

            TapVizTheme(darkTheme = darkMode, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    TapVizRoot(
                        darkMode = darkMode,
                        onDarkModeChange = { value ->
                            Log.d(TAG, "Смена темы приложения: darkMode=$value")
                            darkMode = value
                            settings.edit { putBoolean("dark_mode", value) }
                        },
                    )
                }
            }
        }
    }

    /**
     * Инициализация Firebase и Yandex AppMetrica через рефлексию.
     */
    private fun initExternalSdks() {
        try {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase успешно инициализирован")

            val metricaClass = Class.forName("com.yandex.metrica.YandexMetrica")
            val configClass = Class.forName("com.yandex.metrica.YandexMetricaConfig")
            val builderMethod = configClass.getMethod("newConfigBuilder", String::class.java)
            val builder = builderMethod.invoke(null, "00000000-0000-0000-0000-000000000000")
            val buildMethod = builder.javaClass.getMethod("build")
            val config = buildMethod.invoke(builder)
            val activateMethod = metricaClass.getMethod("activate", Context::class.java, configClass)
            activateMethod.invoke(null, applicationContext, config)

            val reportMethod = metricaClass.getMethod("reportEvent", String::class.java)
            reportMethod.invoke(null, "App Started")
            Log.d(TAG, "YandexMetrica успешно активирована")
        } catch (e: Throwable) {
            Log.w(TAG, "Ошибка инициализации дополнительных SDK (Firebase/Metrica): ${e.message}")
        }
    }
}

/**
 * Перечисление основных разделов приложения.
 */
private enum class AppSection(val title: String, val icon: String) {
    Cards("Карточки", "cards"),
    Qr("QR", "qr"),
    Profile("Профиль", "profile"),
}

/**
 * Корневой компонент интерфейса TapViz.
 * Управляет глобальным состоянием авторизации, загрузкой карточек,
 * навигацией между экранами и диалогом оценки приложения.
 */
@Composable
private fun TapVizRoot(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val api = remember { ApiClient(context) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val settings = remember { context.getSharedPreferences("tapviz_settings", Context.MODE_PRIVATE) }
    var showRateDialog by remember { mutableStateOf(false) }

    /**
     * Проверка условия отображения диалога оценки приложения.
     */
    fun checkShowRateDialog() {
        val never = settings.getBoolean("never_rate", false)
        if (!never) {
            val saves = settings.getInt("save_count", 0) + 1
            settings.edit { putInt("save_count", saves) }
            Log.d(TAG, "Количество сохранений визиток: $saves")
            if (saves % 3 == 0) {
                Log.i(TAG, "Отображение диалога оценки приложения")
                showRateDialog = true
            }
        }
    }

    var email by remember { mutableStateOf("") }
    var cards by remember { mutableStateOf<List<TapVizCard>>(emptyList()) }
    var cardId by remember { mutableStateOf<String?>(null) }
    var isDemo by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var section by remember { mutableStateOf(AppSection.Cards) }
    var loading by remember { mutableStateOf(true) }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    /**
     * Синхронизация аккаунта и карточек с сервером.
     */
    suspend fun refresh() {
        Log.d(TAG, "Запуск обновления аккаунта и списка карточек")
        loading = true
        try {
            val account = api.loadAccount()
            email = account.email
            cards = account.cards
            cardId = cardId?.takeIf { id -> account.cards.any { it.id == id } } ?: account.cards.firstOrNull()?.id
            isDemo = account.isDemo
            error = null
            Log.i(TAG, "Данные аккаунта успешно загружены: email=$email, cardsCount=${cards.size}, isDemo=$isDemo")
        } catch (failure: Exception) {
            Log.e(TAG, "Ошибка загрузки аккаунта", failure)
            if (failure is ApiException && failure.httpStatus == 401) {
                cards = emptyList()
                cardId = null
                email = ""
                isDemo = false
            } else {
                error = failure.message ?: "Не удалось подключиться к TapViz."
            }
        } finally {
            loading = false
        }
    }

    /**
     * Локальное обновление карточки в списке после сохранения.
     */
    fun updateCard(updated: TapVizCard) {
        Log.d(TAG, "Обновление локальной карточки с id=${updated.id}")
        cards = cards.map { if (it.id == updated.id) updated else it }
    }

    LaunchedEffect(Unit) {
        if (api.hasSession()) {
            Log.d(TAG, "Найдена сохраненная сессия, загружаем данные")
            refresh()
        } else {
            Log.d(TAG, "Сохраненная сессия не найдена, открываем авторизацию")
            loading = false
        }
    }

    // Лаунчеры создания документов (vCard и QR PNG)
    val vcfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/vcard")) { uri ->
        if (uri != null) {
            selectedCard(cards, cardId)?.let {
                Log.i(TAG, "Экспорт vCard для визитки id=${it.id}")
                writeDocument(context, uri, vCard(it))
            }
        }
    }

    val qrLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        val current = selectedCard(cards, cardId)
        if (uri != null && current != null) {
            Log.i(TAG, "Сохранение QR-кода в файл PNG для визитки id=${current.id}")
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    qrBitmap(current.publicUrl).compress(Bitmap.CompressFormat.PNG, 100, output)
                }
            }.onFailure { ex ->
                Log.e(TAG, "Ошибка сохранения QR-кода в PNG", ex)
                scope.launch { snackbar.showSnackbar("Не удалось сохранить QR-код.") }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (email.isNotBlank() && !loading) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    AppSection.entries.forEach { item ->
                        NavigationBarItem(
                            selected = section == item,
                            onClick = {
                                Log.d(TAG, "Переключение на раздел ${item.name}")
                                section = item
                                editing = false
                            },
                            icon = {
                                TapVizIcon(
                                    name = item.icon,
                                    description = item.title,
                                    tint = if (section == item) LocalTapVizColors.current.accent else LocalTapVizColors.current.textMuted,
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (section == item) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LocalTapVizColors.current.accent,
                                selectedTextColor = LocalTapVizColors.current.accent,
                                indicatorColor = LocalTapVizColors.current.accentSoft,
                                unselectedIconColor = LocalTapVizColors.current.textMuted,
                                unselectedTextColor = LocalTapVizColors.current.textMuted,
                            ),
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars,
    ) { padding ->
        when {
            loading -> LoadingState(Modifier.padding(padding))

            email.isBlank() -> AuthScreen(
                error = error,
                onAuth = { address, password, register ->
                    scope.launch {
                        Log.i(TAG, "Запрос авторизации: register=$register, email=$address")
                        loading = true
                        error = null
                        try {
                            if (register) api.register(address, password) else api.login(address, password)
                            email = address
                            refresh()
                        } catch (failure: Exception) {
                            Log.e(TAG, "Ошибка авторизации", failure)
                            error = userMessage(failure)
                            loading = false
                        }
                    }
                },
                onDemo = {
                    scope.launch {
                        Log.i(TAG, "Вход в демо-аккаунт")
                        loading = true
                        error = null
                        try {
                            api.loginDemo()
                            refresh()
                        } catch (failure: Exception) {
                            Log.e(TAG, "Ошибка входа в демо-аккаунт", failure)
                            error = userMessage(failure)
                            loading = false
                        }
                    }
                },
                modifier = Modifier.padding(padding),
            )

            cards.isEmpty() -> ConnectionState(
                message = error ?: "Не удалось загрузить карточку.",
                onRetry = { scope.launch { refresh() } },
                onSignOut = {
                    scope.launch {
                        Log.i(TAG, "Выход из аккаунта со страницы ошибки")
                        runCatching { api.logout() }
                        email = ""
                        cards = emptyList()
                        error = null
                    }
                },
                modifier = Modifier.padding(padding),
            )

            else -> {
                val currentCard = selectedCard(cards, cardId) ?: cards.first()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    if (isDemo) DemoBanner()
                    if (working) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = LocalTapVizColors.current.accent,
                            trackColor = LocalTapVizColors.current.surfaceRaised,
                        )
                    }

                    AnimatedContent(
                        targetState = editing && section == AppSection.Cards,
                        transitionSpec = {
                            if (targetState) {
                                (slideInVertically(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { height -> height / 2 } + fadeIn(tween(240))) togetherWith
                                    (slideOutVertically(tween(180)) { height -> -height / 4 } + fadeOut(tween(180)))
                            } else {
                                (slideInVertically(tween(240)) { height -> -height / 4 } + fadeIn(tween(240))) togetherWith
                                    (slideOutVertically(spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMedium)) { height -> height / 2 } + fadeOut(tween(180)))
                            }
                        },
                        label = "editor_transition",
                        modifier = Modifier.weight(1f),
                    ) { isEditing ->
                        if (isEditing) {
                            CardEditorScreen(
                                card = currentCard,
                                userEmail = email,
                                saving = working,
                                error = error,
                                onSave = { edited ->
                                    scope.launch {
                                        Log.i(TAG, "Сохранение и публикация визитки id=${edited.id}")
                                        working = true
                                        try {
                                            val saved = api.save(edited)
                                            val published = api.publish(saved.id, true)
                                            updateCard(published)
                                            snackbar.showSnackbar("Изменения сохранены и опубликованы.")
                                            checkShowRateDialog()
                                            editing = false
                                        } catch (failure: Exception) {
                                            Log.e(TAG, "Ошибка сохранения визитки", failure)
                                            snackbar.showSnackbar(userMessage(failure))
                                        } finally {
                                            working = false
                                        }
                                    }
                                },
                                onPublish = { editedCard, publish ->
                                    scope.launch {
                                        Log.i(TAG, "Смена статуса публикации визитки id=${editedCard.id}: publish=$publish")
                                        working = true
                                        try {
                                            val edited = api.save(editedCard)
                                            val published = api.publish(edited.id, publish)
                                            updateCard(published)
                                            snackbar.showSnackbar(if (publish) "Визитка опубликована." else "Визитка скрыта.")
                                        } catch (failure: Exception) {
                                            Log.e(TAG, "Ошибка публикации визитки", failure)
                                            snackbar.showSnackbar(userMessage(failure))
                                        } finally {
                                            working = false
                                        }
                                    }
                                },
                                onPreviewWebsite = { edited ->
                                    scope.launch {
                                        Log.i(TAG, "Предпросмотр веб-визитки id=${edited.id}")
                                        working = true
                                        try {
                                            val saved = api.save(edited)
                                            updateCard(saved)
                                            openLink(context, api.createWebsitePreview(saved.id))
                                        } catch (failure: Exception) {
                                            Log.e(TAG, "Ошибка создания предпросмотра веб-визитки", failure)
                                            snackbar.showSnackbar(userMessage(failure))
                                        } finally {
                                            working = false
                                        }
                                    }
                                },
                                onBack = {
                                    Log.d(TAG, "Закрытие редактора визитки")
                                    editing = false
                                },
                            )
                        } else {
                            AnimatedContent(
                                targetState = section,
                                transitionSpec = {
                                    val targetIndex = targetState.ordinal
                                    val initialIndex = initialState.ordinal
                                    if (targetIndex > initialIndex) {
                                        (slideInHorizontally(tween(240)) { width -> width / 3 } + fadeIn(tween(240))) togetherWith
                                            (slideOutHorizontally(tween(180)) { width -> -width / 4 } + fadeOut(tween(180)))
                                    } else {
                                        (slideInHorizontally(tween(240)) { width -> -width / 3 } + fadeIn(tween(240))) togetherWith
                                            (slideOutHorizontally(tween(180)) { width -> width / 4 } + fadeOut(tween(180)))
                                    }
                                },
                                label = "section_transition",
                                modifier = Modifier.fillMaxSize(),
                            ) { currentSection ->
                                when (currentSection) {
                                    AppSection.Cards -> TapVizHomeScreen(
                                        cards = cards,
                                        selectedCardId = currentCard.id,
                                        readOnly = isDemo,
                                        showBottomNavigation = false,
                                        onCreateCard = {
                                            scope.launch {
                                                if (isDemo) {
                                                    snackbar.showSnackbar("Демо-аккаунт доступен только для просмотра.")
                                                } else {
                                                    Log.i(TAG, "Создание новой визитки")
                                                    working = true
                                                    try {
                                                        val created = api.createCard("Новая карточка")
                                                        val defaultName = created.displayName.ifBlank {
                                                            email.substringBefore('@')
                                                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                                                .ifBlank { "Визитка" }
                                                        }

                                                        val initialFields = if (created.fields.none { it.type == "email" }) {
                                                            listOf(ContactField(UUID.randomUUID().toString(), "email", "Email", email, "public"))
                                                        } else created.fields

                                                        val cardWithEmail = created.copy(displayName = defaultName, fields = initialFields)
                                                        val saved = runCatching { api.save(cardWithEmail) }.getOrDefault(cardWithEmail)

                                                        cards = cards.filter { it.id != saved.id } + saved
                                                        cardId = saved.id
                                                        editing = true
                                                        Log.d(TAG, "Новая визитка создана: id=${saved.id}")
                                                    } catch (failure: Exception) {
                                                        Log.e(TAG, "Ошибка создания карточки", failure)
                                                        snackbar.showSnackbar(userMessage(failure))
                                                    } finally {
                                                        working = false
                                                    }
                                                }
                                            }
                                        },
                                        onOpenQr = {
                                            cardId = it.id
                                            section = AppSection.Qr
                                            editing = false
                                        },
                                        onShare = { shareLink(context, it.publicUrl) },
                                        onEdit = { card ->
                                            if (isDemo) {
                                                scope.launch { snackbar.showSnackbar("Демо-аккаунт доступен только для просмотра.") }
                                            } else {
                                                cardId = card.id
                                                editing = true
                                            }
                                        },
                                        onCopyLink = { copyLink(context, it.publicUrl, scope, snackbar) },
                                        onOpenProfile = { openLink(context, it.publicUrl) },
                                        onNavigate = { name ->
                                            section = AppSection.valueOf(name)
                                            editing = false
                                        },
                                    )

                                    AppSection.Qr -> QrScreen(
                                        card = currentCard,
                                        onCopy = { copyLink(context, currentCard.publicUrl, scope, snackbar) },
                                        onShare = { shareLink(context, currentCard.publicUrl) },
                                        onSaveQr = { qrLauncher.launch("tapviz-${currentCard.slug}.png") },
                                        onOpenProfile = { openLink(context, currentCard.publicUrl) },
                                    )

                                    AppSection.Profile -> ProfileScreen(
                                        email = email,
                                        card = currentCard,
                                        isDemo = isDemo,
                                        darkMode = darkMode,
                                        working = working,
                                        onExport = { vcfLauncher.launch("tapviz-${currentCard.slug}.vcf") },
                                        onDarkModeChange = onDarkModeChange,
                                        onOpenWebsite = { openLink(context, currentCard.publicUrl) },
                                        onSignOut = {
                                            scope.launch {
                                                Log.i(TAG, "Выход из аккаунта пользователем")
                                                runCatching { api.logout() }
                                                email = ""
                                                cards = emptyList()
                                                cardId = null
                                                isDemo = false
                                                editing = false
                                                section = AppSection.Cards
                                                error = null
                                            }
                                        },
                                        onDeleteAccount = {
                                            scope.launch {
                                                Log.w(TAG, "Удаление аккаунта пользователем: email=$email")
                                                working = true
                                                try {
                                                    api.deleteAccount(email)
                                                    email = ""
                                                    cards = emptyList()
                                                    cardId = null
                                                    isDemo = false
                                                    editing = false
                                                    section = AppSection.Cards
                                                    error = null
                                                    snackbar.showSnackbar("Аккаунт и визитки удалены.")
                                                } catch (failure: Exception) {
                                                    Log.e(TAG, "Ошибка удаления аккаунта", failure)
                                                    snackbar.showSnackbar(userMessage(failure))
                                                } finally {
                                                    working = false
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRateDialog) {
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text(stringResource(R.string.rate_title)) },
            text = { Text(stringResource(R.string.rate_message, BuildConfig.STORE_NAME)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRateDialog = false
                        settings.edit { putBoolean("never_rate", true) }
                        openLink(context, BuildConfig.STORE_REVIEW_URL)
                    },
                ) {
                    Text(stringResource(R.string.rate_button, BuildConfig.STORE_NAME))
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showRateDialog = false
                            settings.edit { putBoolean("never_rate", true) }
                        },
                    ) {
                        Text(stringResource(R.string.rate_never_ask))
                    }
                    TextButton(onClick = { showRateDialog = false }) {
                        Text(stringResource(R.string.rate_later))
                    }
                }
            },
        )
    }
}

/**
 * Получение текущей выбранной карточки по id.
 */
private fun selectedCard(cards: List<TapVizCard>, id: String?): TapVizCard? =
    cards.firstOrNull { it.id == id } ?: cards.firstOrNull()

/**
 * Баннер демо-режима для ограниченного аккаунта.
 */
@Composable
private fun DemoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.demo_badge), color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.demo_view_only), color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Экран входа и регистрации.
 */
@Composable
private fun AuthScreen(
    error: String?,
    onAuth: (String, String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onDemo: () -> Unit = {},
) {
    var register by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 25.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        BrandWordmark()

        Text(
            if (register) "Твоя визитка. Твои правила." else "Снова в сети.",
            fontSize = 38.sp,
            lineHeight = 42.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            "Одна ссылка, и ваши контакты уже у собеседника.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(25.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.take(254) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.email_label)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it.take(128) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.password_label)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        if (error != null) {
            Spacer(Modifier.height(9.dp))
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(19.dp))
        AnimatedButton(
            onClick = { onAuth(email.trim(), password, register) },
            enabled = email.contains('@') && password.length >= 10,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(if (register) "Создать аккаунт" else "Войти", fontWeight = FontWeight.Bold)
        }
        TextButton(
            onClick = { register = !register },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(if (register) "Уже есть аккаунт? Войти" else "Впервые в TapViz? Создать аккаунт", fontWeight = FontWeight.SemiBold)
        }

    }
}

/**
 * Анимированная кнопка с откликом на нажатие.
 */
@Composable
private fun AnimatedButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (interaction.collectIsPressedAsState().value) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "btn_press_scale",
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(18.dp),
    ) {
        content()
    }
}

/**
 * Экран генерации и демонстрации QR-кода публичной визитки.
 */
@Composable
private fun QrScreen(
    card: TapVizCard,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSaveQr: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PageTitle("TAPVIZ / SHARE", "Твой QR", "Открой страницу визитки без приложения.")
        Spacer(Modifier.height(18.dp))
        if (card.status != "published") {
            EmptyCard(title = stringResource(R.string.empty_card_publish_first), message = stringResource(R.string.qr_appears_after_publication))
        } else {
            val bitmap = remember(card.publicUrl) { qrBitmap(card.publicUrl, 600) }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(30.dp),
            ) {
                Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .padding(10.dp),
                    ) {
                        Image(
                            bitmap.asImageBitmap(),
                            contentDescription = "QR-код визитки ${card.displayName}",
                            modifier = Modifier.size(236.dp),
                        )
                    }
                    Text(card.displayName, Modifier.padding(top = 16.dp), fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                    Text(card.role.ifBlank { "TapViz" }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(15.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(17.dp),
                ) {
                    Text(stringResource(R.string.copy))
                }
                Button(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(17.dp),
                ) {
                    Text(stringResource(R.string.share))
                }
            }
            OutlinedButton(
                onClick = onSaveQr,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 9.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(17.dp),
            ) {
                Text(stringResource(R.string.save_qr_code))
            }
            TextButton(onClick = onOpenProfile) {
                Text(stringResource(R.string.open_public_page))
            }
            Text(stringResource(R.string.public_link_hint), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

/**
 * Экран управления профилем, настройками темы и экспортом данных.
 */
@Composable
private fun ProfileScreen(
    email: String,
    card: TapVizCard,
    isDemo: Boolean = false,
    darkMode: Boolean = false,
    working: Boolean = false,
    onExport: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    onDarkModeChange: (Boolean) -> Unit = {},
    onOpenWebsite: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
) {
    val context = LocalContext.current
    var deleteDialog by remember { mutableStateOf(false) }
    var confirmation by remember { mutableStateOf("") }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        PageTitle("TAPVIZ / ACCOUNT", "Твой\nпрофиль", "Аккаунт, оформление и данные.")
        Spacer(Modifier.height(18.dp))
        SectionCard(title = if (isDemo) stringResource(R.string.demo_mode) else stringResource(R.string.account), subtitle = email) {
            if (isDemo) Text(stringResource(R.string.demo_account_notice), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.public_link_privacy_notice), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(
                onClick = onOpenWebsite,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                enabled = card.status == "published",
                shape = RoundedCornerShape(14.dp),
            ) {
                TapVizIcon("web", "", MaterialTheme.colorScheme.primary, size = 19.dp)
                Spacer(Modifier.width(8.dp))
                Text(if (card.status == "published") stringResource(R.string.open_public_page) else stringResource(R.string.page_appears_after_publication))
            }
        }
        Spacer(Modifier.height(12.dp))
        SectionCard(title = stringResource(R.string.section_appearance), subtitle = stringResource(R.string.settings_saved_locally)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TapVizIcon(if (darkMode) "palette" else "web", stringResource(R.string.app_theme), MaterialTheme.colorScheme.primary, size = 22.dp)
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                ) {
                    Text(if (darkMode) stringResource(R.string.dark_theme) else stringResource(R.string.light_theme), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(stringResource(R.string.can_switch_anytime), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
                Switch(checked = darkMode, onCheckedChange = onDarkModeChange, enabled = !working)
            }
        }
        Spacer(Modifier.height(12.dp))
        SectionCard(title = stringResource(R.string.section_about), subtitle = stringResource(R.string.app_version_format, BuildConfig.STORE_NAME)) {
            Text(stringResource(R.string.app_description_format, BuildConfig.STORE_NAME), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(
                onClick = { openLink(context, BuildConfig.STORE_URL) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(stringResource(R.string.open_store_page_format, BuildConfig.STORE_NAME))
            }
        }
        Spacer(Modifier.height(12.dp))
        SectionCard(title = stringResource(R.string.section_export), subtitle = stringResource(R.string.save_contact_file_subtitle)) {
            Button(onClick = onExport, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                TapVizIcon("download", "", MaterialTheme.colorScheme.onPrimary, size = 18.dp)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.save_contact_vcf))
            }
            Text(stringResource(R.string.vcf_file_hint), Modifier.padding(top = 7.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        SectionCard(title = stringResource(R.string.section_privacy), subtitle = stringResource(R.string.contacts_hidden_notice)) {
            Text(stringResource(R.string.public_profile_privacy_details), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !working,
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(stringResource(R.string.sign_out))
        }
        if (!isDemo) {
            TextButton(
                onClick = { confirmation = ""; deleteDialog = true },
                enabled = !working,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                TapVizIcon("trash", "", MaterialTheme.colorScheme.error, size = 18.dp)
                Spacer(Modifier.width(7.dp))
                Text(stringResource(R.string.delete_account_and_cards), color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!working) deleteDialog = false },
            title = { Text(stringResource(R.string.delete_account_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.delete_account_dialog_message), fontSize = 13.sp)
                    OutlinedTextField(
                        value = confirmation,
                        onValueChange = { confirmation = it.take(254) },
                        label = { Text(email) },
                        singleLine = true,
                        enabled = !working,
                        shape = RoundedCornerShape(14.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onDeleteAccount,
                    enabled = confirmation.trim().equals(email, ignoreCase = true) && !working,
                ) {
                    if (working) {
                        CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.delete_account_forever), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialog = false }, enabled = !working) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

/**
 * Логотип бренда TapViz.
 */
@Composable
private fun BrandWordmark() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.tapviz_logo_main),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
        )
        Spacer(Modifier.height(5.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tap", fontSize = 31.sp, fontWeight = FontWeight.Black, letterSpacing = (-1.5).sp)
            Text("Viz", fontSize = 31.sp, fontWeight = FontWeight.Black, letterSpacing = (-1.5).sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/**
 * Заголовок раздела.
 */
@Composable
private fun PageTitle(eyebrow: String, title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth()) {
        Eyebrow(eyebrow)
        Text(title, fontSize = 36.sp, lineHeight = 39.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
        Spacer(Modifier.height(3.dp))
        Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Eyebrow(value: String) {
    Text(value, fontSize = 9.sp, letterSpacing = 1.1.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun SectionCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(15.dp))
            content()
        }
    }
}

@Composable
private fun EmptyCard(title: String = "Сначала опубликуйте визитку", message: String = "QR-код появится после публикации карточки.") {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(26.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("⌁", fontSize = 34.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Индикатор первоначальной загрузки данных.
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    val colors = LocalTapVizColors.current
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(76.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(Modifier.fillMaxSize(), strokeWidth = 2.5.dp, color = colors.accent, trackColor = colors.surfaceRaised)
            Image(painterResource(R.drawable.tapviz_logo_main), contentDescription = null, modifier = Modifier.size(30.dp), colorFilter = ColorFilter.tint(colors.textPrimary))
        }
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.app_name), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
        Spacer(Modifier.height(5.dp))
        Text(stringResource(R.string.connecting_cards), color = colors.textSecondary, fontSize = 12.sp)
    }
}

/**
 * Состояние ошибки подключения к серверу.
 */
@Composable
private fun ConnectionState(
    message: String,
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.server_connection_error), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.retry)) }
        TextButton(onClick = onSignOut) { Text(stringResource(R.string.sign_out)) }
    }
}

/**
 * Формирование сообщений об ошибках для пользователя.
 */
private fun userMessage(error: Exception): String = when (error) {
    is ApiException -> when (error.code) {
        "weak_password" -> "Пароль должен содержать от 10 символов."
        "invalid_credentials" -> "Email или пароль указаны неверно."
        "profile_incomplete" -> "Пожалуйста, укажите имя и фамилию."
        "version_conflict" -> "Данные обновлены. Нажмите «Сохранить» еще раз."
        "api_not_configured" -> error.message ?: "Адрес сервера не настроен."
        "avatar_too_large", "invalid_avatar_format" -> "Фото автоматически оптимизировано для карточки."
        "card_not_found" -> "Визитка не найдена или была удалена."
        "unauthorized", "session_expired" -> "Сессия истекла. Пожалуйста, войдите в аккаунт снова."
        "rate_limit" -> "Слишком много запросов. Попробуйте через минуту."
        else -> error.message.takeIf { !it.isNullOrBlank() } ?: "Не удалось выполнить запрос."
    }
    else -> error.message.takeIf { !it.isNullOrBlank() } ?: "Сервер недоступен. Проверьте подключение к интернету."
}

/**
 * Генерация растрового изображения QR-кода.
 */
private fun qrBitmap(value: String, size: Int = 600): Bitmap {
    val matrix = MultiFormatWriter().encode(value, BarcodeFormat.QR_CODE, size, size, mapOf(EncodeHintType.MARGIN to 2))
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.rgb(16, 27, 35) else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

/**
 * Формирование текстового файла vCard 3.0.
 */
private fun vCard(card: TapVizCard): String {
    fun escape(value: String) = value.replace("\\", "\\\\").replace("\n", "\\n").replace(",", "\\,").replace(";", "\\;")
    val parts = card.displayName.trim().split(Regex("\\s+")).filter(String::isNotBlank)
    val lastName = parts.drop(1).joinToString(" ")
    val firstName = parts.firstOrNull().orEmpty()

    val lines = mutableListOf(
        "BEGIN:VCARD",
        "VERSION:3.0",
        "N:${escape(lastName)};${escape(firstName)};;;",
        "FN:${escape(card.displayName)}",
    )
    if (card.organization.isNotBlank()) lines += "ORG:${escape(card.organization)}"
    if (card.role.isNotBlank()) lines += "TITLE:${escape(card.role)}"
    card.fields.filter { it.enabled && it.value.isNotBlank() }.forEach { field ->
        when (field.type) {
            "phone" -> lines += "TEL;TYPE=WORK,VOICE:${escape(field.value)}"
            "email" -> lines += "EMAIL;TYPE=INTERNET:${escape(field.value)}"
            "website", "social", "messenger" -> lines += "URL:${escape(field.value)}"
            "address" -> lines += "ADR;TYPE=WORK:;;${escape(field.value)};;;;"
        }
    }
    if (card.bio.isNotBlank()) lines += "NOTE:${escape(card.bio)}"
    lines += "END:VCARD"
    return lines.joinToString("\r\n", postfix = "\r\n")
}

private fun writeDocument(context: Context, uri: Uri, content: String) {
    runCatching {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(content) }
    }.onFailure { ex ->
        Log.e(TAG, "Ошибка записи документа $uri", ex)
    }
}

private fun copyLink(context: Context, link: String, scope: CoroutineScope, snackbar: SnackbarHostState) {
    Log.d(TAG, "Копирование ссылки $link в буфер обмена")
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("TapViz", link))
    scope.launch { snackbar.showSnackbar("Ссылка скопирована.") }
}

private fun shareLink(context: Context, link: String) {
    Log.d(TAG, "Отправка интента шеринга ссылки $link")
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, link)
        putExtra(Intent.EXTRA_TITLE, "Моя визитка TapViz")
    }
    context.startActivity(Intent.createChooser(intent, "Поделиться визиткой"))
}

private fun openLink(context: Context, link: String) {
    Log.d(TAG, "Открытие ссылки в браузере: $link")
    context.startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
}

// Тестовая карточка для превью
private val previewCard = TapVizCard(
    id = "preview-card-id",
    slug = "anna-design",
    label = "Основная карточка",
    displayName = "Анна Петрова",
    role = "Продуктовый дизайнер",
    organization = "North Studio",
    bio = "Помогаю цифровым продуктам становиться понятнее и удобнее.",
    theme = "moss",
    status = "published",
    version = 3,
    fields = listOf(
        ContactField("preview-phone", "phone", "Телефон", "+7 900 123-45-67", "public"),
        ContactField("preview-email", "email", "Email", "anna@example.com", "public"),
        ContactField("preview-site", "website", "Портфолио", "https://anna.design", "private"),
    ),
)

// ==================== PREVIEWS (ПРЕВЬЮ ДЛЯ ВСЕХ ЭКРАНОВ И АКТИВИТИ) ====================

@Preview(name = "MainActivity · Тёмная тема", showBackground = true, backgroundColor = 0xFF080B10, widthDp = 393, heightDp = 852)
@Composable
private fun MainActivityDarkPreview() {
    TapVizTheme(darkTheme = true, dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            TapVizRoot(darkMode = true, onDarkModeChange = {})
        }
    }
}

@Preview(name = "MainActivity · Светлая тема", showBackground = true, backgroundColor = 0xFFF1F5F8, widthDp = 393, heightDp = 852)
@Composable
private fun MainActivityLightPreview() {
    TapVizTheme(darkTheme = false, dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            TapVizRoot(darkMode = false, onDarkModeChange = {})
        }
    }
}

@Preview(name = "Авторизация · телефон", showBackground = true, backgroundColor = 0xFFF3F5F1, widthDp = 393, heightDp = 852)
@Composable
private fun AuthScreenPreview() {
    TapVizTheme(darkTheme = false, dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AuthScreen(error = null, onAuth = { _, _, _ -> }, onDemo = {})
        }
    }
}

@Preview(name = "QR-код", showBackground = true, backgroundColor = 0xFFF3F5F1, widthDp = 393, heightDp = 852)
@Composable
private fun QrScreenPreview() {
    TapVizTheme(darkTheme = false, dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            QrScreen(
                card = previewCard,
                onCopy = {},
                onShare = {},
                onSaveQr = {},
                onOpenProfile = {},
            )
        }
    }
}

@Preview(name = "Настройки профиля", showBackground = true, backgroundColor = 0xFFF3F5F1, widthDp = 393, heightDp = 852)
@Composable
private fun ProfileScreenPreview() {
    TapVizTheme(darkTheme = false, dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProfileScreen(email = "anna@example.com", card = previewCard, onExport = {}, onSignOut = {})
        }
    }
}

@Preview(name = "Загрузка", showBackground = true, backgroundColor = 0xFF080B10, widthDp = 393, heightDp = 852)
@Composable
private fun LoadingStatePreview() {
    TapVizTheme(darkTheme = true, dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            LoadingState()
        }
    }
}

@Preview(name = "Ошибка подключения", showBackground = true, backgroundColor = 0xFF080B10, widthDp = 393, heightDp = 852)
@Composable
private fun ConnectionStatePreview() {
    TapVizTheme(darkTheme = true, dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ConnectionState(message = "Сервер недоступен", onRetry = {}, onSignOut = {})
        }
    }
}

@Preview(name = "Демо-баннер", showBackground = true, widthDp = 393)
@Composable
private fun DemoBannerPreview() {
    TapVizTheme(darkTheme = true, dynamicColor = false) {
        DemoBanner()
    }
}
