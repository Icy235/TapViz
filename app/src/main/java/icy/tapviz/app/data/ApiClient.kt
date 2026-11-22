package icy.tapviz.app.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import icy.tapviz.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val TAG_API = "ApiClient"
private const val TAG_VAULT = "SessionVault"

/**
 * Контактное поле визитки (например, телефон, email, Telegram и т.д.).
 *
 * @property id Уникальный идентификатор поля.
 * @property type Тип канала связи (телефон, email, website...).
 * @property label Человекочитаемое название поля.
 * @property value Значение или ссылка.
 * @property visibility "public" если поле видно всем, или "private" если зашифровано/скрыто.
 * @property enabled Включено ли отображение поля.
 */
data class ContactField(
    val id: String,
    val type: String,
    val label: String,
    val value: String,
    val visibility: String = "public",
    val enabled: Boolean = true,
)

/**
 * Модель визитки TapViz.
 *
 * @property id Уникальный GUID визитки.
 * @property slug Уникальный человекочитаемый адрес (URL-алиас).
 * @property label Название карточки в интерфейсе (например, "Рабочая").
 * @property displayName Имя владельца визитки.
 * @property role Должность или род деятельности.
 * @property organization Название компании/студии.
 * @property bio Описание / био.
 * @property avatarData Base64 фото профиля (если есть).
 * @property backgroundData Base64 фонового изображения (если есть).
 * @property logoData Base64 логотипа компании (если есть).
 * @property theme Название цветовой темы карточки.
 * @property status Статус карточки ("published" или "draft").
 * @property version Версия данных карточки для оптимистичной блокировки.
 * @property fields Список контактных полей.
 */
data class TapVizCard(
    val id: String,
    val slug: String,
    val label: String,
    val displayName: String,
    val role: String,
    val organization: String,
    val bio: String,
    val avatarData: String? = null,
    val backgroundData: String? = null,
    val logoData: String? = null,
    val theme: String,
    val status: String,
    val version: Int,
    val fields: List<ContactField>,
) {
    /**
     * Публичный URL карточки для шеринга или QR-кода.
     */
    val publicUrl: String
        get() = "${(BuildConfig.PUBLIC_BASE_URL.ifBlank { BuildConfig.API_BASE_URL }).trimEnd('/')}/p/$slug"

    companion object {
        /**
         * Парсинг объекта TapVizCard из JSON-ответа сервера.
         */
        fun from(json: JSONObject): TapVizCard {
            val fieldArray = json.optJSONArray("fields") ?: JSONArray()
            val fields = (0 until fieldArray.length()).mapNotNull { index ->
                val field = fieldArray.optJSONObject(index) ?: return@mapNotNull null
                ContactField(
                    id = field.optString("id"),
                    type = field.optString("type"),
                    label = field.optString("label"),
                    value = field.optString("value"),
                    visibility = field.optString("visibility", "public"),
                    enabled = field.optBoolean("enabled", true),
                )
            }
            return TapVizCard(
                id = json.optString("id"),
                slug = json.optString("slug"),
                label = json.optString("label"),
                displayName = json.optString("displayName"),
                role = json.optString("role"),
                organization = json.optString("organization"),
                bio = json.optString("bio"),
                avatarData = json.optString("avatarData").takeIf { it.isNotBlank() },
                backgroundData = json.optString("backgroundData").takeIf { it.isNotBlank() },
                logoData = json.optString("logoData").takeIf { it.isNotBlank() },
                theme = json.optString("theme", "ink"),
                status = json.optString("status", "draft"),
                version = json.optInt("version", 1),
                fields = fields,
            )
        }
    }
}

/**
 * Исключение вызова REST API TapViz.
 */
class ApiException(val code: String, message: String, val httpStatus: Int) : Exception(message)

/**
 * Данные текущего аккаунта пользователя.
 */
data class TapVizAccount(val email: String, val cards: List<TapVizCard>, val isDemo: Boolean)

/**
 * Защищенное хранилище сессионного токена с использованием Android KeyStore (AES/GCM).
 */
private class SessionVault(context: Context) {
    private val preferences = context.getSharedPreferences("tapviz_private", Context.MODE_PRIVATE)
    private val alias = "tapviz_session_v1"

