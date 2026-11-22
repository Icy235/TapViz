# TapViz

TapViz — сервис цифровых визиток: владелец создаёт и публикует карточку в Android-приложении, а любой получатель открывает её в браузере по короткой ссылке или QR-коду. Backend и сайт работают как одно Node.js-приложение.

> **Состав проекта:** web/backend находится в `D:\Projects\TapViz`, Android-приложение — в `C:\Users\Icy_S\AndroidStudioProjects\TapViz`. Это две отдельные папки без общего Git-репозитория. Android обращается к серверу по HTTPS API `/v1`.

## Содержание

- [Возможности и ограничения](#возможности-и-ограничения)
- [Карта файлов](#карта-файлов)
- [Требования](#требования)
- [Запуск web/backend локально](#запуск-webbackend-локально)
- [Запуск Android](#запуск-android)
- [Настройки окружения](#настройки-окружения)
- [База данных](#база-данных)
- [Админ-панель и доступ администратора](#админ-панель-и-доступ-администратора)
- [Проверки](#проверки)
- [Деплой на текущий сервер](#деплой-на-текущий-сервер)
- [Переезд на другой сервер](#переезд-на-другой-сервер)
- [Резервное копирование и восстановление](#резервное-копирование-и-восстановление)
- [Безопасность и эксплуатационные особенности](#безопасность-и-эксплуатационные-особенности)
- [Известные ограничения и подготовка к запуску](#известные-ограничения-и-подготовка-к-запуску)

## Возможности и ограничения

- Лендинг TapViz рекламирует приложение; создание и редактирование карточек выполняется в Android-приложении.
- Android: вход и регистрация по email, карточки, редактирование каналов и их видимости, оформление и фон, публикация, QR/ссылка, предпросмотр публичной страницы, экспорт VCF, настройки и удаление аккаунта.
- Web: серверный рендер публичной страницы, безопасные ссылки, сохранение контакта в VCF, QR SVG, временный приватный предпросмотр, адаптивный лендинг и закрытая админ-панель `/admin`.
- Backend: Node.js 24, встроенный HTTP-сервер и SQLite (`node:sqlite`), сессии в HttpOnly cookie, ограничения карточек, серверная проверка владельца и административный аудит.
- Compose Preview доступны в исходниках Android для отдельных экранов и компонентов.

Текущее хранилище — SQLite. Приложение рассчитано на один backend-процесс и один сервер. Не запускайте несколько реплик с отдельными локальными базами и не размещайте SQLite на сетевой файловой системе.

## Карта файлов

### Web/backend: `D:\Projects\TapViz`

| Путь | Назначение |
| --- | --- |
| `server.mjs` | HTTP API `/v1`, SSR, вход/регистрация, сессии, админ API, QR, VCF и rate limiting |
| `src/db/schema.sql` | Базовая схема SQLite: пользователи, карточки, поля, сессии и настройки |
| `src/db/database.mjs` | Открытие SQLite, включение WAL и совместимые миграции при старте |
| `src/profile-renderer.mjs` | SSR публичного профиля и VCF-экранирование |
| `src/channels.mjs`, `src/icons-available.mjs` | Типы каналов, построение безопасных ссылок, список иконок |
| `public/index.html`, `public/app.js`, `public/styles.css` | Лендинг и web-интерфейс админки |
| `public/profile.css`, `public/profile.js` | Визуальная часть и действия публичного профиля |
| `public/icons/`, `public/fonts/`, `public/tapviz_logo_*.svg` | Статические ресурсы сайта |
| `app/src/main/java/icy/tapviz/app/` в Android-папке | Kotlin UI, модели, API-клиент, темы, иконки и Compose Preview |
| `app/src/main/res/` в Android-папке | Android-ресурсы, логотипы, шрифты и векторные иконки каналов |
| `Dockerfile`, `docker-compose.yml` | Сборка и запуск production-контейнера |
| `deploy/nginx/tapviz.vorlan.ru.conf` | Nginx reverse proxy для сайта |
| `scripts/fetch-icons.mjs` | Генерация web- и Android-иконок каналов |
| `test/mvp.test.mjs` | Интеграционные проверки API и публичности данных |
| `docs/decisions.md`, `docs/card-system.md` | Архитектурные решения и правила карточек |
| `.env.example` | Пример локальных настроек без секретов |

### Android: `C:\Users\Icy_S\AndroidStudioProjects\TapViz`

| Путь | Назначение |
| --- | --- |
| `app/src/main/java/icy/tapviz/app/MainActivity.kt` | Навигация, сессия, состояния экранов, профиль и настройки |
| `app/src/main/java/icy/tapviz/app/data/ApiClient.kt` | HTTP API и зашифрованное хранение сессии через Android Keystore |
| `app/src/main/java/icy/tapviz/app/ui/home/` | Главный экран и список карточек |
| `app/src/main/java/icy/tapviz/app/ui/editor/` | Редактор, предпросмотр, валидация полей и выбор фонового изображения |
| `app/src/main/java/icy/tapviz/app/ui/card/` | Компоновка карточки и темы |
| `app/src/main/java/icy/tapviz/app/ui/icons/`, `ui/motion/`, `ui/theme/` | Иконки, анимации и дизайн-токены |
| `app/src/main/res/` | Логотипы, шрифты, иконки и системные ресурсы |
| `gradlew.bat`, `gradle/` | Gradle Wrapper; сборку можно повторить без глобальной установки Gradle |
| `local.properties` | Локальный путь к Android SDK; машинный файл, не переносить на другой компьютер |

## Требования

- Web/backend: Node.js `24.x`, Corepack и pnpm `11.25.0` (версия pnpm зафиксирована deployment-инструкцией; lockfile обязателен).
- Android: Android Studio с JDK/toolchain, указанный в Gradle-проекте Android SDK и установленный Android SDK Platform 37. Gradle Wrapper скачает нужную версию Gradle.
- Production: Linux-сервер с Docker Engine + Compose plugin, Nginx и Certbot; DNS A-запись домена и входящие порты 80/443.

## Запуск web/backend локально

Откройте PowerShell в `D:\Projects\TapViz`:

```powershell
Copy-Item .env.example .env
corepack enable
corepack prepare pnpm@11.25.0 --activate
pnpm install --frozen-lockfile
pnpm dev
```

Откройте <http://localhost:4173>. `pnpm dev` перезапускает сервер при изменениях. Обычный запуск: `pnpm start`.

Локальная БД создаётся автоматически в `.data/tapviz.sqlite`. Первый запуск применяет схему и совместимые миграции. Не удаляйте `.data`, если в ней есть нужные локальные аккаунты или карточки.

Для Android-эмулятора API по адресу компьютера доступен как `http://10.0.2.2:4173`. На реальном телефоне используйте LAN IP компьютера и разрешите доступ firewall; для обычного использования предпочтителен HTTPS-сервер.

## Запуск Android

Откройте `C:\Users\Icy_S\AndroidStudioProjects\TapViz` в Android Studio, дождитесь Sync Gradle и запустите конфигурацию `app` на эмуляторе или устройстве.

Сборка debug APK из PowerShell:

```powershell
Set-Location C:\Users\Icy_S\AndroidStudioProjects\TapViz
.\gradlew.bat :app:assembleDebug
```

APK появится в `app\build\outputs\apk\debug\app-debug.apk`. Для локального backend в Gradle property можно переопределить адрес debug API:

```powershell
.\gradlew.bat :app:assembleDebug `
  -PtapvizDebugApiBaseUrl=http://10.0.2.2:4173 `
  -PtapvizDebugPublicBaseUrl=http://10.0.2.2:4173
```

Адреса по умолчанию указывают на `https://tapviz.vorlan.ru`. Release использует `tapvizApiBaseUrl` и `tapvizPublicBaseUrl`. Секреты в Android-сборку не помещаются; подписывание release keystore ещё нужно настроить отдельно. Debug APK не является готовой публикационной сборкой.

## Настройки окружения

Локально скопируйте `.env.example` в `.env`. Для Compose создайте серверный `/opt/tapviz/.env`; он не должен попадать в Git, публичный архив или Docker image.

| Переменная | По умолчанию | Назначение |
| --- | --- | --- |
| `NODE_ENV` | `development` | `production` включает Secure session cookie и production-режим |
| `HOST` | `127.0.0.1` | Адрес прослушивания; в контейнере должен быть `0.0.0.0` |
| `PORT` | `4173` | HTTP-порт приложения |
| `PUBLIC_BASE_URL` | localhost или production domain | Канонический origin для ссылок/QR. Без завершающего `/` |
| `DATABASE_PATH` | `.data/tapviz.sqlite` | Файл SQLite; Compose использует `/app/.data/tapviz.sqlite` |
| `SESSION_TTL_DAYS` | `30` | Срок сессии, сервер ограничивает значение диапазоном 1–90 дней |
| `FREE_CARD_LIMIT` | `3` | Начальное значение лимита карточек; после инициализации текущее значение хранится в `app_settings` и редактируется админкой |
| `ADMIN_EMAIL` | пусто | Email администратора или список через запятую; должны существовать обычные аккаунты с этими email |
| `ANDROID_APP_URL` | пусто | Необязательная HTTPS-ссылка на страницу скачивания Android-приложения |
| `TRUST_PROXY` | `false` | В production за доверенным Nginx выставить `true`, чтобы корректно обрабатывать forwarded protocol |

`FREE_CARD_LIMIT` записывается в SQLite только при первом создании настройки. Изменение `.env` позже не перезапишет значение, которое уже поменяли в админке. OAuth, SMTP и платёжные секреты пока не поддерживаются.

## База данных

Используется SQLite из стандартного `node:sqlite` в Node 24; отдельный контейнер базы данных отсутствует. В локальной разработке файл находится в `.data/tapviz.sqlite`, в production — в постоянном Docker volume `tapviz-data`, смонтированном в `/app/.data`.

Главные таблицы: `users`, `sessions`, `cards`, `card_fields`, `card_previews`, `app_settings`, `admin_audit`. Внешние ключи и `ON DELETE CASCADE` физически удаляют карточки, сессии и связанные данные при удалении аккаунта владельцем. Приостановка аккаунта администратором и удаление карточки администратором — обратимые статусы; карточки можно восстановить в черновик. Для пользовательских удалений нет периода ожидания или автоматического восстановления.

Схема создаётся из `src/db/schema.sql`. Текущие эволюционные миграции сделаны как совместимые проверки/`ALTER TABLE` в `src/db/database.mjs` при старте. Перед изменением схемы сделайте проверенную резервную копию. Для серьёзной миграции, особенно смены SQLite на PostgreSQL, нужен отдельный экспорт/импорт и проверка данных; автоматической миграции на PostgreSQL сейчас нет.

### Посмотреть таблицы на сервере

Подключитесь по SSH, перейдите в `/opt/tapviz`, затем запустите read-only запрос встроенным драйвером Node:

```bash
cd /opt/tapviz
docker compose exec tapviz node --input-type=module -e 'import { DatabaseSync } from "node:sqlite"; const db = new DatabaseSync("/app/.data/tapviz.sqlite", { readOnly: true }); console.table(db.prepare("SELECT name FROM sqlite_master WHERE type = ? ORDER BY name").all("table")); db.close();'
```

Пример агрегата без вывода паролей и пользовательских данных:

```bash
docker compose exec tapviz node --input-type=module -e 'import { DatabaseSync } from "node:sqlite"; const db = new DatabaseSync("/app/.data/tapviz.sqlite", { readOnly: true }); console.table(db.prepare("SELECT status, COUNT(*) AS cards FROM cards GROUP BY status").all()); db.close();'
```

Для графического просмотра остановите контейнер на время копирования либо сделайте консистентный backup командой ниже, скачайте копию и откройте её в SQLite Browser. Не открывайте и не редактируйте production-файл напрямую, пока сервер пишет в него. Изменяйте настройки через API/админ-панель; прямой SQL UPDATE может обойти проверки и аудит.

## API и публичные адреса

API имеет префикс `/v1`. Клиент и браузер используют сессию через HttpOnly cookie; Android хранит полученную cookie зашифрованно. Для защищённых запросов нужен активный вход. Ошибки возвращаются в форме `{"error":{"code":"...","message":"...","requestId":"..."}}`, а `X-Request-Id` можно приложить к сообщению о сбое.

| Маршрут | Доступ | Назначение |
| --- | --- | --- |
| `POST /v1/auth/register` | Публичный, rate limited | Создать аккаунт и первую черновую карточку |
| `POST /v1/auth/login`, `POST /v1/auth/logout` | Вход / публичный выход | Создать или завершить cookie-сессию |
| `POST /v1/auth/demo` | Публичный, rate limited | Выдать сессию read-only демо-пользователя |
| `POST /v1/auth/password/forgot` | Публичный, rate limited | Сейчас отвечает нейтральным сообщением; отправка письма не подключена |
| `GET /v1/me`, `DELETE /v1/me` | Владелец сессии | Данные аккаунта; удаление требует передать email в `confirmation` |
| `GET /v1/cards`, `POST /v1/cards` | Владелец сессии | Список карточек и создание в рамках серверного лимита |
| `GET/PATCH/DELETE /v1/cards/{uuid}` | Владелец карточки | Прочитать, обновить с `version`, либо пометить карточку удалённой |
| `POST /v1/cards/{uuid}/publish`, `/unpublish` | Владелец карточки | Публикация и немедленное скрытие |
| `GET /v1/cards/{uuid}/qr.svg` | Владелец опубликованной карточки | QR-код стабильного публичного URL |
| `POST /v1/cards/{uuid}/preview` | Владелец карточки | Временный предпросмотр на 10 минут; URL является секретом доступа |
| `GET /v1/public/cards/{slug}` | Публичный | Публичный DTO без скрытых полей |
| `POST /v1/public/cards/{slug}/events` | Публичный, rate limited | Сейчас возвращает `503 analytics_disabled`; события не сохраняются |
| `/p/{slug}`, `/p/{slug}/contact.vcf` | Публичный | SSR-профиль и загрузка контакта |
| `/admin`, `/v1/admin/*` | Только allowlist ADMIN_EMAIL и активная сессия | Панель и административные операции |

На текущей версии нет API refresh-token, password reset, duplicate-card, upload presign или рабочей аналитики/покупок. Android фоновые изображения отправляет как ограниченный data URI при сохранении карточки.

### Резервная копия работающей БД

`VACUUM INTO` создаёт согласованный отдельный SQLite-файл с учётом WAL. Выполните на сервере:

```bash
cd /opt/tapviz
docker compose exec tapviz node --input-type=module -e 'import { DatabaseSync } from "node:sqlite"; const db = new DatabaseSync("/app/.data/tapviz.sqlite"); db.exec("VACUUM INTO \x27/tmp/tapviz-backup.sqlite\x27"); db.close();'
docker cp tapviz:/tmp/tapviz-backup.sqlite ./tapviz-backup.sqlite
docker compose exec tapviz rm -f /tmp/tapviz-backup.sqlite
```

Если удобнее запустить резервное копирование интерактивно, используйте Node:

```bash
docker compose exec -it tapviz node --input-type=module
```

```js
import { DatabaseSync } from 'node:sqlite';
const db = new DatabaseSync('/app/.data/tapviz.sqlite');
db.exec("VACUUM INTO '/tmp/tapviz-backup.sqlite'");
db.close();
```

Затем в другом окне SSH выполните `docker cp tapviz:/tmp/tapviz-backup.sqlite ./tapviz-backup.sqlite`. Проверьте копию:

```bash
node --input-type=module -e 'import { DatabaseSync } from "node:sqlite"; const db = new DatabaseSync("./tapviz-backup.sqlite", { readOnly: true }); console.log(db.prepare("PRAGMA integrity_check").get()); db.close();'
```

Скопируйте backup за пределы сервера и ограничьте доступ к нему: он содержит email, хеши паролей, сессии и карточки. Не храните единственную копию на том же диске.

## Админ-панель и доступ администратора

1. Зарегистрируйте обычный аккаунт TapViz через сайт или приложение.
2. На сервере задайте `ADMIN_EMAIL` ровно для этого аккаунта (несколько адресов разделяются запятой) в `/opt/tapviz/.env`.
3. Пересоздайте контейнер: `docker compose up -d`.
4. Откройте `https://tapviz.vorlan.ru/admin` и войдите email и обычным паролем аккаунта.

Отдельного пароля админки нет. Сервер хранит только хеш пароля; пароль нельзя прочитать из БД. Сброс пароля через email пока не настроен. Демо-аккаунт доступен только для чтения и администратором стать не может. Не выдавайте админский email случайным/публичным аккаунтам.

Панель показывает сводку, позволяет искать аккаунты и карточки, приостанавливать/восстанавливать поддерживаемые записи, менять лимит Free и просматривать журнал административных действий. HTTP-авторизация API остаётся на стороне сервера; одного знания URL `/admin` недостаточно.

## Проверки

Команды из `D:\Projects\TapViz`:

```powershell
pnpm test
node --check server.mjs
node --check public/app.js
```

Android:

```powershell
Set-Location C:\Users\Icy_S\AndroidStudioProjects\TapViz
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

Проверить локальный HTTP health-путь/планы: `Invoke-RestMethod http://localhost:4173/v1/plans` в PowerShell. На production: `curl -fsS https://tapviz.vorlan.ru/v1/plans`.

## Деплой на текущий сервер

- Домен: `tapviz.vorlan.ru`.
- Сервер: Ubuntu, каталог приложения `/opt/tapviz`.
- Внешний трафик обслуживает Nginx и сертификат Let’s Encrypt; Nginx проксирует на `127.0.0.1:4173`.
- Приложение запускается через Docker Compose; SQLite хранится в именованном volume `tapviz-data`.

Полная последовательность первой установки, обновления, Nginx и TLS находится в [`deploy/README.md`](deploy/README.md). Типовое обновление кода:

```bash
cd /opt/tapviz
# Сначала сделать backup БД при изменении схемы.
docker compose up -d --build
docker compose ps
docker compose logs --tail=100 tapviz
curl -fsS http://127.0.0.1:4173/v1/plans
```

Не копируйте локальную `.data` и не удаляйте Docker volume при обновлении. Веб/API и Android — независимые поставки: серверный backend нужно развернуть до клиента, использующего новый API.

## Переезд на другой сервер

1. Подготовьте Linux с Docker Engine/Compose, Nginx и Certbot; откройте входящие TCP 80/443. Установите DNS A/AAAA на новый сервер и дождитесь распространения DNS.
2. Сохраните с прежнего сервера `/opt/tapviz/.env`, SQLite backup и при необходимости текущие Nginx/TLS сведения. Передавайте `.env` и backup только защищённым каналом; не коммитьте их.
3. Скопируйте исходники web/backend в `/opt/tapviz` без `node_modules`, `.data`, `.env` и локальных IDE/build-кэшей. Нужны `Dockerfile`, `docker-compose.yml`, `package.json`, `pnpm-lock.yaml`, `server.mjs`, `src/`, `public/`, `deploy/`.
4. Восстановите `.env`, обновите `PUBLIC_BASE_URL` на HTTPS-домен. Проверьте `ADMIN_EMAIL`, лимиты и прочие настройки.
5. Выпустите новый сертификат Let’s Encrypt после обновления DNS; установите Nginx vhost из `deploy/nginx/tapviz.vorlan.ru.conf`, `nginx -t`, перезагрузите Nginx. Для другого домена измените `server_name` и публичный URL.
6. Запустите приложение: `docker compose up -d --build`. Убедитесь, что контейнер healthy и `/v1/plans` отвечает.
7. Остановите приложение перед импортом базы, скопируйте проверенный backup в Docker volume по пути `/app/.data/tapviz.sqlite`, выставьте владельца файла UID пользователя контейнера (образ запускается как `node`, обычно UID 1000), затем поднимите контейнер.
8. Проверьте вход, карточки, публикацию, VCF, HTTPS и Android-клиент. Android уже настроен на `https://tapviz.vorlan.ru`; при смене домена соберите APK с новыми `tapvizApiBaseUrl` и `tapvizPublicBaseUrl`.
9. После проверки настройте независимый off-site backup и только потом выводите старый сервер из эксплуатации.

Образ содержит Node runtime, зависимости и исходники сайта; отдельной миграции БД при обычной пересборке нет — данные переживают пересоздание контейнера, потому что находятся в volume. Сама БД не включена в образ.

## Резервное копирование и восстановление

- Делайте автоматическую ежедневную копию в зашифрованное внешнее хранилище; срок хранения назначьте с учётом требований рынка и объёма.
- Периодически проверяйте `PRAGMA integrity_check` на копии и делайте тестовое восстановление на отдельной staging-инстанции.
- Для восстановления остановите приложение, сделайте копию текущего volume на всякий случай, поместите проверенный файл `tapviz.sqlite` в `/app/.data/` и запустите контейнер. Схема на старте применит совместимые миграции.
- Не удаляйте `tapviz-data` через `docker compose down -v`: флаг `-v` удалит данные.

## Безопасность и эксплуатационные особенности

- HTTPS завершается на Nginx. Контейнерный порт привязан только к `127.0.0.1`; не публикуйте 4173 наружу.
- Пароли хешируются scrypt; случайные session tokens хранятся в SQLite только как SHA-256-хеши и выдаются через HttpOnly cookie. Android хранит cookie зашифрованно Android Keystore.
- Проверка владельца карточек выполняется на сервере. Публичный DTO отдаёт только опубликованные, включённые и открытые поля.
- Публичная ссылка остаётся стабильной при редактировании. Снятая с публикации карточка недоступна гостям; предпросмотр закрыт случайным токеном на короткий срок и запрещён к индексации.
- Загрузки аватара/фона ограничены и проверяются сервером; фон/аватар сохраняются в SQLite как data URI, поэтому большие изображения увеличивают БД и размер резервной копии.
- За reverse proxy включён `TRUST_PROXY=true`; не используйте это значение, если приложение доступно в обход доверенного прокси.
- Не передавайте SSH-пароли, API-ключи и `.env` в исходники, Android build config, логи или общие архивы.

## Известные ограничения и подготовка к запуску

- Только SQLite и один экземпляр backend; горизонтальное масштабирование и автоматическая PostgreSQL-миграция отсутствуют.
- Нет подтверждения email и рабочего восстановления пароля; для восстановления потребуется подключить надёжный email/auth-провайдер.
- Сбор аналитики и платежи выключены; тарифные цены и подписки не настроены.
- Юридические документы, согласия и политика хранения данных требуют проверки для выбранной страны запуска.
- Android debug APK собирается воспроизводимо при наличии SDK, но release keystore, подпись, канал распространения и публичная ссылка на скачивание требуют настройки.
- До реального пользовательского запуска нужны внешние резервные копии и мониторинг свободного диска/доступности процесса.

## Лицензии и бренд

Логотип и название TapViz — материалы продукта. Зависимости перечислены в `package.json` и `gradle/libs.versions.toml`; перед распространением проверьте их лицензии и лицензию шрифта Montserrat. Иконки социальных сервисов генерируются из `simple-icons`.
