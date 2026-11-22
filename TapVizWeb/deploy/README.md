# TapViz: установка и обновление production

Текущая схема: Nginx → `127.0.0.1:4173` → один Node 24 контейнер → SQLite в named Docker volume. Приложение размещено в `/opt/tapviz`, домен — `tapviz.vorlan.ru`. Инструкции рассчитаны на Ubuntu с Docker Compose plugin, Nginx и Certbot.

> Не отправляйте пароль или приватный SSH-ключ через чат/репозиторий. Для регулярного доступа используйте SSH key и отдельного deploy-пользователя.

## Первая установка

### 1. Подготовить DNS и сервер

- Направьте DNS A (и при наличии AAAA) домена на IP сервера.
- Разрешите входящие TCP-порты 80 и 443 на firewall и у провайдера.
- Установите Docker Engine с Compose plugin, Nginx и Certbot с Nginx plugin.
- Проверьте `docker compose version`, `nginx -v`, `certbot --version`.

Не выполняйте автоматическую установку/перенастройку Nginx вслепую на сервере, где уже работают другие домены. Конфиг TapViz добавляется отдельным vhost.

### 2. Поместить исходники

Создайте `/opt/tapviz` и скопируйте туда файлы проекта, необходимые `Dockerfile`:

```text
Dockerfile, docker-compose.yml, package.json, pnpm-lock.yaml,
server.mjs, src/, public/
```

Для установки Nginx также нужны `deploy/nginx/tapviz.vorlan.ru.conf` и `deploy/README.md`. Не копируйте `node_modules`, `.data`, локальный `.env`, `.idea`, `.gradle`, `build` и тестовые артефакты.

### 3. Настроить окружение

```bash
cd /opt/tapviz
install -m 0600 /dev/null .env
nano .env
```

Пример (вставьте только значения, которые действительно выбрали):

```dotenv
PUBLIC_BASE_URL=https://tapviz.vorlan.ru
SESSION_TTL_DAYS=30
FREE_CARD_LIMIT=3
ADMIN_EMAIL=admin@example.com
ANDROID_APP_URL=
```

`ADMIN_EMAIL` — email уже созданного обычного аккаунта; несколько email разделяются запятыми. Не храните в этом файле пароли пользователей. Настройка не даёт возможности восстановить пароль.

### 4. Собрать и запустить приложение

```bash
docker compose up -d --build
docker compose ps
docker compose logs --tail=100 tapviz
curl -fsS http://127.0.0.1:4173/v1/plans
```

Compose привязывает порт только к loopback. Не меняйте публикацию порта на `0.0.0.0`.

### 5. Установить Nginx vhost

```bash
install -m 0644 deploy/nginx/tapviz.vorlan.ru.conf /etc/nginx/sites-available/tapviz.vorlan.ru
ln -s /etc/nginx/sites-available/tapviz.vorlan.ru /etc/nginx/sites-enabled/tapviz.vorlan.ru
nginx -t
systemctl reload nginx
curl -I http://tapviz.vorlan.ru
```

Если ссылка уже существует, не создавайте её повторно. Исправьте конфигурацию только TapViz и перед reload проверьте `nginx -t`.

### 6. Получить TLS-сертификат

Сначала убедитесь, что публичный DNS уже возвращает IP сервера и HTTP доступен снаружи:

```bash
getent ahosts tapviz.vorlan.ru
certbot --nginx --redirect -d tapviz.vorlan.ru
certbot renew --dry-run
curl -I https://tapviz.vorlan.ru
```

Certbot может запросить email для уведомлений — укажите существующий адрес владельца домена. Не используйте `--register-unsafely-without-email` для нового production-развёртывания.

## Обновить приложение

Сначала передайте новые исходники в `/opt/tapviz`, затем:

```bash
cd /opt/tapviz
docker compose up -d --build
docker compose ps
docker compose logs --tail=100 tapviz
curl -fsS http://127.0.0.1:4173/v1/plans
curl -I https://tapviz.vorlan.ru
```

Пересоздание контейнера сохраняет `tapviz-data`. Не запускайте `docker compose down -v`: `-v` удалит постоянную базу. Если изменяется схема или база содержит реальные пользовательские данные, сначала сделайте и проверьте резервную копию.

## Подключение к SQLite

В контейнере файл находится по `/app/.data/tapviz.sqlite`. Чтобы посмотреть список таблиц read-only:

```bash
cd /opt/tapviz
docker compose exec tapviz node --input-type=module -e 'import { DatabaseSync } from "node:sqlite"; const db = new DatabaseSync("/app/.data/tapviz.sqlite", { readOnly: true }); console.table(db.prepare("SELECT name FROM sqlite_master WHERE type = ? ORDER BY name").all("table")); db.close();'
```

Передайте SQL через `db.prepare(...).all(...)` или `.get(...)`; не выводите `password_hash`, session tokens/их хеши и содержимое карточек без необходимости. Прямые записи в БД могут обходить проверки API и административный audit log.

Посмотреть Docker volume и его mountpoint:

```bash
docker volume ls
docker volume inspect tapviz_tapviz-data
```

Имя volume зависит от Compose project name. Используйте `Name`/`Mountpoint`, возвращённые inspect, а не предполагайте путь вручную.

## Backup работающей базы

SQLite включена в WAL mode, поэтому не копируйте только `tapviz.sqlite` из активного volume обычным `cp`. Создайте согласованный снимок через SQLite `VACUUM INTO`:

