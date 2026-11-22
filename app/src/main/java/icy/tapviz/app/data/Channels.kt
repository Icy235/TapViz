package icy.tapviz.app.data

import android.util.Log
import java.util.UUID

private const val TAG = "Channels"

/**
 * Описание поддерживаемого типа канала связи или социальной сети.
 *
 * @property type Уникальный идентификатор типа (например "phone", "telegram", "github").
 * @property label Человекочитаемое название соцсети или сервиса.
 * @property hint Плейсхолдер или подсказка формата для ввода.
 * @property color Цветовой акцент бренда в формате ARGB Long.
 * @property glyph Символьный фолбэк для иконки (если системный вектор недоступен).
 * @property social Является ли тип социальной сетью/мессенджером.
 */
data class ChannelKind(
    val type: String,
    val label: String,
    val hint: String,
    val color: Long,
    val glyph: String,
    val social: Boolean,
)

/**
 * Реестр доступных каналов связи визитки TapViz.
 * Синхронизирован с серверной логикой `channels.mjs`.
 */
object Channels {
    /**
     * Полный список поддерживаемых способов связи.
     */
    val all = listOf(
        ChannelKind("phone", "Телефон", "+7 900 123-45-67", 0xFF34D399, "☎", false),
        ChannelKind("email", "Email", "name@example.com", 0xFF60A5FA, "✉", false),
        ChannelKind("website", "Сайт", "example.com", 0xFFA78BFA, "◎", false),
        ChannelKind("address", "Адрес", "Город, улица, дом", 0xFFFBBF24, "⌂", false),
        ChannelKind("telegram", "Telegram", "@username", 0xFF27A7E7, "T", true),
        ChannelKind("max", "MAX", "Ссылка на профиль", 0xFF7B61FF, "M", true),
        ChannelKind("whatsapp", "WhatsApp", "+7 900 123-45-67", 0xFF25D366, "W", true),
        ChannelKind("instagram", "Instagram", "@username", 0xFFE4405F, "I", true),
        ChannelKind("youtube", "YouTube", "@channel", 0xFFFF3D3D, "▶", true),
        ChannelKind("discord", "Discord", "Ссылка-приглашение", 0xFF5865F2, "D", true),
        ChannelKind("snapchat", "Snapchat", "username", 0xFFFFFC00, "S", true),
        ChannelKind("vk", "ВКонтакте", "id или короткое имя", 0xFF0077FF, "VK", true),
        ChannelKind("tiktok", "TikTok", "@username", 0xFF25F4EE, "♪", true),
        ChannelKind("github", "GitHub", "username", 0xFFE6EDF3, "G", true),
        ChannelKind("linkedin", "LinkedIn", "username", 0xFF0A8FDC, "in", true),
        ChannelKind("x", "X", "@username", 0xFFE7E9EA, "X", true),
        ChannelKind("facebook", "Facebook", "username", 0xFF1877F2, "f", true),
        ChannelKind("twitch", "Twitch", "username", 0xFF9146FF, "T", true),
        ChannelKind("behance", "Behance", "username", 0xFF1769FF, "Be", true),
        ChannelKind("dribbble", "Dribbble", "username", 0xFFEA4C89, "D", true),
    )

    private val fallback = ChannelKind("link", "Ссылка", "https://…", 0xFF8897A9, "↗", false)

    /**
     * Поиск типа канала по его идентификатору. Возвращает резервную конфигурацию, если тип не найден.
     */
    fun of(type: String): ChannelKind {
        val kind = all.firstOrNull { it.type == type } ?: fallback
        if (kind == fallback && type != "link") {
            Log.d(TAG, "Тип канала '$type' не найден в реестре, используется фолбэк")
        }
        return kind
    }

    /**
     * Создание нового поля контакта с автоматически сгенерированным GUID.
     */
    fun newField(type: String, value: String = ""): ContactField {
        val kind = of(type)
        Log.d(TAG, "Создание нового контактного поля типа '${kind.type}'")
        return ContactField(
            id = UUID.randomUUID().toString(),
            type = type,
            label = kind.label,
            value = value,
        )
    }
}
