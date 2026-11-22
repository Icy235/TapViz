package icy.tapviz.app.ui.card

import androidx.compose.ui.graphics.Color

/**
 * Описание цветовой палитры лицевой стороны визитки.
 * Значения совпадают с стилями в `profile.css` на сервере.
 *
 * @property id Уникальный ключ темы.
 * @property name Название палитры в интерфейсе редактора.
 * @property bgTop Верхний цвет градиента фона.
 * @property bgBottom Нижний цвет градиента фона.
 * @property accent Первичный акцентный цвет.
 * @property accent2 Вторичный акцентный цвет (градиент).
 * @property onAccent Цвет текста на акцентном фоне.
 * @property text Основной цвет текста карточки.
 * @property muted Приглушенный цвет текста карточки.
 * @property light Флаг светлой схемы оформления.
 */
data class CardPalette(
    val id: String,
    val name: String,
    val bgTop: Color,
    val bgBottom: Color,
    val accent: Color,
    val accent2: Color,
    val onAccent: Color,
    val text: Color,
    val muted: Color,
    val light: Boolean = false,
)

/**
 * Шаблон визитки: сочетание темы и готового набора каналов связи.
 *
 * @property id Идентификатор шаблона.
 * @property name Название шаблона в редакторе.
 * @property tagline Краткое описание назначения шаблона.
 * @property theme Палитра по умолчанию для данного шаблона.
 * @property channels Список рекомендуемых каналов связи.
 */
data class CardTemplate(
    val id: String,
    val name: String,
    val tagline: String,
    val theme: String,
    val channels: List<String>,
)

/**
 * Справочник палитр и пресетов оформления визиток TapViz.
 */
object CardStyles {
    private val lightText = Color(0xFF0B1220)
    private val darkText = Color(0xFFF3F7FC)
    private val mutedDarkText = Color(0xFF93A3B4)

    /**
     * Все доступные палитры визиток.
     */
    val palettes = listOf(
        CardPalette("ink", "Чернила", Color(0xFF151F2B), Color(0xFF0B1119), Color(0xFF35D9FF), Color(0xFFAA83FF), Color(0xFF051016), darkText, mutedDarkText),
        CardPalette("aurora", "Аврора", Color(0xFF15253C), Color(0xFF0B1526), Color(0xFF5EEAD4), Color(0xFF818CF8), Color(0xFF04141A), darkText, mutedDarkText),
        CardPalette("ocean", "Океан", Color(0xFF0F2F50), Color(0xFF081A2E), Color(0xFF38BDF8), Color(0xFF2563EB), Color(0xFF021019), darkText, mutedDarkText),
        CardPalette("moss", "Мох", Color(0xFF182A22), Color(0xFF0C1612), Color(0xFF73EDC7), Color(0xFFD1F47B), Color(0xFF06150F), darkText, mutedDarkText),
        CardPalette("sunset", "Закат", Color(0xFF341A28), Color(0xFF180B12), Color(0xFFFB923C), Color(0xFFF43F5E), Color(0xFF1C0A05), darkText, mutedDarkText),
        CardPalette("plum", "Слива", Color(0xFF2B1941), Color(0xFF140B21), Color(0xFFC084FC), Color(0xFFF472B6), Color(0xFF150822), darkText, mutedDarkText),
        CardPalette("paper", "Бумага", Color(0xFFFFFFFF), Color(0xFFE6ECF4), Color(0xFF2563EB), Color(0xFF7C3AED), Color.White, lightText, Color(0xFF566578), light = true),
        CardPalette("mono", "Моно", Color(0xFF1F1F1F), Color(0xFF0E0E0E), Color.White, Color(0xFF9CA3AF), Color(0xFF0A0A0A), darkText, Color(0xFFA3A3A3)),
    )

    /**
     * Поиск палитры по идентификатору темы.
     */
    fun palette(theme: String): CardPalette = palettes.firstOrNull { it.id == theme } ?: palettes.first()

    /**
     * Предустановленные шаблоны под разные роли и сферы деятельности.
     */
    val templates = listOf(
        CardTemplate("business", "Бизнес", "Строго и понятно", "paper", listOf("phone", "email", "website", "whatsapp", "telegram")),
        CardTemplate("creator", "Креатор", "Для блога и соцсетей", "sunset", listOf("instagram", "youtube", "tiktok", "telegram")),
        CardTemplate("designer", "Дизайнер", "Портфолио в один тап", "plum", listOf("behance", "dribbble", "telegram", "email")),
        CardTemplate("dev", "Разработчик", "Код и контакты", "aurora", listOf("github", "telegram", "linkedin", "email")),
        CardTemplate("studio", "Студия", "Для команды и услуг", "ocean", listOf("website", "phone", "telegram", "max")),
        CardTemplate("eco", "Локальный бизнес", "Заказ и адрес", "moss", listOf("whatsapp", "instagram", "address", "phone")),
        CardTemplate("minimal", "Минимализм", "Только главное", "mono", listOf("phone", "email", "telegram")),
        CardTemplate("classic", "Классика", "Тёмная и спокойная", "ink", listOf("phone", "email", "telegram")),
    )
}