```bash
cd /opt/tapviz
docker compose exec tapviz node --input-type=module -e 'import { DatabaseSync } from "node:sqlite"; const db = new DatabaseSync("/app/.data/tapviz.sqlite"); db.exec("VACUUM INTO \x27/tmp/tapviz-backup.sqlite\x27"); db.close();'
docker cp tapviz:/tmp/tapviz-backup.sqlite ./tapviz-backup.sqlite
docker compose exec tapviz rm -f /tmp/tapviz-backup.sqlite
```

Убедитесь, что файл появился, и проверьте integrity:

```bash
docker run --rm -v "$PWD:/backup:ro" node:24.22.1-bookworm-slim node --input-type=module -e 'import { DatabaseSync } from "node:sqlite"; const db = new DatabaseSync("/backup/tapviz-backup.sqlite", { readOnly: true }); console.log(db.prepare("PRAGMA integrity_check").get()); db.close();'
```

Скопируйте проверенную резервную копию на другой сервер или в зашифрованное внешнее хранилище. SQLite содержит пользовательские email, хеши паролей, сессии и карточки; ограничьте доступ и срок хранения.

## Перенести на другой сервер

### Подготовить сервер назначения

1. Установите Docker Engine/Compose, Nginx и Certbot.
2. Обновите DNS домена на новый IP, разрешите порты 80/443.
3. Создайте `/opt/tapviz`, передайте чистые исходники и восстановите `/opt/tapviz/.env` с правами `0600`.
4. Сначала запустите пустой контейнер, чтобы Docker создал volume, затем остановите его:

```bash
cd /opt/tapviz
chmod 600 .env
docker compose up -d --build
docker compose stop tapviz
```

Уточните точное имя volume:

```bash
docker volume ls
```

Compose обычно именует его `<имя-папки>_tapviz-data`; в текущем каталоге это может быть `tapviz_tapviz-data`. Не копируйте данные в volume, пока не проверили имя и mountpoint.

5. Передайте backup SQLite в `/opt/tapviz/tapviz-backup.sqlite` защищённым каналом. Проверьте `PRAGMA integrity_check` на копии до восстановления.
6. Скопируйте backup в volume через временный Alpine-контейнер. Подставьте точное имя volume из предыдущего шага и убедитесь, что старая БД уже сохранена отдельно:

```bash
docker run --rm --user 0:0 \
  -v tapviz_tapviz-data:/data \
  -v /opt/tapviz:/backup:ro \
  alpine:3.21 sh -c 'test -s /backup/tapviz-backup.sqlite && cp /backup/tapviz-backup.sqlite /data/tapviz.sqlite && rm -f /data/tapviz.sqlite-wal /data/tapviz.sqlite-shm && chown 1000:1000 /data/tapviz.sqlite && chmod 600 /data/tapviz.sqlite'
```

Это пример для volume `tapviz_tapviz-data`; замените его фактическим именем. Приложение запускается как `node` (обычно UID/GID 1000), поэтому файл должен принадлежать этому пользователю.
7. Запустите приложение и проверьте логи, `/v1/plans`, вход в аккаунт, несколько публичных ссылок, QR и VCF.
8. Добавьте Nginx конфигурацию, проверьте `nginx -t`, запросите сертификат Let’s Encrypt после обновления DNS.
9. Настройте ежедневное off-host backup. Оставьте старый сервер доступным до завершения проверки нового.

Если домен меняется, обновите `PUBLIC_BASE_URL`, `server_name` Nginx и пересоберите Android с новыми `tapvizApiBaseUrl` / `tapvizPublicBaseUrl`. Передайте новую подписанную сборку пользователям. Старый домен можно оставить редиректом только после проверки ссылок и TLS.

## Восстановить backup в существующий volume

1. Сохраните текущую БД отдельной копией.
2. Остановите приложение, чтобы закрыть SQLite и WAL:

```bash
cd /opt/tapviz
docker compose stop tapviz
```

3. Скопируйте проверенный `tapviz-backup.sqlite` в volume той же командой с временным Alpine-контейнером выше. Замените имя volume и путь host backup при необходимости. Старые WAL/SHM удаляйте только при остановленном приложении.
4. Проверьте владельца/права базы и запустите:

```bash
docker compose up -d
docker compose logs --tail=100 tapviz
```

5. Проверьте работоспособность и `PRAGMA integrity_check` до удаления прежней копии.

## Диагностика

```bash
docker compose ps
docker compose logs --tail=200 tapviz
curl -i http://127.0.0.1:4173/v1/plans
nginx -t
systemctl status nginx --no-pager
certbot certificates
df -h
docker system df
```

- Контейнер перезапускается: смотрите `docker compose logs`; проверьте права/свободное место и переменные окружения.
- Сайт доступен по HTTP, но не HTTPS: проверьте DNS, firewall, `certbot certificates` и Nginx error log.
- Админка не пускает: проверьте `ADMIN_EMAIL`, соответствие email существующему аккаунту и обычный пароль пользователя.
- Android выдаёт сетевую ошибку: проверьте, что API URL использует публичный HTTPS, контейнер healthy и сертификат действителен.

## Эксплуатационные пределы

- Одно приложение и локальная SQLite база подходят для начального трафика, но не для горизонтального масштабирования. Перед несколькими инстансами и заметным ростом нагрузки спроектируйте PostgreSQL migration и объектное хранилище.
- Платежи, подтверждение email, reset-password email, сбор аналитики и production-мониторинг не подключены.
- Фоны и аватары хранятся в SQLite; размер БД и копий растёт вместе с изображениями.
- Юридические документы и сроки хранения требуют проверки для выбранной юрисдикции.