    /**
     * Считывание и расшифровка сессионной куки.
     */
    fun read(): String? = runCatching {
        val encoded = preferences.getString("session", null) ?: return null
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = payload.copyOfRange(0, 12)
        val ciphertext = payload.copyOfRange(12, payload.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }.onFailure { ex ->
        Log.w(TAG_VAULT, "Не удалось расшифровать сессионную куку из KeyStore: ${ex.message}")
    }.getOrNull()

    /**
     * Шифрование и запись сессионной куки.
     */
    fun write(value: String?) {
        if (value == null) {
            Log.d(TAG_VAULT, "Удаление сессионной куки из хранилища")
            preferences.edit { remove("session") }
            return
        }
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key())
            val encrypted = cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            preferences.edit { putString("session", Base64.encodeToString(encrypted, Base64.NO_WRAP)) }
            Log.d(TAG_VAULT, "Сессионная кука успешно зашифрована и сохранена")
        } catch (ex: Exception) {
            Log.e(TAG_VAULT, "Ошибка шифрования сессионной куки", ex)
        }
    }

    /**
     * Получение или генерация криптографического ключа в Android KeyStore.
     */
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }

        Log.i(TAG_VAULT, "Генерация нового ключа AES/GCM в AndroidKeyStore")
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
            generateKey()
        }
    }
}

/**
 * Клиент взаимодействия с REST API TapViz.
 * Обеспечивает сетевой обмен, авторизацию, создание и редактирование визиток.
 */
class ApiClient(context: Context) {
    private val vault = SessionVault(context.applicationContext)
    private val baseUrl = BuildConfig.API_BASE_URL.trimEnd('/')

    /**
     * Проверка наличия сохраненной авторизационной сессии.
     */
    fun hasSession(): Boolean {
        val exists = vault.read() != null
        Log.d(TAG_API, "Проверка сессии: active=$exists")
        return exists
    }

    /**
     * Регистрация нового аккаунта.
     */
    suspend fun register(email: String, password: String): JSONObject {
        Log.i(TAG_API, "Регистрация аккаунта email=$email")
        return request("/v1/auth/register", "POST", JSONObject().put("email", email).put("password", password))
    }

    /**
     * Авторизация существующего аккаунта.
     */
    suspend fun login(email: String, password: String): JSONObject {
        Log.i(TAG_API, "Вход в аккаунт email=$email")
        return request("/v1/auth/login", "POST", JSONObject().put("email", email).put("password", password))
    }

    /**
     * Вход в демонстрационный аккаунт.
     */
    suspend fun loginDemo(): JSONObject {
        Log.i(TAG_API, "Вход в демо-режим")
        return request("/v1/auth/demo", "POST", JSONObject())
    }

    /**
     * Завершение текущей сессии пользователя.
     */
    suspend fun logout() {
        Log.i(TAG_API, "Выход из аккаунта")
        try {
            request("/v1/auth/logout", "POST", JSONObject())
        } finally {
            vault.write(null)
        }
    }

    /**
     * Удаление аккаунта и связанных с ним данных.
     */
    suspend fun deleteAccount(email: String) {
        Log.w(TAG_API, "Запрос на удаление аккаунта email=$email")
        request("/v1/me", "DELETE", JSONObject().put("confirmation", email))
        vault.write(null)
    }

    /**
     * Загрузка профиля пользователя и его визиток.
     */
    suspend fun loadAccount(): TapVizAccount {
        Log.d(TAG_API, "Загрузка профиля пользователя и списка визиток")
        val me = request("/v1/me")
        val user = me.getJSONObject("user")
        val cardsArray = request("/v1/cards").getJSONArray("data")

        val cardList = mutableListOf<TapVizCard>()
        for (i in 0 until cardsArray.length()) {
            cardList += TapVizCard.from(cardsArray.getJSONObject(i))
        }

        if (cardList.isEmpty() && !user.optBoolean("isDemo")) {
            Log.i(TAG_API, "У пользователя нет визиток, создаем основную по умолчанию")
            cardList += TapVizCard.from(
                request("/v1/cards", "POST", JSONObject().put("label", "Основная карточка")).getJSONObject("data"),
            )
        }
        return TapVizAccount(user.optString("email"), cardList, user.optBoolean("isDemo"))
    }

    /**
     * Создание новой визитки с заданной меткой.
     */
    suspend fun createCard(label: String): TapVizCard {
        Log.i(TAG_API, "Создание визитки label='$label'")
        return TapVizCard.from(
            request("/v1/cards", "POST", JSONObject().put("label", label)).getJSONObject("data"),
        )
    }

    /**
     * Сохранение отредактированной визитки.
     */
    suspend fun save(card: TapVizCard): TapVizCard {
        Log.i(TAG_API, "Сохранение визитки id=${card.id}, version=${card.version}")
        val fields = JSONArray()
        card.fields.filter { it.value.isNotBlank() }.forEach { field ->
            fields.put(
                JSONObject()
                    .put("id", field.id)
                    .put("type", field.type)
                    .put("label", field.label)
                    .put("value", field.value)
                    .put("visibility", field.visibility)
                    .put("enabled", field.enabled),
            )
        }
        val body = JSONObject()
            .put("version", card.version)
            .put("label", card.label)
            .put("displayName", card.displayName)
            .put("role", card.role)
            .put("organization", card.organization)
            .put("bio", card.bio)
            .put("avatarData", card.avatarData ?: JSONObject.NULL)
            .put("backgroundData", card.backgroundData ?: JSONObject.NULL)
            .put("logoData", card.logoData ?: JSONObject.NULL)
            .put("theme", card.theme)
            .put("fields", fields)

        return TapVizCard.from(request("/v1/cards/${card.id}", "PATCH", body).getJSONObject("data"))
    }

    /**
     * Публикация или снятие визитки с публикации.
     */
    suspend fun publish(cardId: String, publish: Boolean): TapVizCard {
        Log.i(TAG_API, "Смена статуса публикации визитки id=$cardId: publish=$publish")
        val action = if (publish) "publish" else "unpublish"
        return TapVizCard.from(
            request("/v1/cards/$cardId/$action", "POST", JSONObject()).getJSONObject("data"),
        )
    }

    /**
     * Создание временной ссылки для предпросмотра веб-версии визитки.
     */
    suspend fun createWebsitePreview(cardId: String): String {
        Log.i(TAG_API, "Запрос временной ссылки предпросмотра для id=$cardId")
        return request("/v1/cards/$cardId/preview", "POST", JSONObject()).getJSONObject("data").getString("url")
    }

    /**
     * Внутренний метод отправки HTTP-запросов к API.
     */
    private suspend fun request(path: String, method: String = "GET", body: JSONObject? = null): JSONObject = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) {
            Log.e(TAG_API, "API_BASE_URL не настроен в конфигурации сборки")
            throw ApiException("api_not_configured", "Адрес сервера не настроен для этой сборки.", 0)
        }
        Log.d(TAG_API, "HTTP Request: $method $path")
        val (status, responseText) = connection(path, method, body)
        val data = runCatching { JSONObject(responseText) }.getOrElse { JSONObject() }

        if (status !in 200..299) {
            val error = data.optJSONObject("error") ?: JSONObject()
            val code = error.optString("code", "network_error")
            val msg = error.optString("message", "Сервер временно недоступен.")
            Log.e(TAG_API, "HTTP Error $status ($code): $msg")
            throw ApiException(code, msg, status)
        }
        data
    }

    /**
     * Низкоуровневая установка соединения HttpURLConnection.
     */
    private fun connection(path: String, method: String, body: JSONObject? = null): Pair<Int, String> {
        val urlBase = baseUrl.ifBlank { "https://tapviz.invalid" }
        val connection = (URL("$urlBase$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 12_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json, image/svg+xml")
            if (method != "GET") setRequestProperty("Origin", urlBase)
            vault.read()?.let { setRequestProperty("Cookie", it) }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        return try {
            body?.let {
                connection.outputStream.use { output ->
                    output.write(it.toString().toByteArray(Charsets.UTF_8))
                }
            }
            connection.getHeaderField("Set-Cookie")
                ?.substringBefore(';')
                ?.takeIf { it.startsWith("tv_session=") }
                ?.let(vault::write)

            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            status to responseText
        } catch (ex: Exception) {
            Log.e(TAG_API, "Сетевая ошибка соединия по пути $path", ex)
            throw ex
        } finally {
            connection.disconnect()
        }
    }
}
